package br.com.creditcontract.adapter.out.persistence.outbox;

import br.com.creditcontract.domain.event.DomainEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** Persists domain events in PostgreSQL without exposing outbox details inward. */
@Component
public class OutboxEventPersistenceAdapter {

	private static final String INSERT_SQL = """
			INSERT INTO outbox_events (
			    event_id,
			    aggregate_id,
			    aggregate_type,
			    event_type,
			    payload,
			    schema_version,
			    occurred_at,
			    correlation_id,
			    causation_id,
			    publication_status,
			    publication_attempts
			) VALUES (?, ?, ?, ?, CAST(? AS JSONB), ?, ?, ?, ?, 'PENDING', 0)
			""";

	private final JdbcTemplate jdbcTemplate;
	private final OutboxEventSerializer serializer;

	public OutboxEventPersistenceAdapter(
			JdbcTemplate jdbcTemplate,
			OutboxEventSerializer serializer) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
		this.serializer = Objects.requireNonNull(serializer);
	}

	public void persist(List<DomainEvent> events) {
		for (DomainEvent event : events) {
			jdbcTemplate.update(
					INSERT_SQL,
					event.eventId(),
					event.aggregateId().value(),
					event.aggregateType(),
					event.eventType(),
					serializer.serialize(event),
					event.schemaVersion(),
					event.occurredAt(),
					event.correlationId(),
					event.causationId());
		}
	}
}
