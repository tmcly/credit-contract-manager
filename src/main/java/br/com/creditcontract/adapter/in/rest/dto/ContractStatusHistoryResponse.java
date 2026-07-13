package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.ContractStatusHistoryItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "One auditable lifecycle transition, returned newest first.")
public record ContractStatusHistoryResponse(
		@Schema(format = "uuid", example = "60af2a43-bd11-442f-8051-5d993f6a9752")
		UUID id,
		@Schema(description = "Null only for the initial DRAFT history entry.", example = "ACTIVE", nullable = true)
		String previousStatus,
		@Schema(example = "BLOCKED")
		String newStatus,
		@Schema(description = "Transition-specific business reason, when applicable.",
				example = "Payment overdue for more than 30 days", nullable = true)
		String reason,
		@Schema(example = "2026-07-13T11:00:00")
		LocalDateTime changedAt) {

	public static ContractStatusHistoryResponse from(ContractStatusHistoryItem item) {
		return new ContractStatusHistoryResponse(
				item.id(),
				item.previousStatus() == null ? null : item.previousStatus().name(),
				item.newStatus().name(),
				item.reason(),
				item.changedAt());
	}
}
