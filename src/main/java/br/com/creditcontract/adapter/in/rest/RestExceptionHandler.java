package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.application.exception.LimitNotAvailableException;
import br.com.creditcontract.domain.exception.InvalidCpfException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Maps application and domain errors to RFC 7807 {@link ProblemDetail} responses.
 */
@RestControllerAdvice
public class RestExceptionHandler {

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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleRequestValidation(MethodArgumentNotValidException exception) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage() == null
						? "request validation failed"
						: error.getDefaultMessage())
				.orElse("request validation failed");
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
		problem.setTitle("Invalid request");
		problem.setType(URI.create("/errors/request-validation"));
		return problem;
	}

	@ExceptionHandler(InvalidCpfException.class)
	ProblemDetail handleInvalidCpf(InvalidCpfException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Invalid request");
		problem.setType(URI.create("/errors/invalid-cpf"));
		return problem;
	}
}
