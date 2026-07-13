package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.application.usecase.FindCreditContractUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract queries")
public class FindCreditContractController {

	private final FindCreditContractUseCase useCase;

	public FindCreditContractController(FindCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@GetMapping("/{id}")
	@Operation(
			operationId = "findCreditContractById",
			summary = "Find a credit contract",
			description = "Returns the current aggregate state. Use the history endpoints for transition and reanalysis audits.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Current contract state.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditContractResponse.class),
							examples = @ExampleObject(name = "Active contract", value = OpenApiExamples.ACTIVE_CONTRACT_RESPONSE))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public CreditContractResponse findById(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id) {
		CreditContract contract = useCase.execute(ContractId.from(id));
		return CreditContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion());
	}
}
