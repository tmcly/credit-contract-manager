package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.CreditReanalysisItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "One credit-reanalysis request and its eventual outcome, returned newest first.")
public record CreditReanalysisResponse(
		@Schema(format = "uuid", example = "a4424800-0d30-49a9-bdb1-17206e00ea72")
		UUID id,
		@Schema(example = "APPROVED", allowableValues = {"REQUESTED", "APPROVED", "REJECTED"})
		String status,
		@Schema(description = "Limit in Brazilian reais before reanalysis.", example = "5000.00")
		String previousLimit,
		@Schema(description = "Approved new limit. Null while pending or when rejected.", example = "7500.00", nullable = true)
		String newLimit,
		@Schema(description = "Provider reason when the request is rejected.", nullable = true)
		String reason,
		@Schema(example = "2026-07-13T10:20:00")
		LocalDateTime requestedAt,
		@Schema(description = "Null while asynchronous processing is pending.",
				example = "2026-07-13T10:20:01", nullable = true)
		LocalDateTime completedAt) {

	public static CreditReanalysisResponse from(CreditReanalysisItem item) {
		return new CreditReanalysisResponse(
				item.id(),
				item.status().name(),
				item.previousLimit().toPlainString(),
				item.newLimit() == null ? null : item.newLimit().toPlainString(),
				item.reason(),
				item.requestedAt(),
				item.completedAt());
	}
}
