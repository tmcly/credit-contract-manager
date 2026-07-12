package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.util.Objects;
import java.util.UUID;

/** Input translated from a credit-reanalysis-requested integration event. */
public record ProcessCreditReanalysisCommand(
		ContractId contractId,
		UUID eventId,
		UUID correlationId) {

	public ProcessCreditReanalysisCommand {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
	}
}
