package br.com.creditcontract.adapter.in.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.messaging.rabbitmq.RabbitMqTopology;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxRelay;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.usecase.ActivateCreditContractCommand;
import br.com.creditcontract.application.usecase.ActivateCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractAccepted;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"credit-contract.outbox.initial-delay=1h",
		"credit-contract.outbox.fixed-delay=1h",
		"credit-contract.outbox.confirm-timeout=10s",
		"spring.rabbitmq.listener.simple.retry.initial-interval=10ms",
		"spring.rabbitmq.listener.simple.retry.max-interval=20ms"
})
@Testcontainers
class ContractActivationWorkflowIntegrationTest {

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
	@Autowired private ActivateCreditContractUseCase activateUseCase;
	@Autowired private OutboxRelay relay;
	@Autowired private RabbitTemplate rabbitTemplate;
	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void shouldActivateAcceptedContractAndIgnoreDuplicateDelivery() {
		CreditContract contract = approvedContract();
		repository.save(contract);
		UUID correlationId = UUID.randomUUID();
		contract.accept(correlationId);
		CreditContractAccepted accepted =
				(CreditContractAccepted) contract.getDomainEvents().getFirst();
		repository.save(contract);

		relay.publishPending();

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
				assertEquals(ContractStatus.ACTIVE,
						repository.findById(contract.getId()).orElseThrow().getStatus()));

		CreditContract active = repository.findById(contract.getId()).orElseThrow();
		assertEquals(ContractStatus.ACCEPTED,
				active.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.ACTIVE, active.getStatusHistory().getLast().newStatus());
		assertEquals(1, processedMessageCount(accepted.eventId()));
		assertActivatedEvent(contract, accepted);

		activateUseCase.execute(new ActivateCreditContractCommand(
				contract.getId(), accepted.eventId(), correlationId));
		assertEquals(2, lifecycleEventCount(contract));
		assertEquals(1, processedMessageCount(accepted.eventId()));

		relay.publishPending();
		Message result = rabbitTemplate.receive(
				RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_RESULTS_QUEUE,
				Duration.ofSeconds(10).toMillis());
		assertNotNull(result);
		assertEquals("CreditContractActivated", result.getMessageProperties().getType());
	}

	@Test
	void shouldDeadLetterInvalidActivationMessageAfterBoundedRetries() {
		UUID eventId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		Message poison = MessageBuilder
				.withBody("{}".getBytes(StandardCharsets.UTF_8))
				.setMessageId(eventId.toString())
				.setCorrelationId(correlationId.toString())
				.build();

		rabbitTemplate.send("", RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE, poison);

		Message deadLetter = rabbitTemplate.receive(
				RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_DLQ,
				Duration.ofSeconds(10).toMillis());
		assertNotNull(deadLetter);
		assertEquals(eventId.toString(), deadLetter.getMessageProperties().getMessageId());
		assertNotNull(deadLetter.getMessageProperties().getHeaders().get("x-death"));
	}

	private CreditContract approvedContract() {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(
				ContractId.generate(),
				"CT-2026-000401",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.APPROVED,
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				now,
				now,
				2L,
				List.of());
	}

	private int processedMessageCount(UUID eventId) {
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM processed_messages WHERE event_id = ?",
				Integer.class,
				eventId);
	}

	private int lifecycleEventCount(CreditContract contract) {
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ? "
						+ "AND event_type IN ('CreditContractAccepted', 'CreditContractActivated')",
				Integer.class,
				contract.getId().value());
	}

	private void assertActivatedEvent(CreditContract contract, CreditContractAccepted accepted) {
		Map<String, Object> event = jdbcTemplate.queryForMap(
				"SELECT event_type, payload::text AS payload, correlation_id, causation_id "
						+ "FROM outbox_events WHERE aggregate_id = ? "
						+ "AND event_type = 'CreditContractActivated'",
				contract.getId().value());
		assertEquals("CreditContractActivated", event.get("event_type"));
		assertEquals(accepted.correlationId(), event.get("correlation_id"));
		assertEquals(accepted.eventId(), event.get("causation_id"));
		assertTrue(event.get("payload").toString().contains(contract.getId().asString()));
	}
}
