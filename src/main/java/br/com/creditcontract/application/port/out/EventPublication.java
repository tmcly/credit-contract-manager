package br.com.creditcontract.application.port.out;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Transport-neutral event data required by an outbound message publisher. */
public record EventPublication(
		UUID eventId,
		UUID aggregateId,
		String aggregateType,
		String eventType,
		int schemaVersion,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId,
		String payload) {

	public EventPublication {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(aggregateType, "aggregate type is required");
		Objects.requireNonNull(eventType, "event type is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(payload, "event payload is required");
		if (schemaVersion <= 0) {
			throw new IllegalArgumentException("schema version must be positive");
		}
	}
}
