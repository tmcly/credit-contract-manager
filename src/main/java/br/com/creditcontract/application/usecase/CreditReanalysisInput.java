package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.util.Objects;

/** Stable provider input captured from the pending aggregate request. */
public record CreditReanalysisInput(
		DocumentNumber documentNumber,
		MonetaryAmount currentLimit) {

	public CreditReanalysisInput {
		Objects.requireNonNull(documentNumber, "document number is required");
		Objects.requireNonNull(currentLimit, "current limit is required");
	}
}
