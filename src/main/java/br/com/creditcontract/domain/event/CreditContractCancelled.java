package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.enums.CancellationOrigin;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when a credit contract is permanently cancelled. */
public record CreditContractCancelled(
		UUID eventId,
		ContractId aggregateId,
		ContractStatus previousStatus,
		CancellationOrigin origin,
		String reason,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditContractCancelled";
	public static final int SCHEMA_VERSION = 1;

	public CreditContractCancelled {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(previousStatus, "previous status is required");
		Objects.requireNonNull(origin, "cancellation origin is required");
		Objects.requireNonNull(reason, "cancellation reason is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("cancellation reason cannot be blank");
		}
		if (reason.length() > 255) {
			throw new IllegalArgumentException("cancellation reason cannot exceed 255 characters");
		}
	}

	public static CreditContractCancelled create(
			ContractId aggregateId,
			ContractStatus previousStatus,
			CancellationOrigin origin,
			String reason,
			LocalDateTime occurredAt,
			UUID correlationId) {
		return new CreditContractCancelled(UUID.randomUUID(), aggregateId, previousStatus,
				origin, reason, occurredAt, correlationId, null);
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
