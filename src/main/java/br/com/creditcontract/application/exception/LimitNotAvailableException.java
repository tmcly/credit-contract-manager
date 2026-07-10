package br.com.creditcontract.application.exception;

import br.com.creditcontract.application.port.out.CreditLimitProvider;
import br.com.creditcontract.domain.valueobject.DocumentNumber;

/**
 * Thrown when {@link CreditLimitProvider} cannot determine a credit limit
 * for the given client (e.g. engine returned a denial or the client has
 * no eligibility).
 */
public class LimitNotAvailableException extends RuntimeException {

	public LimitNotAvailableException(DocumentNumber documentNumber) {
		super("credit limit not available for document: " + documentNumber.value());
	}
}
