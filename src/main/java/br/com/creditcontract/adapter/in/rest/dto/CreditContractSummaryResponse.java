package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.application.query.CreditContractSummary;

import java.time.LocalDateTime;

public record CreditContractSummaryResponse(
		String id,
		String contractNumber,
		String clientName,
		String status,
		String creditLimit,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
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
