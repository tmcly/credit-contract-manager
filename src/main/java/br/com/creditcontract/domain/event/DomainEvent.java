package br.com.creditcontract.domain.event;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Business fact recorded by an aggregate for later durable publication.
 *
 * <p>Domain events carry stable identity and tracing metadata, but no transport,
 * serialization, broker or persistence concern.
 */
public interface DomainEvent {

	UUID eventId();

	ContractId aggregateId();

	String aggregateType();

	String eventType();

	int schemaVersion();

	LocalDateTime occurredAt();

	UUID correlationId();

	UUID causationId();
}
