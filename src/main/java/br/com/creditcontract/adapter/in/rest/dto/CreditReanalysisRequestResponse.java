package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/** Asynchronous acknowledgement for a credit reanalysis request. */
@Schema(description = "Acknowledgement that a credit-reanalysis request was persisted for asynchronous processing.")
public record CreditReanalysisRequestResponse(
		@Schema(format = "uuid", example = "a4424800-0d30-49a9-bdb1-17206e00ea72")
		UUID requestId,
		@Schema(format = "uuid", example = "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a")
		String contractId,
		@Schema(example = "REQUESTED", allowableValues = {"REQUESTED"})
		String status,
		@Schema(example = "2026-07-13T10:20:00")
		LocalDateTime requestedAt,
		@Schema(description = "First instant at which the client may request another reanalysis.",
				example = "2026-08-12T10:20:00")
		LocalDateTime nextEligibleAt) {

	public static CreditReanalysisRequestResponse from(CreditReanalysisRequestResult result) {
		return new CreditReanalysisRequestResponse(
				result.requestId(), result.contractId().asString(), "REQUESTED",
				result.requestedAt(), result.nextEligibleAt());
	}
}
