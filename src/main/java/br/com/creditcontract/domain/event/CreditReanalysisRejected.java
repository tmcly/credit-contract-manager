package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Fact emitted when reanalysis retains the existing contract limit. */
public record CreditReanalysisRejected(
		UUID eventId,
		ContractId aggregateId,
		UUID reanalysisId,
		MonetaryAmount previousLimit,
		MonetaryAmount retainedLimit,
		String reason,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditReanalysisRejected";
	public static final int SCHEMA_VERSION = 1;

	public CreditReanalysisRejected {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(reanalysisId, "reanalysis id is required");
		Objects.requireNonNull(previousLimit, "previous limit is required");
		Objects.requireNonNull(retainedLimit, "retained limit is required");
		Objects.requireNonNull(reason, "rejection reason is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("rejection reason cannot be blank");
		}
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(causationId, "causation id is required");
	}

	public static CreditReanalysisRejected create(
			ContractId aggregateId,
			UUID reanalysisId,
			MonetaryAmount previousLimit,
			MonetaryAmount retainedLimit,
			String reason,
			LocalDateTime occurredAt,
			EventContext context) {
		return new CreditReanalysisRejected(
				UUID.randomUUID(), aggregateId, reanalysisId, previousLimit, retainedLimit,
				reason, occurredAt, context.correlationId(), context.causationId());
	}

	@Override public String aggregateType() { return AGGREGATE_TYPE; }
	@Override public String eventType() { return EVENT_TYPE; }
	@Override public int schemaVersion() { return SCHEMA_VERSION; }
}
