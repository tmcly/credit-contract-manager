package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.application.usecase.AcceptCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/** REST command endpoint for client acceptance of an approved contract. */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract commands")
public class AcceptCreditContractController {

	private final AcceptCreditContractUseCase useCase;

	public AcceptCreditContractController(AcceptCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/acceptance")
	@Operation(
			operationId = "acceptCreditContract",
			summary = "Accept an approved contract",
			description = """
					Records client acceptance of an APPROVED contract. Repeating the request after
					the contract is already ACCEPTED or ACTIVE is idempotent. A committed acceptance
					event later drives asynchronous activation.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Acceptance recorded or an earlier acceptance returned idempotently.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditContractResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.ACCEPTED_CONTRACT_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid correlation ID.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM))),
			@ApiResponse(responseCode = "409", description = "Contract is not APPROVED and has not already been accepted.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.ACCEPTANCE_CONFLICT_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> accept(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE) UUID correlationId) {
		CreditContract contract = useCase.execute(ContractId.from(id), correlationId);
		return ResponseEntity.ok(CreditContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion()));
	}
}
