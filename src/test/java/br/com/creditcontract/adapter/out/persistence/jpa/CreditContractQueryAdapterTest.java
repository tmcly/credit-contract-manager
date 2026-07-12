package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.application.port.out.CreditContractQueryPort;
import br.com.creditcontract.application.query.ContractSortField;
import br.com.creditcontract.application.query.CreditContractSearchCriteria;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.SortDirection;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CreditContractQueryAdapter.class)
class CreditContractQueryAdapterTest {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired private CreditContractQueryPort queryPort;
	@Autowired private CreditContractJpaRepository contractRepository;
	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void shouldFilterContractsAndPaginateWithStableOrdering() {
		LocalDateTime base = LocalDateTime.of(2026, 7, 1, 10, 0);
		CreditContractJpaEntity first = contract("CT-2026-000001", "52998224725",
				ContractStatus.ACTIVE, base, base.plusHours(1));
		CreditContractJpaEntity second = contract("CT-2026-000002", "52998224725",
				ContractStatus.ACTIVE, base.plusDays(1), base.plusDays(1));
		contractRepository.save(first);
		contractRepository.save(second);
		contractRepository.save(contract("CT-2026-000003", "16899535009",
				ContractStatus.BLOCKED, base.plusDays(2), base.plusDays(2)));
		contractRepository.flush();

		var filtered = queryPort.search(
				new CreditContractSearchCriteria(
						ContractStatus.ACTIVE, DocumentNumber.from("52998224725"), null),
				new PageQuery(0, 1), ContractSortField.CREATED_AT, SortDirection.DESC);

		assertEquals(2, filtered.totalElements());
		assertEquals(2, filtered.totalPages());
		assertEquals(second.getId(), filtered.content().getFirst().id().value());
		assertTrue(filtered.first());
		assertFalse(filtered.last());

		var exactContract = queryPort.search(
				new CreditContractSearchCriteria(null, null, "CT-2026-000001"),
				new PageQuery(0, 20), ContractSortField.UPDATED_AT, SortDirection.ASC);
		assertEquals(1, exactContract.totalElements());
		assertEquals(first.getId(), exactContract.content().getFirst().id().value());
		assertEquals("Maria Silva", exactContract.content().getFirst().clientName());
	}

	@Test
	void shouldReadStatusAndReanalysisHistoriesNewestFirst() {
		LocalDateTime base = LocalDateTime.of(2026, 7, 1, 10, 0);
		CreditContractJpaEntity contract = contract("CT-2026-000010", "52998224725",
				ContractStatus.ACTIVE, base, base.plusDays(2));
		UUID oldHistoryId = UUID.randomUUID();
		UUID newHistoryId = UUID.randomUUID();
		contract.addStatusHistory(new ContractStatusHistoryJpaEntity(
				oldHistoryId, null, ContractStatus.DRAFT, null, base));
		contract.addStatusHistory(new ContractStatusHistoryJpaEntity(
				newHistoryId, ContractStatus.ACCEPTED, ContractStatus.ACTIVE,
				"Activation completed", base.plusDays(2)));
		UUID oldReanalysisId = UUID.randomUUID();
		UUID newReanalysisId = UUID.randomUUID();
		contract.addCreditReanalysis(new CreditReanalysisJpaEntity(
				oldReanalysisId, CreditReanalysisStatus.REJECTED,
				new BigDecimal("5000.00"), new BigDecimal("5000.00"),
				"Policy denied", base, base.plusSeconds(1)));
		contract.addCreditReanalysis(new CreditReanalysisJpaEntity(
				newReanalysisId, CreditReanalysisStatus.APPROVED,
				new BigDecimal("5000.00"), new BigDecimal("7500.00"), null,
				base.plusDays(1), base.plusDays(1).plusSeconds(1)));
		contractRepository.saveAndFlush(contract);

		ContractId id = ContractId.from(contract.getId());
		var history = queryPort.findStatusHistory(id, new PageQuery(0, 1));
		assertEquals(2, history.totalElements());
		assertEquals(newHistoryId, history.content().getFirst().id());
		assertEquals("Activation completed", history.content().getFirst().reason());

		var reanalyses = queryPort.findCreditReanalyses(id, new PageQuery(0, 10));
		assertEquals(2, reanalyses.totalElements());
		assertEquals(newReanalysisId, reanalyses.content().getFirst().id());
		assertEquals(new BigDecimal("7500.00"), reanalyses.content().getFirst().newLimit());
		assertTrue(queryPort.existsById(id));
		assertFalse(queryPort.existsById(ContractId.generate()));
	}

	@Test
	void shouldCreateIndexesUsedByContractReadQueries() {
		Integer count = jdbcTemplate.queryForObject("""
				SELECT count(*) FROM pg_indexes
				WHERE tablename = 'credit_contracts'
				  AND indexname IN (
				    'idx_credit_contracts_created_at_id',
				    'idx_credit_contracts_updated_at_id',
				    'idx_credit_contracts_status_created_at_id')
				""", Integer.class);
		assertEquals(3, count);
	}

	private CreditContractJpaEntity contract(
			String number,
			String documentNumber,
			ContractStatus status,
			LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		return new CreditContractJpaEntity(
				UUID.randomUUID(), number, documentNumber, "Maria Silva", "PR", "Curitiba",
				"Rua das Flores", "123", "80010000", status,
				new BigDecimal("5000.00"), createdAt, updatedAt);
	}
}
