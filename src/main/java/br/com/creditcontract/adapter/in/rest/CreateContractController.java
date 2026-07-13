package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CreateContractInput;
import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.adapter.in.rest.dto.CreateContractRequest;
import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * REST endpoint for contract creation.
 */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract commands")
public class CreateContractController {

	private final CreateContractUseCase createContractUseCase;

	public CreateContractController(CreateContractUseCase createContractUseCase) {
		this.createContractUseCase = Objects.requireNonNull(createContractUseCase);
	}

	@PostMapping
	@Operation(
			operationId = "createCreditContract",
			summary = "Create a credit contract",
			description = """
					Creates a DRAFT contract from a CPF and an immutable client snapshot.
					The credit-analysis request is persisted in the same transaction and processed
					asynchronously after this response.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Contract created and credit analysis requested.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreditContractResponse.class),
							examples = @ExampleObject(name = "Draft contract", value = OpenApiExamples.DRAFT_CONTRACT_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid CPF, body, or correlation ID.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(name = "Missing CPF", value = OpenApiExamples.INVALID_REQUEST_PROBLEM))),
			@ApiResponse(responseCode = "404", description = "No client snapshot was found for the CPF.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and always echoes the effective value in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> create(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "CPF used to resolve the external client snapshot.", required = true,
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateContractRequest.class),
							examples = @ExampleObject(value = OpenApiExamples.CREATE_CONTRACT_REQUEST)))
			@Valid @RequestBody CreateContractRequest request,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE) UUID correlationId) {
		CreateContractInput input = new CreateContractInput(
				DocumentNumber.from(request.documentNumber()), correlationId);
		CreditContract contract = createContractUseCase.execute(input);

		CreditContractResponse response = CreditContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion()
		);

		URI location = URI.create("/api/contracts/" + contract.getId().asString());
		return ResponseEntity.created(location)
				.body(response);
	}
}
