package br.com.creditcontract.adapter.in.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.messaging.rabbitmq.RabbitMqTopology;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxRelay;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;
import br.com.creditcontract.application.usecase.ProcessCreditReanalysisCommand;
import br.com.creditcontract.application.usecase.ProcessCreditReanalysisUseCase;
import br.com.creditcontract.application.usecase.RequestCreditReanalysisUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.exception.CreditReanalysisCooldownException;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"credit-contract.outbox.initial-delay=1h",
		"credit-contract.outbox.fixed-delay=1h",
		"credit-contract.outbox.confirm-timeout=10s",
		"credit-contract.reanalysis.cooldown=30d",
		"spring.rabbitmq.listener.simple.retry.initial-interval=10ms",
		"spring.rabbitmq.listener.simple.retry.max-interval=20ms"
})
@Testcontainers
class CreditReanalysisWorkflowIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@Container
	static final RabbitMQContainer RABBITMQ =
			new RabbitMQContainer("rabbitmq:4.3.2-management-alpine");

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
		registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
		registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
		registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
	}

	@Autowired private CreditContractRepository repository;
	@Autowired private RequestCreditReanalysisUseCase requestUseCase;
	@Autowired private ProcessCreditReanalysisUseCase processUseCase;
	@Autowired private OutboxRelay relay;
	@Autowired private RabbitTemplate rabbitTemplate;
	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void shouldApproveAsynchronouslyPersistAuditAndIgnoreDuplicateDelivery() {
		CreditContract contract = activeContract("52998224725", "5000.00");
		repository.save(contract);
		UUID correlationId = UUID.randomUUID();
		CreditReanalysisRequestResult request = requestUseCase.execute(
				contract.getId(), correlationId);

		assertEquals(CreditReanalysisStatus.REQUESTED,
				find(contract).getCreditReanalyses().getFirst().getStatus());
		assertThrows(CreditReanalysisCooldownException.class,
				() -> requestUseCase.execute(contract.getId(), UUID.randomUUID()));

		relay.publishPending();
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			CreditContract completed = find(contract);
			assertEquals(CreditReanalysisStatus.APPROVED,
					completed.getCreditReanalyses().getFirst().getStatus());
			assertEquals(new BigDecimal("10000.00"), completed.getCreditLimit().amount());
		});

		Map<String, Object> outcome = outcome(contract, "CreditReanalysisApproved");
		assertEquals(correlationId, outcome.get("correlation_id"));
		assertEquals(request.requestId(), outcome.get("causation_id"));
		String payload = outcome.get("payload").toString();
		assertTrue(payload.contains("\"previousLimit\": \"5000.00\""));
		assertTrue(payload.contains("\"newLimit\": \"10000.00\""));
		assertEquals(1, processedMessageCount(request.requestId()));

		processUseCase.execute(new ProcessCreditReanalysisCommand(
				contract.getId(), request.requestId(), correlationId));
		assertEquals(1, outcomeCount(contract));
		assertEquals(1, processedMessageCount(request.requestId()));

		relay.publishPending();
		Message result = receiveResult("CreditReanalysisApproved");
		assertNotNull(result);
		assertEquals("CreditReanalysisApproved", result.getMessageProperties().getType());
	}

	@Test
	void shouldRejectDeterministicallyAndRetainCurrentLimit() {
		CreditContract contract = activeContract("10000000280", "5000.00");
		repository.save(contract);
		CreditReanalysisRequestResult request = requestUseCase.execute(
				contract.getId(), UUID.randomUUID());

		relay.publishPending();
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			CreditContract completed = find(contract);
			assertEquals(CreditReanalysisStatus.REJECTED,
					completed.getCreditReanalyses().getFirst().getStatus());
			assertEquals(new BigDecimal("5000.00"), completed.getCreditLimit().amount());
		});

		Map<String, Object> outcome = outcome(contract, "CreditReanalysisRejected");
		assertEquals(request.requestId(), outcome.get("causation_id"));
		String payload = outcome.get("payload").toString();
		assertTrue(payload.contains("\"previousLimit\": \"5000.00\""));
		assertTrue(payload.contains("\"retainedLimit\": \"5000.00\""));
		assertTrue(payload.contains("Credit reanalysis policy criteria not met"));
	}

	@Test
	void shouldDeadLetterInvalidReanalysisMessageAfterBoundedRetries() {
		UUID eventId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		Message poison = MessageBuilder
				.withBody("{}".getBytes(StandardCharsets.UTF_8))
				.setMessageId(eventId.toString())
				.setCorrelationId(correlationId.toString())
				.build();

		rabbitTemplate.send("", RabbitMqTopology.CREDIT_REANALYSIS_REQUESTS_QUEUE, poison);

		Message deadLetter = rabbitTemplate.receive(
				RabbitMqTopology.CREDIT_REANALYSIS_DLQ,
				Duration.ofSeconds(10).toMillis());
		assertNotNull(deadLetter);
		assertEquals(eventId.toString(), deadLetter.getMessageProperties().getMessageId());
		assertNotNull(deadLetter.getMessageProperties().getHeaders().get("x-death"));
	}

	private CreditContract activeContract(String documentNumber, String limit) {
		LocalDateTime now = LocalDateTime.now().minusDays(1);
		return CreditContract.rehydrate(
				ContractId.generate(), "CT-2026-" + UUID.randomUUID().toString().substring(0, 12),
				new Client(DocumentNumber.from(documentNumber), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.ACTIVE, MonetaryAmount.reais(new BigDecimal(limit)),
				now, now, 4L, List.of());
	}

	private CreditContract find(CreditContract contract) {
		return repository.findById(contract.getId()).orElseThrow();
	}

	private Map<String, Object> outcome(CreditContract contract, String eventType) {
		return jdbcTemplate.queryForMap(
				"SELECT payload::text AS payload, correlation_id, causation_id "
						+ "FROM outbox_events WHERE aggregate_id = ? AND event_type = ?",
				contract.getId().value(), eventType);
	}

	private int outcomeCount(CreditContract contract) {
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ? "
						+ "AND event_type IN ('CreditReanalysisApproved', 'CreditReanalysisRejected')",
				Integer.class, contract.getId().value());
	}

	private int processedMessageCount(UUID eventId) {
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM processed_messages WHERE event_id = ?",
				Integer.class, eventId);
	}

	private Message receiveResult(String expectedType) {
		long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
		while (System.nanoTime() < deadline) {
			Message message = rabbitTemplate.receive(
					RabbitMqTopology.CREDIT_REANALYSIS_RESULTS_QUEUE, 500);
			if (message != null && expectedType.equals(message.getMessageProperties().getType())) {
				return message;
			}
		}
		return null;
	}
}
