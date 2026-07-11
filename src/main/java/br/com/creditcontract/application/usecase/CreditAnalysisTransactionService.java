package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.port.out.ProcessedMessageStore;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/** Owns the database transactions around the external credit-analysis call. */
@Service
public class CreditAnalysisTransactionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreditAnalysisTransactionService.class);

	private final CreditContractRepository repository;
	private final ProcessedMessageStore processedMessageStore;

	public CreditAnalysisTransactionService(
			CreditContractRepository repository,
			ProcessedMessageStore processedMessageStore) {
		this.repository = Objects.requireNonNull(repository);
		this.processedMessageStore = Objects.requireNonNull(processedMessageStore);
	}

	public boolean wasProcessed(java.util.UUID eventId) {
		return processedMessageStore.contains(eventId);
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
			LOGGER.atInfo()
					.addKeyValue("event", "credit_analysis_started")
					.addKeyValue("contractId", contractId.asString())
					.addKeyValue("previousStatus", ContractStatus.DRAFT)
					.addKeyValue("newStatus", ContractStatus.UNDER_REVIEW)
					.log("Credit analysis started");
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
			EventContext eventContext,
			java.util.UUID consumedEventId) {
		Objects.requireNonNull(result, "analysis result is required");
		Objects.requireNonNull(eventContext, "event context is required");
		CreditContract contract = find(contractId);
		if (contract.hasCreditAnalysisFinished()) {
			processedMessageStore.record(consumedEventId, "credit-analysis", contractId,
					eventContext.correlationId());
			LOGGER.atInfo()
					.addKeyValue("event", "credit_analysis_completion_ignored")
					.addKeyValue("contractId", contractId.asString())
					.addKeyValue("contractStatus", contract.getStatus())
					.log("Credit analysis was already complete");
			return;
		}
		ContractStatus previousStatus = contract.getStatus();
		String completedEvent;
		if (result instanceof CreditAnalysisResult.Approved approved) {
			contract.approveCreditAnalysis(approved.limit(), eventContext);
			completedEvent = "credit_analysis_approved";
		} else if (result instanceof CreditAnalysisResult.Rejected rejected) {
			contract.rejectCreditAnalysis(rejected.reason(), eventContext);
			completedEvent = "credit_analysis_rejected";
		} else {
			throw new IllegalArgumentException("unsupported credit analysis result");
		}
		repository.save(contract);
		processedMessageStore.record(consumedEventId, "credit-analysis", contractId,
				eventContext.correlationId());
		LOGGER.atInfo()
				.addKeyValue("event", completedEvent)
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("previousStatus", previousStatus)
				.addKeyValue("newStatus", contract.getStatus())
				.addKeyValue("causationId", consumedEventId)
				.log("Credit analysis completed");
	}

	private CreditContract find(ContractId contractId) {
		return repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));
	}
}
