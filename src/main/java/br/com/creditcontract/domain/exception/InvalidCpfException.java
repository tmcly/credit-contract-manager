package br.com.creditcontract.domain.exception;

/**
 * Thrown when a client CPF is missing, malformed or has invalid check digits.
 */
public class InvalidCpfException extends IllegalArgumentException {

	public InvalidCpfException(String message) {
		super(message);
	}
}
