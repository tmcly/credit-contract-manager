package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Persistence-only projection that avoids loading the aggregate and client address. */
public record CreditContractSummaryJpaProjection(
		UUID id,
		String contractNumber,
		String clientName,
		ContractStatus status,
		BigDecimal creditLimit,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		Long version) {
}
