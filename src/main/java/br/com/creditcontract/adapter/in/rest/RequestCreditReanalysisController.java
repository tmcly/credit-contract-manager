package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditReanalysisRequestResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;
import br.com.creditcontract.application.usecase.RequestCreditReanalysisUseCase;
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

/** REST command endpoint for client-requested credit reanalysis. */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract commands")
public class RequestCreditReanalysisController {

	private final RequestCreditReanalysisUseCase useCase;

	public RequestCreditReanalysisController(RequestCreditReanalysisUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/credit-reanalysis")
	@Operation(
			operationId = "requestCreditReanalysis",
			summary = "Request a new credit analysis",
			description = """
					Available only for ACTIVE contracts after the configured 30-day cooldown.
					Returns 202 because a durable message triggers asynchronous provider evaluation;
					inspect the reanalysis history endpoint for the final APPROVED or REJECTED result.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "202", description = "Reanalysis request persisted for asynchronous processing.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditReanalysisRequestResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.REANALYSIS_REQUEST_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid correlation ID.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.INVALID_CORRELATION_ID_PROBLEM))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM))),
			@ApiResponse(responseCode = "409", description = "Contract is not ACTIVE or already has a pending reanalysis.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.REANALYSIS_NOT_ALLOWED_PROBLEM))),
			@ApiResponse(responseCode = "429", description = "The 30-day request cooldown is still active.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.REANALYSIS_COOLDOWN_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditReanalysisRequestResponse> request(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE)
			UUID correlationId) {
		CreditReanalysisRequestResult result = useCase.execute(ContractId.from(id), correlationId);
		return ResponseEntity.accepted().body(CreditReanalysisRequestResponse.from(result));
	}
}
