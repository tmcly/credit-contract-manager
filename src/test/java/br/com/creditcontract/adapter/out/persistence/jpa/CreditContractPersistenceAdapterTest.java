package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({CreditContractPersistenceAdapter.class, CreditContractPersistenceMapper.class})
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

	@Test
	void shouldPersistContractSnapshotAndInitialStatusHistory() {
		ContractId id = ContractId.generate();
		CreditContract contract = CreditContract.create(
				id,
				"CT-2026-000001",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				MonetaryAmount.reais(new BigDecimal("5000.00")));

		repository.save(contract);
		jpaRepository.flush();

		CreditContractJpaEntity persisted = jpaRepository.findById(id.value()).orElseThrow();
		assertEquals("52998224725", persisted.getClientDocumentNumber());
		assertEquals("Maria Silva", persisted.getClientName());
		assertEquals("80010000", persisted.getClientZipCode());
		assertEquals(new BigDecimal("5000.00"), persisted.getCreditLimit());
		assertEquals(ContractStatus.DRAFT, persisted.getStatus());
		assertEquals(0L, persisted.getVersion());
		assertEquals(1, persisted.getStatusHistory().size());
		assertNull(persisted.getStatusHistory().getFirst().getPreviousStatus());
		assertEquals(ContractStatus.DRAFT, persisted.getStatusHistory().getFirst().getNewStatus());
	}
}
