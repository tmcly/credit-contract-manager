package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;

import java.time.LocalDateTime;
import java.util.UUID;

/** Asynchronous acknowledgement for a credit reanalysis request. */
public record CreditReanalysisRequestResponse(
		UUID requestId,
		String contractId,
		String status,
		LocalDateTime requestedAt,
		LocalDateTime nextEligibleAt) {

	public static CreditReanalysisRequestResponse from(CreditReanalysisRequestResult result) {
		return new CreditReanalysisRequestResponse(
				result.requestId(), result.contractId().asString(), "REQUESTED",
				result.requestedAt(), result.nextEligibleAt());
	}
}
