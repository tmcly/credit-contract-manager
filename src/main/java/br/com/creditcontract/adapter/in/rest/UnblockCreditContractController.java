package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.adapter.in.rest.dto.UnblockCreditContractRequest;
import br.com.creditcontract.application.usecase.UnblockCreditContractUseCase;
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

/** REST command endpoint used by external applications to unblock contracts. */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract commands")
public class UnblockCreditContractController {

	private final UnblockCreditContractUseCase useCase;

	public UnblockCreditContractController(UnblockCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/unblocking")
	@Operation(
			operationId = "unblockCreditContract",
			summary = "Unblock a blocked contract",
			description = "Restores only a BLOCKED contract to ACTIVE, records the reason in status history, and emits CreditContractUnblocked through the transactional outbox.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Contract restored to ACTIVE.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditContractResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.ACTIVE_CONTRACT_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid body or correlation ID.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.INVALID_UNBLOCK_REQUEST_PROBLEM))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM))),
			@ApiResponse(responseCode = "409", description = "Contract is not BLOCKED or a concurrent command changed it.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = {
									@ExampleObject(name = "Invalid transition", value = OpenApiExamples.UNBLOCK_CONFLICT_PROBLEM),
									@ExampleObject(name = "Concurrent update", value = OpenApiExamples.CONCURRENT_UPDATE_PROBLEM)
							}))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> unblock(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Reason that will remain in the contract audit history.", required = true,
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnblockCreditContractRequest.class),
							examples = @ExampleObject(value = OpenApiExamples.UNBLOCK_CONTRACT_REQUEST)))
			@Valid @RequestBody UnblockCreditContractRequest request,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE) UUID correlationId) {
		CreditContract contract = useCase.execute(
				ContractId.from(id), request.reason(), correlationId);
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
