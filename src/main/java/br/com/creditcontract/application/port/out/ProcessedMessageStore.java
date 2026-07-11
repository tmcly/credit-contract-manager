package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.util.UUID;

/** Records successfully handled inbound messages for durable idempotency. */
public interface ProcessedMessageStore {

	boolean contains(UUID eventId);

	void record(UUID eventId, String consumerName, ContractId aggregateId, UUID correlationId);
}
