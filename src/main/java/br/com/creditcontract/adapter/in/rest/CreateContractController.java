package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CreateContractInput;
import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.Cpf;
import br.com.creditcontract.adapter.in.rest.dto.CreateContractRequest;
import br.com.creditcontract.adapter.in.rest.dto.CreateContractResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;

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
	public ResponseEntity<CreateContractResponse> create(@Valid @RequestBody CreateContractRequest request) {
		CreateContractInput input = new CreateContractInput(Cpf.from(request.cpf()));
		CreditContract contract = createContractUseCase.execute(input);

		CreateContractResponse response = CreateContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion()
		);

		URI location = URI.create("/api/contracts/" + contract.getId().asString());
		return ResponseEntity.created(location).body(response);
	}
}
