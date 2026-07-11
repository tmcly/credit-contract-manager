package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.application.usecase.FindCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
public class FindCreditContractController {

	private final FindCreditContractUseCase useCase;

	public FindCreditContractController(FindCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@GetMapping("/{id}")
	public CreditContractResponse findById(@PathVariable UUID id) {
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
