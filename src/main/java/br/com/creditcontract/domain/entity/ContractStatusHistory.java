package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable record of a status transition inside the credit-contract aggregate.
 *
 * <p>The first entry has no previous status. Later transitions may carry a
 * single business reason, avoiding status-specific columns in the contract.
 */
public record ContractStatusHistory(
		UUID id,
		ContractStatus previousStatus,
		ContractStatus newStatus,
		String reason,
		LocalDateTime changedAt) {

	public ContractStatusHistory {
		Objects.requireNonNull(id, "status history id is required");
		Objects.requireNonNull(newStatus, "new status is required");
		Objects.requireNonNull(changedAt, "status change date is required");
		if (reason != null && reason.isBlank()) {
			reason = null;
		}
	}

	public static ContractStatusHistory initial(ContractStatus status, LocalDateTime changedAt) {
		return new ContractStatusHistory(UUID.randomUUID(), null, status, null, changedAt);
	}

	public static ContractStatusHistory transition(
			ContractStatus previousStatus,
			ContractStatus newStatus,
			String reason,
			LocalDateTime changedAt) {
		return new ContractStatusHistory(
				UUID.randomUUID(), previousStatus, newStatus, reason, changedAt);
	}
}
