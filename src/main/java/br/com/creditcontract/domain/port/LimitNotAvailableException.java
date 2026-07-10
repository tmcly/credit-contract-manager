package br.com.creditcontract.domain.port;

/**
 * Thrown when {@link CreditLimitProvider} cannot determine a credit limit
 * for the given client (e.g. engine returned a denial or the client has
 * no eligibility).
 */
public class LimitNotAvailableException extends RuntimeException {

	public LimitNotAvailableException(String documentNumber) {
		super("credit limit not available for document: " + documentNumber);
	}
}
