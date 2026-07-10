package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Fact emitted when a new credit contract is assembled in the domain.
 */
public record CreditContractCreated(
		UUID eventId,
		ContractId aggregateId,
		String contractNumber,
		DocumentNumber clientDocumentNumber,
		LocalDateTime occurredAt,
		UUID correlationId,
		UUID causationId) implements DomainEvent {

	public static final String AGGREGATE_TYPE = "CreditContract";
	public static final String EVENT_TYPE = "CreditContractCreated";
	public static final int SCHEMA_VERSION = 1;

	public CreditContractCreated {
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(aggregateId, "aggregate id is required");
		Objects.requireNonNull(contractNumber, "contract number is required");
		Objects.requireNonNull(clientDocumentNumber, "client document number is required");
		Objects.requireNonNull(occurredAt, "event occurrence date is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
	}

	public static CreditContractCreated initial(
			ContractId aggregateId,
			String contractNumber,
			DocumentNumber clientDocumentNumber,
			LocalDateTime occurredAt) {
		UUID eventId = UUID.randomUUID();
		return new CreditContractCreated(
				eventId,
				aggregateId,
				contractNumber,
				clientDocumentNumber,
				occurredAt,
				eventId,
				null);
	}

	@Override
	public String aggregateType() {
		return AGGREGATE_TYPE;
	}

	@Override
	public String eventType() {
		return EVENT_TYPE;
	}

	@Override
	public int schemaVersion() {
		return SCHEMA_VERSION;
	}
}
