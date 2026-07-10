package br.com.creditcontract.adapter.out.persistence.outbox;

import br.com.creditcontract.application.port.out.EventPublication;
import br.com.creditcontract.application.port.out.EventPublicationResult;
import br.com.creditcontract.application.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Relays committed outbox rows and changes their state only after broker confirmation. */
@Component
public class OutboxRelay {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelay.class);
	private static final String SELECT_PENDING_SQL = """
			SELECT event_id, aggregate_id, aggregate_type, event_type, payload::text,
			       schema_version, occurred_at, correlation_id, causation_id
			FROM outbox_events
			WHERE publication_status = 'PENDING'
			  AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP)
			ORDER BY occurred_at, event_id
			LIMIT ?
			FOR UPDATE SKIP LOCKED
			""";

	private final JdbcTemplate jdbcTemplate;
	private final EventPublisher eventPublisher;
	private final int batchSize;
	private final Duration retryDelay;

	public OutboxRelay(
			JdbcTemplate jdbcTemplate,
			EventPublisher eventPublisher,
			@Value("${credit-contract.outbox.batch-size}") int batchSize,
			@Value("${credit-contract.outbox.retry-delay}") Duration retryDelay) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
		this.eventPublisher = Objects.requireNonNull(eventPublisher);
		this.retryDelay = Objects.requireNonNull(retryDelay);
		if (batchSize <= 0) {
			throw new IllegalArgumentException("outbox batch size must be positive");
		}
		this.batchSize = batchSize;
	}

	@Scheduled(
			initialDelayString = "${credit-contract.outbox.initial-delay}",
			fixedDelayString = "${credit-contract.outbox.fixed-delay}")
	@Transactional
	public void publishPending() {
		List<EventPublication> events = jdbcTemplate.query(
				SELECT_PENDING_SQL,
				(resultSet, rowNumber) -> new EventPublication(
						resultSet.getObject("event_id", UUID.class),
						resultSet.getObject("aggregate_id", UUID.class),
						resultSet.getString("aggregate_type"),
						resultSet.getString("event_type"),
						resultSet.getInt("schema_version"),
						resultSet.getTimestamp("occurred_at").toLocalDateTime(),
						resultSet.getObject("correlation_id", UUID.class),
						resultSet.getObject("causation_id", UUID.class),
						resultSet.getString("payload")),
				batchSize);

		for (EventPublication event : events) {
			EventPublicationResult result = eventPublisher.publish(event);
			if (result.confirmed()) {
				markPublished(event.eventId());
				LOGGER.info("Published outbox event {} of type {}", event.eventId(), event.eventType());
			} else {
				markForRetry(event.eventId(), result.failureReason());
				LOGGER.warn("Could not publish outbox event {}: {}", event.eventId(), result.failureReason());
			}
		}
	}

	private void markPublished(UUID eventId) {
		jdbcTemplate.update("""
				UPDATE outbox_events
				SET publication_status = 'PUBLISHED', published_at = CURRENT_TIMESTAMP,
				    last_error = NULL, next_attempt_at = NULL
				WHERE event_id = ? AND publication_status = 'PENDING'
				""", eventId);
	}

	private void markForRetry(UUID eventId, String failureReason) {
		String error = failureReason == null ? "broker did not confirm publication" : failureReason;
		if (error.length() > 1000) {
			error = error.substring(0, 1000);
		}
		jdbcTemplate.update("""
				UPDATE outbox_events
				SET publication_attempts = publication_attempts + 1,
				    next_attempt_at = CURRENT_TIMESTAMP + (? * INTERVAL '1 millisecond'),
				    last_error = ?
				WHERE event_id = ? AND publication_status = 'PENDING'
				""", retryDelay.toMillis(), error, eventId);
	}
}
