package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.CancellationOrigin;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/** Applies a client or legal cancellation request to a credit contract. */
@Service
public class CancelCreditContractUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CancelCreditContractUseCase.class);
	private final CreditContractRepository repository;

	public CancelCreditContractUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional
	public CreditContract execute(
			ContractId contractId,
			CancellationOrigin origin,
			String reason,
			UUID correlationId) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(origin, "cancellation origin is required");
		Objects.requireNonNull(reason, "cancellation reason is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (origin == CancellationOrigin.BLOCKED_EXPIRATION) {
			throw new IllegalArgumentException("automatic cancellation cannot use the manual endpoint");
		}

		CreditContract contract = repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));
		ContractStatus previousStatus = contract.getStatus();
		if (origin == CancellationOrigin.CLIENT_REQUEST) {
			contract.cancelByClient(reason, correlationId);
		} else {
			contract.cancelForLegalReason(reason, correlationId);
		}
		repository.save(contract);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_contract_cancelled")
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("previousStatus", previousStatus)
				.addKeyValue("newStatus", ContractStatus.CANCELLED)
				.addKeyValue("cancellationOrigin", origin)
				.addKeyValue("correlationId", correlationId)
				.log("Credit contract cancelled");
		return contract;
	}
}
