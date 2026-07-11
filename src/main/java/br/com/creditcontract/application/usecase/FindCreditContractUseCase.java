package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class FindCreditContractUseCase {

	private final CreditContractRepository repository;

	public FindCreditContractUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional(readOnly = true)
	public CreditContract execute(ContractId contractId) {
		return repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));
	}
}
