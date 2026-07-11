package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditAnalysisProvider;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/** Processes a credit-analysis request delivered at least once by RabbitMQ. */
@Service
public class ProcessCreditAnalysisUseCase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCreditAnalysisUseCase.class);

	private final CreditAnalysisProvider analysisProvider;
	private final CreditAnalysisTransactionService transactionService;

	public ProcessCreditAnalysisUseCase(
			CreditAnalysisProvider analysisProvider,
			CreditAnalysisTransactionService transactionService) {
		this.analysisProvider = Objects.requireNonNull(analysisProvider);
		this.transactionService = Objects.requireNonNull(transactionService);
	}

	public void execute(ProcessCreditAnalysisCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (transactionService.wasProcessed(command.eventId())) {
			LOGGER.atInfo()
					.addKeyValue("event", "message_ignored_duplicate")
					.addKeyValue("contractId", command.contractId().asString())
					.log("Credit-analysis message was already processed");
			return;
		}
		Optional<DocumentNumber> documentNumber =
				transactionService.startOrResume(command.contractId());
		if (documentNumber.isEmpty()) {
			LOGGER.atInfo()
					.addKeyValue("event", "credit_analysis_request_ignored")
					.addKeyValue("contractId", command.contractId().asString())
					.log("Credit-analysis request no longer requires processing");
			return;
		}
		CreditAnalysisResult result = analysisProvider.analyze(documentNumber.orElseThrow());
		transactionService.complete(
				command.contractId(),
				result,
				new EventContext(command.correlationId(), command.eventId()),
				command.eventId());
	}
}
