package br.com.creditcontract.domain.port;

import br.com.creditcontract.domain.valueobject.Client;

/**
 * Retrieves a {@link Client} from the external client-registry system.
 *
 * <p>This is the bounded-context seam — the credit-contract domain does
 * not own the client aggregate; it merely needs a read-only snapshot for
 * the contract.
 */
public interface ClientDataProvider {

	/**
	 * Finds a client by their document number (CPF/CNPJ).
	 *
	 * @param documentNumber non-null, non-blank document identifier
	 * @return the client snapshot
	 * @throws ClientNotFoundException if no client matches the document number
	 */
	Client findByDocument(String documentNumber);
}
