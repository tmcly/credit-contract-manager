package br.com.creditcontract.application.exception;

import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.domain.valueobject.Cpf;

/**
 * Thrown when {@link ClientDataProvider} cannot find a client by the
 * supplied CPF.
 */
public class ClientNotFoundException extends RuntimeException {

	public ClientNotFoundException(Cpf cpf) {
		super("client not found for CPF: " + cpf.value());
	}
}
