package br.com.creditcontract.adapter.out.persistence.outbox;

import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.CreditContractAccepted;
import br.com.creditcontract.domain.event.CreditContractActivated;
import br.com.creditcontract.domain.event.CreditContractBlocked;
import br.com.creditcontract.domain.event.CreditContractUnblocked;
import br.com.creditcontract.domain.event.CreditAnalysisApproved;
import br.com.creditcontract.domain.event.CreditAnalysisRejected;
import br.com.creditcontract.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** Maps domain events to stable, versioned JSON integration payloads. */
@Component
public class OutboxEventSerializer {

	private final ObjectMapper objectMapper;

	public OutboxEventSerializer(ObjectMapper objectMapper) {
		this.objectMapper = Objects.requireNonNull(objectMapper);
	}

	String serialize(DomainEvent event) {
		if (event instanceof CreditContractCreated created) {
			return serialize(created);
		}
		if (event instanceof CreditAnalysisApproved approved) {
			return serialize(approved);
		}
		if (event instanceof CreditAnalysisRejected rejected) {
			return serialize(rejected);
		}
		if (event instanceof CreditContractAccepted accepted) {
			return write(accepted, commonPayload(accepted));
		}
		if (event instanceof CreditContractActivated activated) {
			return write(activated, commonPayload(activated));
		}
		if (event instanceof CreditContractBlocked blocked) {
			ObjectNode payload = commonPayload(blocked);
			payload.put("reason", blocked.reason());
			return write(blocked, payload);
		}
		if (event instanceof CreditContractUnblocked unblocked) {
			ObjectNode payload = commonPayload(unblocked);
			payload.put("reason", unblocked.reason());
			return write(unblocked, payload);
		}
		throw new IllegalArgumentException("unsupported domain event: " + event.eventType());
	}

	private String serialize(CreditContractCreated event) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("eventId", event.eventId().toString());
		payload.put("contractId", event.aggregateId().value().toString());
		payload.put("contractNumber", event.contractNumber());
		payload.put("clientDocumentNumber", event.clientDocumentNumber().value());
		payload.put("occurredAt", event.occurredAt().toString());

		return write(event, payload);
	}

	private String serialize(CreditAnalysisApproved event) {
		ObjectNode payload = commonPayload(event);
		payload.put("approvedLimit", event.approvedLimit().amount().toPlainString());
		return write(event, payload);
	}

	private String serialize(CreditAnalysisRejected event) {
		ObjectNode payload = commonPayload(event);
		payload.put("reason", event.reason());
		return write(event, payload);
	}

	private ObjectNode commonPayload(DomainEvent event) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("eventId", event.eventId().toString());
		payload.put("contractId", event.aggregateId().value().toString());
		payload.put("occurredAt", event.occurredAt().toString());
		return payload;
	}

	private String write(DomainEvent event, ObjectNode payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException(
					"could not serialize event " + event.eventId(), exception);
		}
	}
}
