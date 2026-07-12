package br.com.creditcontract.application.query;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.ContractId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Lightweight contract projection for collection reads. */
public record CreditContractSummary(
		ContractId id,
		String contractNumber,
		String clientName,
		ContractStatus status,
		BigDecimal creditLimit,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long version) {

	public CreditContractSummary {
		Objects.requireNonNull(id, "contract id is required");
		Objects.requireNonNull(contractNumber, "contract number is required");
		Objects.requireNonNull(clientName, "client name is required");
		Objects.requireNonNull(status, "contract status is required");
		Objects.requireNonNull(createdAt, "creation date is required");
		Objects.requireNonNull(updatedAt, "update date is required");
	}
}
