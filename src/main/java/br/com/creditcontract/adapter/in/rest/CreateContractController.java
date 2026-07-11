package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CreateContractInput;
import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.adapter.in.rest.dto.CreateContractRequest;
import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class CreateContractController {

	private final CreateContractUseCase createContractUseCase;

	public CreateContractController(CreateContractUseCase createContractUseCase) {
		this.createContractUseCase = Objects.requireNonNull(createContractUseCase);
	}

	@PostMapping
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> create(
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
