package br.com.creditcontract.adapter.in.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.messaging.rabbitmq.RabbitMqTopology;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxRelay;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.usecase.CreateContractInput;
import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisCommand;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
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

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"credit-contract.outbox.initial-delay=1h",
		"credit-contract.outbox.fixed-delay=1h",
		"credit-contract.outbox.confirm-timeout=10s"
})
@Testcontainers
class CreditAnalysisWorkflowIntegrationTest {

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

	@Autowired private CreateContractUseCase createContractUseCase;
	@Autowired private ProcessCreditAnalysisUseCase processCreditAnalysisUseCase;
	@Autowired private CreditContractRepository repository;
	@Autowired private OutboxRelay relay;
	@Autowired private RabbitTemplate rabbitTemplate;
	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void shouldApproveAndRejectAsynchronouslyAndIgnoreDuplicateDelivery() {
		CreditContract approved = create("52998224725");
		assertEquals(ContractStatus.DRAFT, approved.getStatus());
		assertNull(approved.getCreditLimit());

		Map<String, Object> createdEvent = createdEvent(approved);
		relay.publishPending();
		awaitTerminalState(approved, ContractStatus.APPROVED);

		CreditContract approvedAfterAnalysis = find(approved);
		assertEquals("5000.00", approvedAfterAnalysis.getCreditLimit().amount().toPlainString());
		assertEquals(3, approvedAfterAnalysis.getStatusHistory().size());
		assertEquals(2L, approvedAfterAnalysis.getVersion());
		assertOutcome(approved, "CreditAnalysisApproved", "5000.00", null);

		processCreditAnalysisUseCase.execute(new ProcessCreditAnalysisCommand(
				approved.getId(),
				(UUID) createdEvent.get("event_id"),
				(UUID) createdEvent.get("correlation_id")));
		assertEquals(3, find(approved).getStatusHistory().size());
		assertEquals(2, outboxCount(approved));

		relay.publishPending();
		Message approvedResult = rabbitTemplate.receive(
				RabbitMqTopology.CREDIT_ANALYSIS_RESULTS_QUEUE,
				Duration.ofSeconds(10).toMillis());
		assertNotNull(approvedResult);
		assertEquals("CreditAnalysisApproved", approvedResult.getMessageProperties().getType());

		CreditContract rejected = create("10000000280");
		relay.publishPending();
		awaitTerminalState(rejected, ContractStatus.REJECTED);

		CreditContract rejectedAfterAnalysis = find(rejected);
		assertNull(rejectedAfterAnalysis.getCreditLimit());
		assertEquals("Credit policy criteria not met",
				rejectedAfterAnalysis.getStatusHistory().getLast().reason());
		assertOutcome(
				rejected,
				"CreditAnalysisRejected",
				null,
				"Credit policy criteria not met");
	}

	private CreditContract create(String documentNumber) {
		return createContractUseCase.execute(
				new CreateContractInput(DocumentNumber.from(documentNumber)));
	}

	private void awaitTerminalState(CreditContract contract, ContractStatus expected) {
		await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
				assertEquals(expected, find(contract).getStatus()));
	}

	private CreditContract find(CreditContract contract) {
		return repository.findById(contract.getId()).orElseThrow();
	}

	private Map<String, Object> createdEvent(CreditContract contract) {
		return jdbcTemplate.queryForMap(
				"SELECT event_id, correlation_id FROM outbox_events "
						+ "WHERE aggregate_id = ? AND event_type = 'CreditContractCreated'",
				contract.getId().value());
	}

	private int outboxCount(CreditContract contract) {
		return jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ?",
				Integer.class,
				contract.getId().value());
	}

	private void assertOutcome(
			CreditContract contract,
			String eventType,
			String approvedLimit,
			String reason) {
		Map<String, Object> event = jdbcTemplate.queryForMap(
				"SELECT event_type, payload::text AS payload, correlation_id, causation_id "
						+ "FROM outbox_events WHERE aggregate_id = ? AND event_type = ?",
				contract.getId().value(),
				eventType);
		assertEquals(eventType, event.get("event_type"));
		assertNotNull(event.get("correlation_id"));
		assertNotNull(event.get("causation_id"));
		String payload = event.get("payload").toString();
		assertTrue(payload.contains(contract.getId().asString()));
		if (approvedLimit != null) {
			assertTrue(payload.contains("\"approvedLimit\": \"" + approvedLimit + "\""));
		}
		if (reason != null) {
			assertTrue(payload.contains("\"reason\": \"" + reason + "\""));
		}
	}
}
