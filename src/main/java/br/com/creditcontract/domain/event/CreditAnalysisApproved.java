package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when credit analysis approves a positive contract limit. */
public record CreditAnalysisApproved(
		UUID eventId,
		ContractId aggregateId,
		MonetaryAmount approvedLimit,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditAnalysisApproved";
	public static final int SCHEMA_VERSION = 1;

	public CreditAnalysisApproved {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(approvedLimit, "approved limit is required");
		if (approvedLimit.amount().signum() <= 0) {
			throw new IllegalArgumentException("approved limit must be positive");
		}
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(causationId, "causation id is required");
	}

	public static CreditAnalysisApproved create(
			ContractId aggregateId,
			MonetaryAmount approvedLimit,
			LocalDateTime occurredAt,
			EventContext context) {
		return new CreditAnalysisApproved(
				UUID.randomUUID(), aggregateId, approvedLimit, occurredAt,
				context.correlationId(), context.causationId());
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
