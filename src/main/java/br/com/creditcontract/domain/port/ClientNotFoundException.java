package br.com.creditcontract.domain.port;

/**
 * Thrown when {@link ClientDataProvider} cannot find a client by the
 * supplied document number.
 */
public class ClientNotFoundException extends RuntimeException {

	public ClientNotFoundException(String documentNumber) {
		super("client not found for document: " + documentNumber);
	}
}
