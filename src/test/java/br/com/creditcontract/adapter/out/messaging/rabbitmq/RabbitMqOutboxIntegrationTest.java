package br.com.creditcontract.adapter.out.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxRelay;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"credit-contract.outbox.initial-delay=1h",
		"credit-contract.outbox.fixed-delay=1h",
		"credit-contract.outbox.confirm-timeout=10s"
})
@Testcontainers
class RabbitMqOutboxIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@Container
	static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:4.3.2-management-alpine");

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

	@Autowired
	private CreditContractRepository repository;

	@Autowired
	private OutboxRelay relay;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldPublishPendingEventWithMetadataAndMarkItAfterBrokerConfirmation() {
		CreditContract contract = sampleContract();
		repository.save(contract);

		relay.publishPending();

		Message message = rabbitTemplate.receive(
				RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE,
				Duration.ofSeconds(10).toMillis());
		assertNotNull(message);
		assertEquals("CreditContractCreated", message.getMessageProperties().getType());
		assertEquals(1, ((Number) message.getMessageProperties().getHeader("schemaVersion")).intValue());
		assertEquals(
				contract.getId().value().toString(),
				message.getMessageProperties().getHeader("aggregateId"));
		assertNotNull(message.getMessageProperties().getMessageId());
		assertNotNull(message.getMessageProperties().getCorrelationId());
		assertTrue(new String(message.getBody(), StandardCharsets.UTF_8)
				.contains(contract.getId().value().toString()));

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT publication_status, publication_attempts, published_at FROM outbox_events WHERE aggregate_id = ?",
				contract.getId().value());
		assertEquals("PUBLISHED", outbox.get("publication_status"));
		assertEquals(0, outbox.get("publication_attempts"));
		assertNotNull(outbox.get("published_at"));
	}

	private CreditContract sampleContract() {
		return CreditContract.create(
				ContractId.generate(),
				"CT-2026-000101",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				MonetaryAmount.reais(new BigDecimal("5000.00")));
	}
}
