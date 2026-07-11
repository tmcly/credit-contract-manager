package br.com.creditcontract.domain.event;

import java.util.Objects;
import java.util.UUID;

/** Trace metadata inherited by an event caused by another event. */
public record EventContext(UUID correlationId, UUID causationId) {

	public EventContext {
		Objects.requireNonNull(correlationId, "correlation id is required");
		Objects.requireNonNull(causationId, "causation id is required");
	}
}
