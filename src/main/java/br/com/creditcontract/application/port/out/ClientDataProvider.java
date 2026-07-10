package br.com.creditcontract.application.port.out;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.Cpf;

/**
 * Retrieves a {@link Client} from the external client-registry system.
 *
 * <p>This is the bounded-context seam — the credit-contract domain does
 * not own the client aggregate; it merely needs a read-only snapshot for the
 * contract identified by CPF.
 */
public interface ClientDataProvider {

	/**
	 * Finds a client by CPF.
	 *
	 * @param cpf validated client CPF
	 * @return the client snapshot
	 * @throws ClientNotFoundException if no client matches the CPF
	 */
	Client findByCpf(Cpf cpf);
}
