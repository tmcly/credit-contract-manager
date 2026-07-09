package br.com.creditcontract.infrastructure.client;

import br.com.creditcontract.domain.port.ClientDataProvider;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ClientId;
import br.com.creditcontract.domain.valueobject.ZipCode;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * STUB adapter for {@link ClientDataProvider}.
 *
 * In production this would call the external client-management API over HTTP
 * (RestTemplate / WebClient). For the study project there is no client
 * service to talk to yet, so we return a deterministic fake. The seam is real:
 * swap this for {@code ApiClientDataProvider} once the client system exists.
 *
 * Demonstrates the Anti-Corruption Layer: the contract domain only ever sees
 * the {@link Client} value object, never the foreign model.
 */
@Component
public class StubClientDataProvider implements ClientDataProvider {

	@Override
	public Optional<Client> fetch(ClientId clientId) {
		if (clientId == null) {
			return Optional.empty();
		}
		// Deterministic fake kept stable for tests / demos.
		Client fake = new Client(
				"Maria Silva (from client context)",
				new Address("PR", "Curitiba", "Rua das Flores", "123",
						new ZipCode("80010-000")));
		return Optional.of(fake);
	}

	// Helper used by tests to build a matching id if needed.
	public static ClientId anyKnownClientId() {
		return ClientId.from(UUID.fromString("00000000-0000-0000-0000-000000000001"));
	}
}
