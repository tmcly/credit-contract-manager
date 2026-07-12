package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Cancels blocked contracts whose configurable regularization period elapsed. */
@Service
public class CancelExpiredBlockedContractsUseCase {

	public static final String EXPIRATION_REASON =
			"Blocked contract regularization period elapsed";
	private static final Logger LOGGER =
			LoggerFactory.getLogger(CancelExpiredBlockedContractsUseCase.class);
	private final CreditContractRepository repository;

	public CancelExpiredBlockedContractsUseCase(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional
	public int execute(LocalDateTime cutoff, int batchSize, UUID correlationId) {
		Objects.requireNonNull(cutoff, "expiration cutoff is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		if (batchSize <= 0) {
			throw new IllegalArgumentException("batch size must be positive");
		}
		List<CreditContract> expired = repository.findBlockedUpdatedBefore(cutoff, batchSize);
		for (CreditContract contract : expired) {
			contract.cancelAfterBlockedExpiration(EXPIRATION_REASON, correlationId);
			repository.save(contract);
			LOGGER.atInfo()
					.addKeyValue("event", "credit_contract_cancelled")
					.addKeyValue("contractId", contract.getId().asString())
					.addKeyValue("previousStatus", ContractStatus.BLOCKED)
					.addKeyValue("newStatus", ContractStatus.CANCELLED)
					.addKeyValue("cancellationOrigin", "BLOCKED_EXPIRATION")
					.addKeyValue("correlationId", correlationId)
					.log("Expired blocked credit contract cancelled");
		}
		return expired.size();
	}
}
