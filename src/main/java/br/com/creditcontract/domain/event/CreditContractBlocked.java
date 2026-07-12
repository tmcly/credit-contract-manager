package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when an active credit contract is blocked. */
public record CreditContractBlocked(
		UUID eventId,
		ContractId aggregateId,
		String reason,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditContractBlocked";
	public static final int SCHEMA_VERSION = 1;

	public CreditContractBlocked {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(reason, "blocking reason is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("blocking reason cannot be blank");
		}
		if (reason.length() > 255) {
			throw new IllegalArgumentException("blocking reason cannot exceed 255 characters");
		}
	}

	public static CreditContractBlocked create(
			ContractId aggregateId,
			String reason,
			LocalDateTime occurredAt,
			UUID correlationId) {
		return new CreditContractBlocked(
				UUID.randomUUID(), aggregateId, reason, occurredAt, correlationId, null);
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
