package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CancelCreditContractRequest;
import br.com.creditcontract.adapter.in.rest.dto.CancellationRequester;
import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.application.usecase.CancelCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.CancellationOrigin;
import br.com.creditcontract.domain.valueobject.ContractId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/** REST command endpoint for client-requested and legal contract cancellation. */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract commands")
public class CancelCreditContractController {

	private final CancelCreditContractUseCase useCase;

	public CancelCreditContractController(CancelCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/cancellation")
	@Operation(
			operationId = "cancelCreditContract",
			summary = "Cancel a contract manually",
			description = """
					CLIENT may cancel only an ACTIVE contract. LEGAL may cancel ACTIVE or BLOCKED
					contracts. Automatic cancellation after the blocked-expiration period is handled
					internally and does not use this endpoint.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Contract cancelled.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditContractResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CANCELLED_CONTRACT_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid body or correlation ID.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.INVALID_CANCELLATION_REQUEST_PROBLEM))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM))),
			@ApiResponse(responseCode = "409", description = "Requester cannot cancel the contract in its current state.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CANCELLATION_CONFLICT_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> cancel(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Requesting party and auditable cancellation reason.", required = true,
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CancelCreditContractRequest.class),
							examples = @ExampleObject(value = OpenApiExamples.CANCEL_CONTRACT_REQUEST)))
			@Valid @RequestBody CancelCreditContractRequest request,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE) UUID correlationId) {
		CreditContract contract = useCase.execute(
				ContractId.from(id), origin(request.requestedBy()), request.reason(), correlationId);
		return ResponseEntity.ok(CreditContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion()));
	}

	private CancellationOrigin origin(CancellationRequester requester) {
		return switch (requester) {
			case CLIENT -> CancellationOrigin.CLIENT_REQUEST;
			case LEGAL -> CancellationOrigin.LEGAL_REQUEST;
		};
	}
}
