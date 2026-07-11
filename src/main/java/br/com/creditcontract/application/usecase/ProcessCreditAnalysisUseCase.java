package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditAnalysisProvider;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/** Processes a credit-analysis request delivered at least once by RabbitMQ. */
@Service
public class ProcessCreditAnalysisUseCase {

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
		Optional<DocumentNumber> documentNumber =
				transactionService.startOrResume(command.contractId());
		if (documentNumber.isEmpty()) {
			return;
		}
		CreditAnalysisResult result = analysisProvider.analyze(documentNumber.orElseThrow());
		transactionService.complete(
				command.contractId(),
				result,
				new EventContext(command.correlationId(), command.eventId()));
	}
}
