package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.util.Objects;
import java.util.UUID;

/** Input translated from an accepted-contract integration event. */
public record ActivateCreditContractCommand(
		ContractId contractId,
		UUID eventId,
		UUID correlationId) {

	public ActivateCreditContractCommand {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(eventId, "event id is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
	}
}
