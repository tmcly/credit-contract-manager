package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.application.port.out.CreditReanalysisProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/** Processes a credit-reanalysis request delivered at least once by RabbitMQ. */
@Service
public class ProcessCreditReanalysisUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCreditReanalysisUseCase.class);
	private final CreditReanalysisProvider provider;
	private final CreditReanalysisTransactionService transactionService;

	public ProcessCreditReanalysisUseCase(
			CreditReanalysisProvider provider,
			CreditReanalysisTransactionService transactionService) {
		this.provider = Objects.requireNonNull(provider);
		this.transactionService = Objects.requireNonNull(transactionService);
	}

	public void execute(ProcessCreditReanalysisCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (transactionService.wasProcessed(command.eventId())) {
			LOGGER.atInfo()
					.addKeyValue("event", "message_ignored_duplicate")
					.addKeyValue("consumer", "credit-reanalysis")
					.addKeyValue("contractId", command.contractId().asString())
					.log("Credit-reanalysis message was already processed");
			return;
		}
		Optional<CreditReanalysisInput> input = transactionService.prepare(command);
		if (input.isEmpty()) {
			return;
		}
		CreditReanalysisInput providerInput = input.orElseThrow();
		CreditAnalysisResult result = provider.reanalyze(
				providerInput.documentNumber(), providerInput.currentLimit(), command.eventId());
		transactionService.complete(command, result);
	}
}
