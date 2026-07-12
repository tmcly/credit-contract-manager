package br.com.creditcontract.application.exception;

public class InvalidQueryParameterException extends RuntimeException {

	public InvalidQueryParameterException(String message) {
		super(message);
	}
}
