package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventPersistenceAdapter;
import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventSerializer;
import br.com.creditcontract.application.port.out.CreditContractRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
class CreditContractOutboxAtomicityTest {

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
	void shouldRollbackContractWhenOutboxPersistenceFails() {
		CreditContract contract = sampleContract();
		jdbcTemplate.execute("DROP TABLE outbox_events");

		assertThrows(DataAccessException.class, () -> repository.save(contract));

		assertEquals(0, jpaRepository.count());
		assertEquals(1, contract.getDomainEvents().size());
	}

	private CreditContract sampleContract() {
		return CreditContract.create(
				ContractId.generate(),
				"CT-2026-000099",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address(
								"PR",
								"Curitiba",
								"Rua das Flores",
								"123",
								new ZipCode("80010-000"))));
	}
}
