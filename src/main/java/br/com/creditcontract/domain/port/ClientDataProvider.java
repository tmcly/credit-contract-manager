package br.com.creditcontract.domain.port;

import br.com.creditcontract.domain.valueobject.ClientId;
import br.com.creditcontract.domain.valueobject.Client;

import java.util.Optional;

/**
 * Domain port: retrieves client data owned by the EXTERNAL client-context.
 *
 * This is the Anti-Corruption Layer seam. The contract domain declares WHAT
 * it needs (read a client by id) but NEVER HOW (HTTP, gRPC, DB...). The
 * concrete implementation lives in the infrastructure layer and may call
 * the client management API.
 *
 * Keeping this as an interface in the domain is what preserves Clean
 * Architecture: the domain stays ignorant of any transport or external
 * system.
 */
public interface ClientDataProvider {

	Optional<Client> fetch(ClientId clientId);
}
