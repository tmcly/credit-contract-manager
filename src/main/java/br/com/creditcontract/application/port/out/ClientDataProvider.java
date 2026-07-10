package br.com.creditcontract.application.port.out;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;

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
	 * @param documentNumber validated client document identifier
	 * @return the client snapshot
	 * @throws ClientNotFoundException if no client matches the document number
	 */
	Client findByDocument(DocumentNumber documentNumber);
}
