package br.com.creditcontract.adapter.out.persistence.postgres;

import br.com.creditcontract.application.port.out.ProcessedMessageStore;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/** PostgreSQL inbox used by consumers to make at-least-once delivery idempotent. */
@Component
public class PostgresProcessedMessageStore implements ProcessedMessageStore {

	private final JdbcTemplate jdbcTemplate;

	public PostgresProcessedMessageStore(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
	}

	@Override
	public boolean contains(UUID eventId) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM processed_messages WHERE event_id = ?",
				Integer.class,
				eventId);
		return count != null && count > 0;
	}

	@Override
	public void record(UUID eventId, String consumerName, ContractId aggregateId, UUID correlationId) {
		jdbcTemplate.update("""
				INSERT INTO processed_messages (
				    event_id, consumer_name, aggregate_id, correlation_id
				) VALUES (?, ?, ?, ?)
				ON CONFLICT (event_id) DO NOTHING
				""", eventId, consumerName, aggregateId.value(), correlationId);
	}
}
