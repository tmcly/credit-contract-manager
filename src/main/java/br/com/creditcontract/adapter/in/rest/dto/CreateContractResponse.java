package br.com.creditcontract.adapter.in.rest.dto;

import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;

/**
 * Response body for {@code POST /api/contracts}.
 *
 * <p>DTOs are plain data carriers — no domain logic, no validation beyond
 * null-safety. The controller maps the domain aggregate into this shape.
 */
public record CreateContractResponse(
		String id,
		String contractNumber,
		String clientName,
		String status,
		String currency,
		String creditLimit,
		LocalDateTime createdAt,
		Long version
) {

	public static CreateContractResponse from(
			String id,
			String contractNumber,
			String clientName,
			String status,
			MonetaryAmount creditLimit,
			LocalDateTime createdAt,
			Long version) {
		return new CreateContractResponse(
				id,
				contractNumber,
				clientName,
				status,
				creditLimit.currency(),
				creditLimit.amount().toPlainString(),
				createdAt,
				version
		);
	}
}
