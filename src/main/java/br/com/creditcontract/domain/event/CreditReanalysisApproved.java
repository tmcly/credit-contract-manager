package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when reanalysis increases an active contract's limit. */
public record CreditReanalysisApproved(
		UUID eventId,
		ContractId aggregateId,
		UUID reanalysisId,
		MonetaryAmount previousLimit,
		MonetaryAmount newLimit,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditReanalysisApproved";
	public static final int SCHEMA_VERSION = 1;

	public CreditReanalysisApproved {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(reanalysisId, "reanalysis id is required");
		Objects.requireNonNull(previousLimit, "previous limit is required");
		Objects.requireNonNull(newLimit, "new limit is required");
		if (newLimit.amount().compareTo(previousLimit.amount()) <= 0) {
			throw new IllegalArgumentException("new limit must be greater than previous limit");
		}
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(causationId, "causation id is required");
	}

	public static CreditReanalysisApproved create(
			ContractId aggregateId,
			UUID reanalysisId,
			MonetaryAmount previousLimit,
			MonetaryAmount newLimit,
			LocalDateTime occurredAt,
			EventContext context) {
		return new CreditReanalysisApproved(
				UUID.randomUUID(), aggregateId, reanalysisId, previousLimit, newLimit,
				occurredAt, context.correlationId(), context.causationId());
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
