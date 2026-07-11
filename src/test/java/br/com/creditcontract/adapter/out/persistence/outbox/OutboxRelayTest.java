package br.com.creditcontract.adapter.out.persistence.outbox;

import br.com.creditcontract.adapter.out.persistence.jpa.CreditContractPersistenceAdapter;
import br.com.creditcontract.adapter.out.persistence.jpa.CreditContractPersistenceMapper;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.port.out.EventPublication;
import br.com.creditcontract.application.port.out.EventPublicationResult;
import br.com.creditcontract.application.port.out.EventPublisher;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
		CreditContractPersistenceAdapter.class,
		CreditContractPersistenceMapper.class,
		OutboxEventPersistenceAdapter.class,
		OutboxEventSerializer.class,
		OutboxRelay.class,
		JacksonAutoConfiguration.class,
		OutboxRelayTest.PublisherConfiguration.class
})
class OutboxRelayTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("credit-contract.outbox.batch-size", () -> 10);
		registry.add("credit-contract.outbox.retry-delay", () -> "1ms");
		registry.add("credit-contract.outbox.retry-initial-delay", () -> "1ms");
		registry.add("credit-contract.outbox.retry-max-delay", () -> "10ms");
		registry.add("credit-contract.outbox.max-attempts", () -> 3);
	}

	@Autowired
	private CreditContractRepository repository;

	@Autowired
	private OutboxRelay relay;

	@Autowired
	private RecordingEventPublisher publisher;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldRetryUnconfirmedEventsAndNotRepublishConfirmedEvents() {
		publisher.publicationCount = 0;
		CreditContract contract = sampleContract("CT-2026-000100");
		repository.save(contract);
		String eventId = contractEventId(contract);

		publisher.nextResult = EventPublicationResult.failure("broker unavailable");
		relay.publishPending();

		Map<String, Object> pending = outbox(eventId);
		assertEquals("PENDING", pending.get("publication_status"));
		assertEquals(1, pending.get("publication_attempts"));
		assertEquals("broker unavailable", pending.get("last_error"));
		assertNotNull(pending.get("next_attempt_at"));

		jdbcTemplate.update(
				"UPDATE outbox_events SET next_attempt_at = CURRENT_TIMESTAMP WHERE event_id = ?::uuid",
				eventId);
		publisher.nextResult = EventPublicationResult.success();
		relay.publishPending();
		relay.publishPending();

		Map<String, Object> published = outbox(eventId);
		assertEquals("PUBLISHED", published.get("publication_status"));
		assertEquals(1, published.get("publication_attempts"));
		assertNotNull(published.get("published_at"));
		assertNull(published.get("next_attempt_at"));
		assertNull(published.get("last_error"));
		assertEquals(2, publisher.publicationCount);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldStopPublishingAfterBoundedAttempts() {
		publisher.publicationCount = 0;
		CreditContract contract = sampleContract("CT-2026-000102");
		repository.save(contract);
		String eventId = contractEventId(contract);
		publisher.nextResult = EventPublicationResult.failure("broker unavailable");

		for (int attempt = 0; attempt < 3; attempt++) {
			relay.publishPending();
			jdbcTemplate.update(
					"UPDATE outbox_events SET next_attempt_at = CURRENT_TIMESTAMP "
							+ "WHERE event_id = ?::uuid AND publication_status = 'PENDING'",
					eventId);
		}
		relay.publishPending();

		Map<String, Object> failed = outbox(eventId);
		assertEquals("FAILED", failed.get("publication_status"));
		assertEquals(3, failed.get("publication_attempts"));
		assertNull(failed.get("next_attempt_at"));
		assertEquals(3, publisher.publicationCount);
	}

	private String contractEventId(CreditContract contract) {
		return jdbcTemplate.queryForObject(
				"SELECT event_id::text FROM outbox_events WHERE aggregate_id = ?",
				String.class,
				contract.getId().value());
	}

	private Map<String, Object> outbox(String eventId) {
		return jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?::uuid",
				eventId);
	}

	private CreditContract sampleContract(String contractNumber) {
		return CreditContract.create(
				ContractId.generate(),
				contractNumber,
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))));
	}

	@TestConfiguration
	static class PublisherConfiguration {

		@Bean
		RecordingEventPublisher eventPublisher() {
			return new RecordingEventPublisher();
		}

		@Bean
		SimpleMeterRegistry meterRegistry() {
			return new SimpleMeterRegistry();
		}
	}

	static class RecordingEventPublisher implements EventPublisher {

		private EventPublicationResult nextResult = EventPublicationResult.success();
		private int publicationCount;

		@Override
		public EventPublicationResult publish(EventPublication event) {
			publicationCount++;
			return nextResult;
		}
	}
}
