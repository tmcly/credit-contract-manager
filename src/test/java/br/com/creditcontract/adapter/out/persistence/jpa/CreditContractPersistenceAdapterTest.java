package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventPersistenceAdapter;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventSerializer;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCreated;
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
import org.springframework.context.annotation.Import;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
		CreditContractPersistenceAdapter.class,
		CreditContractPersistenceMapper.class,
		OutboxEventPersistenceAdapter.class,
		OutboxEventSerializer.class,
		JacksonAutoConfiguration.class
})
class CreditContractPersistenceAdapterTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	private CreditContractRepository repository;

	@Autowired
	private CreditContractJpaRepository jpaRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldPersistContractSnapshotAndInitialStatusHistory() {
		ContractId id = ContractId.generate();
		CreditContract contract = CreditContract.create(
				id,
				"CT-2026-000001",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))));

		CreditContractCreated createdEvent =
				(CreditContractCreated) contract.getDomainEvents().getFirst();

		repository.save(contract);

		CreditContractJpaEntity persisted = jpaRepository.findById(id.value()).orElseThrow();
		assertEquals("52998224725", persisted.getClientDocumentNumber());
		assertEquals("Maria Silva", persisted.getClientName());
		assertEquals("80010000", persisted.getClientZipCode());
		assertNull(persisted.getCreditLimit());
		assertEquals(ContractStatus.DRAFT, persisted.getStatus());
		assertEquals(0L, persisted.getVersion());

		Map<String, Object> statusHistory = jdbcTemplate.queryForMap(
				"SELECT previous_status, new_status FROM contract_status_history WHERE contract_id = ?",
				id.value());
		assertNull(statusHistory.get("previous_status"));
		assertEquals(ContractStatus.DRAFT.name(), statusHistory.get("new_status"));

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?",
				createdEvent.eventId());
		assertEquals(id.value(), outbox.get("aggregate_id"));
		assertEquals("CreditContract", outbox.get("aggregate_type"));
		assertEquals("CreditContractCreated", outbox.get("event_type"));
		assertEquals(1, outbox.get("schema_version"));
		assertEquals(createdEvent.eventId(), outbox.get("correlation_id"));
		assertNull(outbox.get("causation_id"));
		assertEquals("PENDING", outbox.get("publication_status"));
		assertEquals(0, outbox.get("publication_attempts"));
		String payload = outbox.get("payload").toString();
		assertTrue(payload.contains("\"eventId\": \"" + createdEvent.eventId() + "\""));
		assertTrue(payload.contains("\"contractId\": \"" + id.value() + "\""));
		assertTrue(payload.contains("\"contractNumber\": \"CT-2026-000001\""));
		assertTrue(payload.contains("\"clientDocumentNumber\": \"52998224725\""));
		assertFalse(payload.contains("Maria Silva"));
		assertTrue(contract.getDomainEvents().isEmpty());
	}
}
