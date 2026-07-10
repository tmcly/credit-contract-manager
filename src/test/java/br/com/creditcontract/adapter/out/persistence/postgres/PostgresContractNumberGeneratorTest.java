package br.com.creditcontract.adapter.out.persistence.postgres;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContractNumberGenerator.class)
class PostgresContractNumberGeneratorTest {

	private static final int CONCURRENT_CALLS = 24;

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PostgresContractNumberGenerator generator;

	@Test
	void shouldKeepGeneratingUniqueNumbersAcrossGeneratorInstances() {
		PostgresContractNumberGenerator firstInstance =
				new PostgresContractNumberGenerator(jdbcTemplate);
		PostgresContractNumberGenerator restartedInstance =
				new PostgresContractNumberGenerator(jdbcTemplate);

		String firstNumber = firstInstance.next();
		String numberAfterRestart = restartedInstance.next();

		assertTrue(firstNumber.matches("CT-\\d{4}-\\d{6,}"));
		assertTrue(numberAfterRestart.matches("CT-\\d{4}-\\d{6,}"));
		assertNotEquals(firstNumber, numberAfterRestart);
	}

	@Test
	void shouldGenerateDistinctNumbersConcurrently() throws Exception {
		List<Callable<String>> calls = new ArrayList<>();
		for (int index = 0; index < CONCURRENT_CALLS; index++) {
			calls.add(generator::next);
		}

		List<String> generatedNumbers = new ArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
			for (var result : executor.invokeAll(calls)) {
				generatedNumbers.add(result.get());
			}
		}

		assertEquals(CONCURRENT_CALLS, generatedNumbers.size());
		assertEquals(CONCURRENT_CALLS, new HashSet<>(generatedNumbers).size());
		assertTrue(generatedNumbers.stream()
				.allMatch(number -> number.matches("CT-\\d{4}-\\d{6,}")));
	}
}
