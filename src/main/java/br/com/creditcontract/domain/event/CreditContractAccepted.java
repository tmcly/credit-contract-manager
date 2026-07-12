package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when the client accepts an approved credit contract. */
public record CreditContractAccepted(
		UUID eventId,
		ContractId aggregateId,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditContractAccepted";
	public static final int SCHEMA_VERSION = 1;

	public CreditContractAccepted {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
	}

	public static CreditContractAccepted create(
			ContractId aggregateId,
			LocalDateTime occurredAt,
			UUID correlationId) {
		return new CreditContractAccepted(
				UUID.randomUUID(), aggregateId, occurredAt, correlationId, null);
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
