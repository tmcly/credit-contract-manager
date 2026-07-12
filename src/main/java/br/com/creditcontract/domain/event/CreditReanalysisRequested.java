package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when an active client requests a new credit assessment. */
public record CreditReanalysisRequested(
		UUID eventId,
		ContractId aggregateId,
		MonetaryAmount currentLimit,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditReanalysisRequested";
	public static final int SCHEMA_VERSION = 1;

	public CreditReanalysisRequested {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(currentLimit, "current limit is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (causationId != null) {
			throw new IllegalArgumentException("client-requested reanalysis has no causation event");
		}
	}

	public static CreditReanalysisRequested create(
			UUID requestId,
			ContractId aggregateId,
			MonetaryAmount currentLimit,
			LocalDateTime occurredAt,
			UUID correlationId) {
		return new CreditReanalysisRequested(
				requestId, aggregateId, currentLimit, occurredAt, correlationId, null);
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
