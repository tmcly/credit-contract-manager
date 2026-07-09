package br.com.creditcontract.infrastructure.client;

import br.com.creditcontract.domain.valueobject.ClientId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StubClientDataProviderTest {

	private final StubClientDataProvider provider = new StubClientDataProvider();

	@Test
	void fetch_returns_client_snapshot_for_any_id() {
		ClientId id = StubClientDataProvider.anyKnownClientId();
		var client = provider.fetch(id);
		assertTrue(client.isPresent());
		assertNotNull(client.get().name());
		assertNotNull(client.get().address());
	}

	@Test
	void fetch_returns_empty_when_id_is_null() {
		assertTrue(provider.fetch(null).isEmpty());
	}
}
