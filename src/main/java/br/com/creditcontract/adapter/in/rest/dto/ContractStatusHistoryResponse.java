package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.ContractStatusHistoryItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContractStatusHistoryResponse(
		UUID id,
		String previousStatus,
		String newStatus,
		String reason,
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
