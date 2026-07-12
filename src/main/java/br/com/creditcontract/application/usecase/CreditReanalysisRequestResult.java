package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.ContractId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Acknowledgement returned after a reanalysis request commits to the outbox. */
public record CreditReanalysisRequestResult(
		UUID requestId,
		ContractId contractId,
		LocalDateTime requestedAt,
		LocalDateTime nextEligibleAt) {

	public CreditReanalysisRequestResult {
		Objects.requireNonNull(requestId, "request id is required");
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(requestedAt, "request date is required");
		Objects.requireNonNull(nextEligibleAt, "next eligible date is required");
	}
}
