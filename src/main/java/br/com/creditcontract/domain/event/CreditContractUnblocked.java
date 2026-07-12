package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when a blocked credit contract is returned to active status. */
public record CreditContractUnblocked(
		UUID eventId,
		ContractId aggregateId,
		String reason,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditContractUnblocked";
	public static final int SCHEMA_VERSION = 1;

	public CreditContractUnblocked {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(reason, "unblocking reason is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("unblocking reason cannot be blank");
		}
		if (reason.length() > 255) {
			throw new IllegalArgumentException("unblocking reason cannot exceed 255 characters");
		}
	}

	public static CreditContractUnblocked create(
			ContractId aggregateId,
			String reason,
			LocalDateTime occurredAt,
			UUID correlationId) {
		return new CreditContractUnblocked(
				UUID.randomUUID(), aggregateId, reason, occurredAt, correlationId, null);
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
