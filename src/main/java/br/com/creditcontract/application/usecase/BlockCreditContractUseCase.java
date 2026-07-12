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

/** Applies an external blocking request to an active credit contract. */
@Service
public class BlockCreditContractUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlockCreditContractUseCase.class);
	private final CreditContractRepository repository;

	public BlockCreditContractUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional
	public CreditContract execute(ContractId contractId, String reason, UUID correlationId) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(reason, "blocking reason is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		CreditContract contract = repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));

		if (contract.getStatus() == ContractStatus.BLOCKED) {
			LOGGER.atInfo()
					.addKeyValue("event", "credit_contract_blocking_ignored")
					.addKeyValue("contractId", contractId.asString())
					.addKeyValue("contractStatus", contract.getStatus())
					.log("Credit contract was already blocked");
			return contract;
		}

		contract.block(reason, correlationId);
		repository.save(contract);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_contract_blocked")
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("previousStatus", ContractStatus.ACTIVE)
				.addKeyValue("newStatus", ContractStatus.BLOCKED)
				.addKeyValue("correlationId", correlationId)
				.log("Credit contract blocked");
		return contract;
	}
}
