package br.com.creditcontract.adapter.out.persistence.postgres;

import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Generates public contract numbers from a PostgreSQL sequence.
 *
 * <p>The sequence provides concurrency safety and survives application restarts.
 * Its values are intentionally allowed to have gaps because PostgreSQL sequences
 * are not rolled back with the transaction that requested a value.
 */
@Component
public class PostgresContractNumberGenerator implements ContractNumberGenerator {

	private static final String NEXT_VALUE_SQL =
			"SELECT nextval('credit_contract_number_seq')";
	private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("America/Sao_Paulo");

	private final JdbcTemplate jdbcTemplate;

	public PostgresContractNumberGenerator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
	}

	@Override
	public String next() {
		Long sequenceValue = Objects.requireNonNull(
				jdbcTemplate.queryForObject(NEXT_VALUE_SQL, Long.class),
				"PostgreSQL sequence returned no value");

		return "CT-%d-%06d".formatted(
				Year.now(BUSINESS_TIME_ZONE).getValue(),
				sequenceValue);
	}
}
