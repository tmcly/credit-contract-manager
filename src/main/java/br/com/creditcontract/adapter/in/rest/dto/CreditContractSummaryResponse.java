package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.CreditContractSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Privacy-conscious contract summary used in paginated searches.")
public record CreditContractSummaryResponse(
		@Schema(format = "uuid", example = "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a")
		String id,
		@Schema(example = "CT-2026-000001")
		String contractNumber,
		@Schema(example = "Maria Silva")
		String clientName,
		@Schema(example = "ACTIVE",
				allowableValues = {"DRAFT", "UNDER_REVIEW", "APPROVED", "REJECTED", "ACCEPTED", "ACTIVE", "BLOCKED", "CANCELLED"})
		String status,
		@Schema(description = "Approved limit in Brazilian reais.", example = "7500.00", nullable = true)
		String creditLimit,
		@Schema(example = "2026-07-13T10:15:30")
		LocalDateTime createdAt,
		@Schema(example = "2026-07-13T10:18:12")
		LocalDateTime updatedAt,
		@Schema(example = "4")
		long version) {

	public static CreditContractSummaryResponse from(CreditContractSummary summary) {
		return new CreditContractSummaryResponse(
				summary.id().asString(),
				summary.contractNumber(),
				summary.clientName(),
				summary.status().name(),
				summary.creditLimit() == null ? null : summary.creditLimit().toPlainString(),
				summary.createdAt(),
				summary.updatedAt(),
				summary.version());
	}
}
