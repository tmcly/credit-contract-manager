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

/** Applies an external unblocking request to a blocked credit contract. */
@Service
public class UnblockCreditContractUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnblockCreditContractUseCase.class);
	private final CreditContractRepository repository;

	public UnblockCreditContractUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional
	public CreditContract execute(ContractId contractId, String reason, UUID correlationId) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(reason, "unblocking reason is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		CreditContract contract = repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));

		contract.unblock(reason, correlationId);
		repository.save(contract);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_contract_unblocked")
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("previousStatus", ContractStatus.BLOCKED)
				.addKeyValue("newStatus", ContractStatus.ACTIVE)
				.addKeyValue("correlationId", correlationId)
				.log("Credit contract unblocked");
		return contract;
	}
}
