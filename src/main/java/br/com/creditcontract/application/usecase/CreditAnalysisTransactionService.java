package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/** Owns the database transactions around the external credit-analysis call. */
@Service
public class CreditAnalysisTransactionService {

	private final CreditContractRepository repository;

	public CreditAnalysisTransactionService(CreditContractRepository repository) {
		this.repository = Objects.requireNonNull(repository);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<DocumentNumber> startOrResume(ContractId contractId) {
		Objects.requireNonNull(contractId, "contract id is required");
		CreditContract contract = find(contractId);
		if (contract.hasCreditAnalysisFinished()) {
			return Optional.empty();
		}
		if (contract.getStatus() == ContractStatus.DRAFT) {
			contract.startCreditAnalysis();
			repository.save(contract);
		} else if (contract.getStatus() != ContractStatus.UNDER_REVIEW) {
			throw new IllegalStateException(
					"credit analysis cannot run for contract in " + contract.getStatus());
		}
		return Optional.of(contract.getClient().documentNumber());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void complete(
			ContractId contractId,
			CreditAnalysisResult result,
			EventContext eventContext) {
		Objects.requireNonNull(result, "analysis result is required");
		Objects.requireNonNull(eventContext, "event context is required");
		CreditContract contract = find(contractId);
		if (contract.hasCreditAnalysisFinished()) {
			return;
		}
		if (result instanceof CreditAnalysisResult.Approved approved) {
			contract.approveCreditAnalysis(approved.limit(), eventContext);
		} else if (result instanceof CreditAnalysisResult.Rejected rejected) {
			contract.rejectCreditAnalysis(rejected.reason(), eventContext);
		}
		repository.save(contract);
	}

	private CreditContract find(ContractId contractId) {
		return repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));
	}
}
