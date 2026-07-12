package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Applies the client's acceptance to an approved credit contract. */
@Service
public class AcceptCreditContractUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptCreditContractUseCase.class);
	private final CreditContractRepository repository;

	public AcceptCreditContractUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional
	public CreditContract execute(ContractId contractId, UUID correlationId) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		CreditContract contract = repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));

		if (contract.getStatus() == ContractStatus.ACCEPTED) {
			LOGGER.atInfo()
					.addKeyValue("event", "credit_contract_acceptance_ignored")
					.addKeyValue("contractId", contractId.asString())
					.addKeyValue("contractStatus", contract.getStatus())
					.log("Credit contract was already accepted");
			return contract;
		}

		contract.accept(correlationId);
		repository.save(contract);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_contract_accepted")
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("previousStatus", ContractStatus.APPROVED)
				.addKeyValue("newStatus", ContractStatus.ACCEPTED)
				.addKeyValue("correlationId", correlationId)
				.log("Credit contract accepted");
		return contract;
	}
}
