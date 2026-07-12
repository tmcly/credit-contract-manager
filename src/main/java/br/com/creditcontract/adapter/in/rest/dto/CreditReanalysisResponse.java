package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.CreditReanalysisItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditReanalysisResponse(
		UUID id,
		String status,
		String previousLimit,
		String newLimit,
		String reason,
		LocalDateTime requestedAt,
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
