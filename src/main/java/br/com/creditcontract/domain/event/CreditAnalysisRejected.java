package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when credit analysis rejects a credit contract. */
public record CreditAnalysisRejected(
		UUID eventId,
		ContractId aggregateId,
		String reason,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditAnalysisRejected";
	public static final int SCHEMA_VERSION = 1;

	public CreditAnalysisRejected {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(reason, "rejection reason is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("rejection reason cannot be blank");
		}
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(causationId, "causation id is required");
	}

	public static CreditAnalysisRejected create(
			ContractId aggregateId,
			String reason,
			LocalDateTime occurredAt,
			EventContext context) {
		return new CreditAnalysisRejected(
				UUID.randomUUID(), aggregateId, reason, occurredAt,
				context.correlationId(), context.causationId());
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
