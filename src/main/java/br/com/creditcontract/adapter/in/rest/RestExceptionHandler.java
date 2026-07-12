package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.domain.exception.InvalidDocumentNumberException;
import br.com.creditcontract.domain.exception.InvalidContractTransitionException;
import br.com.creditcontract.domain.exception.CreditReanalysisCooldownException;
import br.com.creditcontract.domain.exception.CreditReanalysisNotAllowedException;
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

	@ExceptionHandler(CreditContractNotFoundException.class)
	ProblemDetail handleContractNotFound(CreditContractNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Credit contract not found");
		problem.setType(URI.create("/errors/credit-contract-not-found"));
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

	@ExceptionHandler(InvalidDocumentNumberException.class)
	ProblemDetail handleInvalidDocumentNumber(InvalidDocumentNumberException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Invalid request");
		problem.setType(URI.create("/errors/invalid-document-number"));
		return problem;
	}

	@ExceptionHandler(InvalidContractTransitionException.class)
	ProblemDetail handleInvalidContractTransition(InvalidContractTransitionException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Invalid contract transition");
		problem.setType(URI.create("/errors/invalid-contract-transition"));
		return problem;
	}

	@ExceptionHandler(CreditReanalysisNotAllowedException.class)
	ProblemDetail handleCreditReanalysisNotAllowed(CreditReanalysisNotAllowedException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Credit reanalysis not allowed");
		problem.setType(URI.create("/errors/credit-reanalysis-not-allowed"));
		return problem;
	}

	@ExceptionHandler(CreditReanalysisCooldownException.class)
	ProblemDetail handleCreditReanalysisCooldown(CreditReanalysisCooldownException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
		problem.setTitle("Credit reanalysis cooldown active");
		problem.setType(URI.create("/errors/credit-reanalysis-cooldown"));
		problem.setProperty("nextEligibleAt", exception.getNextEligibleAt());
		return problem;
	}
}
