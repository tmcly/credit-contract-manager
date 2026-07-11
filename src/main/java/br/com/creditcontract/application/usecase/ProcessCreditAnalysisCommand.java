package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.util.Objects;
import java.util.UUID;

public record ProcessCreditAnalysisCommand(
		ContractId contractId,
		UUID eventId,
		UUID correlationId) {

	public ProcessCreditAnalysisCommand {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
	}
}
