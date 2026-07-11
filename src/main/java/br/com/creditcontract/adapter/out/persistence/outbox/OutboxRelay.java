package br.com.creditcontract.adapter.out.persistence.outbox;

import br.com.creditcontract.application.port.out.EventPublication;
import br.com.creditcontract.application.port.out.EventPublicationResult;
import br.com.creditcontract.application.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
	private final Duration retryInitialDelay;
	private final Duration retryMaxDelay;
	private final int maxAttempts;
	private final Counter publishedCounter;
	private final Counter failureCounter;
	private final Timer publishLatency;

	public OutboxRelay(
			JdbcTemplate jdbcTemplate,
			EventPublisher eventPublisher,
			@Value("${credit-contract.outbox.batch-size}") int batchSize,
			@Value("${credit-contract.outbox.retry-initial-delay}") Duration retryInitialDelay,
			@Value("${credit-contract.outbox.retry-max-delay}") Duration retryMaxDelay,
			@Value("${credit-contract.outbox.max-attempts}") int maxAttempts,
			MeterRegistry meterRegistry) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
		this.eventPublisher = Objects.requireNonNull(eventPublisher);
		this.retryInitialDelay = Objects.requireNonNull(retryInitialDelay);
		this.retryMaxDelay = Objects.requireNonNull(retryMaxDelay);
		if (batchSize <= 0) {
			throw new IllegalArgumentException("outbox batch size must be positive");
		}
		if (maxAttempts <= 0) {
			throw new IllegalArgumentException("outbox max attempts must be positive");
		}
		this.batchSize = batchSize;
		this.maxAttempts = maxAttempts;
		this.publishedCounter = meterRegistry.counter("credit_contract.outbox.published");
		this.failureCounter = meterRegistry.counter("credit_contract.outbox.publish.failures");
		this.publishLatency = meterRegistry.timer("credit_contract.outbox.publish.latency");
		Gauge.builder("credit_contract.outbox.pending", this, OutboxRelay::pendingCount)
				.description("Number of outbox events eligible or waiting for retry")
				.register(meterRegistry);
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
			EventPublicationResult result = publishLatency.record(() -> eventPublisher.publish(event));
			if (result.confirmed()) {
				markPublished(event.eventId());
				publishedCounter.increment();
				LOGGER.atInfo()
						.addKeyValue("event", "outbox_published")
						.addKeyValue("eventId", event.eventId())
						.addKeyValue("eventType", event.eventType())
						.addKeyValue("correlationId", event.correlationId())
						.addKeyValue("causationId", event.causationId())
						.log("Outbox event published");
			} else {
				markForRetry(event.eventId(), result.failureReason());
				failureCounter.increment();
				LOGGER.atWarn()
						.addKeyValue("event", "outbox_publish_failed")
						.addKeyValue("eventId", event.eventId())
						.addKeyValue("eventType", event.eventType())
						.addKeyValue("correlationId", event.correlationId())
						.log("Outbox event publication failed");
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
				    publication_status = CASE
				        WHEN publication_attempts + 1 >= ? THEN 'FAILED'
				        ELSE 'PENDING'
				    END,
				    next_attempt_at = CASE
				        WHEN publication_attempts + 1 >= ? THEN NULL
				        ELSE CURRENT_TIMESTAMP + (
				            LEAST(? * POWER(2, publication_attempts), ?) * INTERVAL '1 millisecond'
				        )
				    END,
				    last_error = ?
				WHERE event_id = ? AND publication_status = 'PENDING'
				""", maxAttempts, maxAttempts, retryInitialDelay.toMillis(),
				retryMaxDelay.toMillis(), error, eventId);
	}

	private double pendingCount() {
		Long count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE publication_status = 'PENDING'",
				Long.class);
		return count == null ? 0 : count.doubleValue();
	}
}
