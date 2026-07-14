package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventPersistenceAdapter;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventSerializer;
import br.com.creditcontract.application.exception.ConcurrentCreditContractUpdateException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractAccepted;
import br.com.creditcontract.domain.event.CreditContractBlocked;
import br.com.creditcontract.domain.event.CreditContractCancelled;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.CreditContractUnblocked;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

	@Autowired
	private PlatformTransactionManager transactionManager;

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

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldPersistAcceptanceAndItsOutboxEventAtomically() {
		ContractId id = ContractId.generate();
		LocalDateTime now = LocalDateTime.now();
		CreditContract contract = CreditContract.rehydrate(
				id,
				"CT-2026-000302",
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
		repository.save(contract);

		UUID correlationId = UUID.randomUUID();
		contract.accept(correlationId);
		CreditContractAccepted event = (CreditContractAccepted) contract.getDomainEvents().getFirst();
		repository.save(contract);

		CreditContract persisted = repository.findById(id).orElseThrow();
		assertEquals(ContractStatus.ACCEPTED, persisted.getStatus());
		assertEquals(ContractStatus.APPROVED,
				persisted.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.ACCEPTED,
				persisted.getStatusHistory().getLast().newStatus());

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?", event.eventId());
		assertEquals("CreditContractAccepted", outbox.get("event_type"));
		assertEquals(correlationId, outbox.get("correlation_id"));
		assertNull(outbox.get("causation_id"));
		assertTrue(outbox.get("payload").toString()
				.contains("\"contractId\": \"" + id.value() + "\""));
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldPersistBlockingReasonAndItsOutboxEventAtomically() {
		ContractId id = ContractId.generate();
		LocalDateTime now = LocalDateTime.now();
		CreditContract contract = CreditContract.rehydrate(
				id,
				"CT-2026-000502",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.ACTIVE,
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				now,
				now,
				4L,
				List.of());
		repository.save(contract);

		UUID correlationId = UUID.randomUUID();
		contract.block("Payment overdue for more than 30 days", correlationId);
		CreditContractBlocked event =
				(CreditContractBlocked) contract.getDomainEvents().getFirst();
		repository.save(contract);

		CreditContract persisted = repository.findById(id).orElseThrow();
		assertEquals(ContractStatus.BLOCKED, persisted.getStatus());
		assertEquals("Payment overdue for more than 30 days",
				persisted.getStatusHistory().getLast().reason());

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?", event.eventId());
		assertEquals("CreditContractBlocked", outbox.get("event_type"));
		assertEquals(correlationId, outbox.get("correlation_id"));
		assertNull(outbox.get("causation_id"));
		assertTrue(outbox.get("payload").toString()
				.contains("\"reason\": \"Payment overdue for more than 30 days\""));
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldPersistUnblockingReasonAndItsOutboxEventAtomically() {
		ContractId id = ContractId.generate();
		LocalDateTime now = LocalDateTime.now();
		CreditContract contract = CreditContract.rehydrate(
				id,
				"CT-2026-000602",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.BLOCKED,
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				now,
				now,
				5L,
				List.of());
		repository.save(contract);

		UUID correlationId = UUID.randomUUID();
		contract.unblock("Outstanding balance settled", correlationId);
		CreditContractUnblocked event =
				(CreditContractUnblocked) contract.getDomainEvents().getFirst();
		repository.save(contract);

		CreditContract persisted = repository.findById(id).orElseThrow();
		assertEquals(ContractStatus.ACTIVE, persisted.getStatus());
		assertEquals(ContractStatus.BLOCKED,
				persisted.getStatusHistory().getLast().previousStatus());
		assertEquals("Outstanding balance settled",
				persisted.getStatusHistory().getLast().reason());

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?", event.eventId());
		assertEquals("CreditContractUnblocked", outbox.get("event_type"));
		assertEquals(correlationId, outbox.get("correlation_id"));
		assertNull(outbox.get("causation_id"));
		assertTrue(outbox.get("payload").toString()
				.contains("\"reason\": \"Outstanding balance settled\""));
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldFindExpiredBlockedContractsAndPersistCancellationEvent() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
		CreditContract expired = persistedContract(
				"CT-2026-000703", ContractStatus.BLOCKED, cutoff.minusMinutes(1));
		CreditContract recent = persistedContract(
				"CT-2026-000704", ContractStatus.BLOCKED, cutoff.plusMinutes(1));
		repository.save(expired);
		repository.save(recent);

		List<CreditContract> found = repository.findBlockedUpdatedBefore(cutoff, 10);
		assertEquals(List.of(expired.getId()), found.stream().map(CreditContract::getId).toList());

		UUID correlationId = UUID.randomUUID();
		CreditContract selected = found.getFirst();
		selected.cancelAfterBlockedExpiration("Regularization period elapsed", correlationId);
		CreditContractCancelled event =
				(CreditContractCancelled) selected.getDomainEvents().getFirst();
		repository.save(selected);

		Map<String, Object> outbox = jdbcTemplate.queryForMap(
				"SELECT * FROM outbox_events WHERE event_id = ?", event.eventId());
		assertEquals("CreditContractCancelled", outbox.get("event_type"));
		assertTrue(outbox.get("payload").toString().contains("BLOCKED_EXPIRATION"));
		assertEquals(ContractStatus.BLOCKED, repository.findById(recent.getId()).orElseThrow().getStatus());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldRejectOneOfTwoConcurrentContractUpdatesWithoutPartialWrites() throws Exception {
		CreditContract contract = persistedContract(
				"CT-2026-000803", ContractStatus.ACTIVE, LocalDateTime.now());
		repository.save(contract);

		CountDownLatch bothLoaded = new CountDownLatch(2);
		CountDownLatch startUpdates = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<RuntimeException> blocking = executor.submit(() -> runConcurrentUpdate(
					contract.getId(), bothLoaded, startUpdates,
					loaded -> loaded.block("Payment overdue", UUID.fromString(
							"10000000-0000-0000-0000-000000000001"))));
			Future<RuntimeException> cancellation = executor.submit(() -> runConcurrentUpdate(
					contract.getId(), bothLoaded, startUpdates,
					loaded -> loaded.cancelForLegalReason("Legal cancellation", UUID.fromString(
							"20000000-0000-0000-0000-000000000002"))));

			assertTrue(bothLoaded.await(10, TimeUnit.SECONDS),
					"both transactions should load the same contract version");
			startUpdates.countDown();

			RuntimeException blockingFailure = blocking.get(20, TimeUnit.SECONDS);
			RuntimeException cancellationFailure = cancellation.get(20, TimeUnit.SECONDS);
			List<RuntimeException> failures = Stream.of(blockingFailure, cancellationFailure)
					.filter(Objects::nonNull)
					.toList();

			assertEquals(1, failures.size());
			assertInstanceOf(ConcurrentCreditContractUpdateException.class, failures.getFirst());
		} finally {
			startUpdates.countDown();
			executor.shutdownNow();
		}

		CreditContract persisted = repository.findById(contract.getId()).orElseThrow();
		assertTrue(persisted.getStatus() == ContractStatus.BLOCKED
				|| persisted.getStatus() == ContractStatus.CANCELLED);
		assertEquals(1, jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM contract_status_history
				WHERE contract_id = ? AND previous_status IS NOT NULL
				""",
				Integer.class, contract.getId().value()));
		assertEquals(1, jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = ?",
				Integer.class, contract.getId().value()));
	}

	private RuntimeException runConcurrentUpdate(
			ContractId contractId,
			CountDownLatch bothLoaded,
			CountDownLatch startUpdates,
			Consumer<CreditContract> update) {
		TransactionTemplate transactions = new TransactionTemplate(transactionManager);
		try {
			transactions.executeWithoutResult(status -> {
				CreditContract loaded = repository.findById(contractId).orElseThrow();
				bothLoaded.countDown();
				await(startUpdates);
				update.accept(loaded);
				repository.save(loaded);
			});
			return null;
		} catch (RuntimeException exception) {
			return exception;
		}
	}

	private void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("timed out waiting to start concurrent updates");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while waiting to start concurrent updates", exception);
		}
	}

	private CreditContract persistedContract(
			String number, ContractStatus status, LocalDateTime updatedAt) {
		return CreditContract.rehydrate(ContractId.generate(), number,
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				status, MonetaryAmount.reais(new BigDecimal("5000.00")),
				updatedAt.minusDays(1), updatedAt, 6L, List.of());
	}
}
