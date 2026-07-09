package br.com.creditcontract.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Reference to a client that lives in ANOTHER bounded context (the client
 * management system). This contract app does NOT own client data.
 *
 * In the original system, contracts and clients were separate applications
 * talking over an API. So the contract only carries the client's identity
 * ({@code ClientId}), never the client entity itself. The readable data
 * (name, address) arrives as a {@link Client} snapshot hydrated through
 * {@code ClientDataProvider}.
 *
 * This VO is what makes the bounded-context boundary explicit: it is a
 * pointer, not an aggregate.
 */
public record ClientId(UUID value) {

	public ClientId {
		if (value == null) {
			throw new IllegalArgumentException("ClientId cannot be null");
		}
	}

	public static ClientId generate() {
		return new ClientId(UUID.randomUUID());
	}

	public static ClientId from(UUID value) {
		return new ClientId(value);
	}

	public String asString() {
		return value.toString();
	}
}
