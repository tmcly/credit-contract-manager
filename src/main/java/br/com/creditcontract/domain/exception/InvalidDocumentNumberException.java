package br.com.creditcontract.domain.exception;

/**
 * Thrown when a client CPF is missing, malformed or has invalid check digits.
 */
public class InvalidDocumentNumberException extends IllegalArgumentException {

	public InvalidDocumentNumberException(String message) {
		super(message);
	}
}
