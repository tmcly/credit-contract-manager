package br.com.creditcontract.domain.exception;

/**
 * Thrown when a client document cannot be represented as a valid CPF or CNPJ.
 */
public class InvalidDocumentNumberException extends IllegalArgumentException {

	public InvalidDocumentNumberException(String message) {
		super(message);
	}
}
