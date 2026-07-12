package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.port.out.ProcessedMessageStore;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.entity.CreditReanalysis;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.event.EventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/** Owns transactions before and after the external reanalysis provider call. */
@Service
public class CreditReanalysisTransactionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreditReanalysisTransactionService.class);
	private static final String CONSUMER_NAME = "credit-reanalysis";
	private final CreditContractRepository repository;
	private final ProcessedMessageStore processedMessageStore;
	private final Clock clock;

	public CreditReanalysisTransactionService(
			CreditContractRepository repository,
			ProcessedMessageStore processedMessageStore,
			Clock clock) {
		this.repository = Objects.requireNonNull(repository);
		this.processedMessageStore = Objects.requireNonNull(processedMessageStore);
		this.clock = Objects.requireNonNull(clock);
	}

	public boolean wasProcessed(java.util.UUID eventId) {
		return processedMessageStore.contains(eventId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<CreditReanalysisInput> prepare(ProcessCreditReanalysisCommand command) {
		CreditContract contract = find(command);
		CreditReanalysis reanalysis = findReanalysis(contract, command);
		if (reanalysis.getStatus() != CreditReanalysisStatus.REQUESTED) {
			recordProcessed(command);
			return Optional.empty();
		}
		if (contract.getStatus() != ContractStatus.ACTIVE) {
			contract.rejectCreditReanalysisRequest(
					command.eventId(), "Contract is no longer active",
					LocalDateTime.now(clock), context(command));
			repository.save(contract);
			recordProcessed(command);
			return Optional.empty();
		}
		return Optional.of(new CreditReanalysisInput(
				contract.getClient().documentNumber(), reanalysis.getPreviousLimit()));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void complete(ProcessCreditReanalysisCommand command, CreditAnalysisResult result) {
		Objects.requireNonNull(result, "reanalysis result is required");
		if (processedMessageStore.contains(command.eventId())) {
			return;
		}
		CreditContract contract = find(command);
		CreditReanalysis reanalysis = findReanalysis(contract, command);
		if (reanalysis.getStatus() != CreditReanalysisStatus.REQUESTED) {
			recordProcessed(command);
			return;
		}
		LocalDateTime completedAt = LocalDateTime.now(clock);
		if (result instanceof CreditAnalysisResult.Approved approved) {
			contract.approveCreditReanalysisRequest(
					command.eventId(), approved.limit(), completedAt, context(command));
		} else if (result instanceof CreditAnalysisResult.Rejected rejected) {
			contract.rejectCreditReanalysisRequest(
					command.eventId(), rejected.reason(), completedAt, context(command));
		} else {
			throw new IllegalArgumentException("unsupported credit reanalysis result");
		}
		repository.save(contract);
		recordProcessed(command);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_reanalysis_completed")
				.addKeyValue("contractId", command.contractId().asString())
				.addKeyValue("reanalysisId", command.eventId())
				.addKeyValue("reanalysisStatus",
						findReanalysis(contract, command).getStatus())
				.addKeyValue("causationId", command.eventId())
				.log("Credit reanalysis completed");
	}

	private CreditContract find(ProcessCreditReanalysisCommand command) {
		return repository.findById(command.contractId())
				.orElseThrow(() -> new CreditContractNotFoundException(command.contractId()));
	}

	private CreditReanalysis findReanalysis(
			CreditContract contract, ProcessCreditReanalysisCommand command) {
		return contract.getCreditReanalyses().stream()
				.filter(reanalysis -> reanalysis.getId().equals(command.eventId()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"credit reanalysis request was not found: " + command.eventId()));
	}

	private EventContext context(ProcessCreditReanalysisCommand command) {
		return new EventContext(command.correlationId(), command.eventId());
	}

	private void recordProcessed(ProcessCreditReanalysisCommand command) {
		processedMessageStore.record(
				command.eventId(), CONSUMER_NAME, command.contractId(), command.correlationId());
	}
}
