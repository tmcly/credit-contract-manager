package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;

/** Current credit-contract representation returned by create and query APIs. */
public record CreditContractResponse(
		String id,
		String contractNumber,
		String clientName,
		String status,
		String creditLimit,
		LocalDateTime createdAt,
		Long version) {

	public static CreditContractResponse from(
			String id,
			String contractNumber,
			String clientName,
			String status,
			MonetaryAmount creditLimit,
			LocalDateTime createdAt,
			Long version) {
		return new CreditContractResponse(
				id,
				contractNumber,
				clientName,
				status,
				creditLimit == null ? null : creditLimit.amount().toPlainString(),
				createdAt,
				version);
	}
}
