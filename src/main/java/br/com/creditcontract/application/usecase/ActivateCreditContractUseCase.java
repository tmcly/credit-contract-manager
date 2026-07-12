package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.port.out.ProcessedMessageStore;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.EventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Activates accepted contracts from at-least-once RabbitMQ deliveries. */
@Service
public class ActivateCreditContractUseCase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivateCreditContractUseCase.class);
	private static final String CONSUMER_NAME = "contract-activation";

	private final CreditContractRepository repository;
	private final ProcessedMessageStore processedMessageStore;

	public ActivateCreditContractUseCase(
			CreditContractRepository repository,
			ProcessedMessageStore processedMessageStore) {
		this.repository = Objects.requireNonNull(repository);
		this.processedMessageStore = Objects.requireNonNull(processedMessageStore);
	}

	@Transactional
	public void execute(ActivateCreditContractCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (processedMessageStore.contains(command.eventId())) {
			LOGGER.atInfo()
					.addKeyValue("event", "message_ignored_duplicate")
					.addKeyValue("consumer", CONSUMER_NAME)
					.addKeyValue("contractId", command.contractId().asString())
					.log("Contract-activation message was already processed");
			return;
		}

		CreditContract contract = repository.findById(command.contractId())
				.orElseThrow(() -> new CreditContractNotFoundException(command.contractId()));
		if (contract.getStatus() == ContractStatus.ACTIVE) {
			recordProcessed(command);
			LOGGER.atInfo()
					.addKeyValue("event", "contract_activation_ignored")
					.addKeyValue("contractId", command.contractId().asString())
					.addKeyValue("contractStatus", contract.getStatus())
					.log("Contract was already active");
			return;
		}

		ContractStatus previousStatus = contract.getStatus();
		contract.activate(new EventContext(command.correlationId(), command.eventId()));
		repository.save(contract);
		recordProcessed(command);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_contract_activated")
				.addKeyValue("contractId", command.contractId().asString())
				.addKeyValue("previousStatus", previousStatus)
				.addKeyValue("newStatus", contract.getStatus())
				.addKeyValue("causationId", command.eventId())
				.log("Credit contract activated");
	}

	private void recordProcessed(ActivateCreditContractCommand command) {
		processedMessageStore.record(
				command.eventId(),
				CONSUMER_NAME,
				command.contractId(),
				command.correlationId());
	}
}
