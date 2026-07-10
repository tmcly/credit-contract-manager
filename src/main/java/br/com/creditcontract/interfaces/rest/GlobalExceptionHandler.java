package br.com.creditcontract.interfaces.rest;

import br.com.creditcontract.domain.port.ClientNotFoundException;
import br.com.creditcontract.domain.port.LimitNotAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Maps domain exceptions to RFC 7807 {@link ProblemDetail} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ClientNotFoundException.class)
	ProblemDetail handleClientNotFound(ClientNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Client not found");
		problem.setType(URI.create("/errors/client-not-found"));
		return problem;
	}

	@ExceptionHandler(LimitNotAvailableException.class)
	ProblemDetail handleLimitNotAvailable(LimitNotAvailableException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
		problem.setTitle("Credit limit not available");
		problem.setType(URI.create("/errors/limit-not-available"));
		return problem;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleBadRequest(IllegalArgumentException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Invalid request");
		problem.setType(URI.create("/errors/bad-request"));
		return problem;
	}
}
