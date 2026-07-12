package br.com.creditcontract.application.query;

import br.com.creditcontract.domain.enums.ContractStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Read projection for one auditable contract-status transition. */
public record ContractStatusHistoryItem(
		UUID id,
		ContractStatus previousStatus,
		ContractStatus newStatus,
		String reason,
		LocalDateTime changedAt) {

	public ContractStatusHistoryItem {
		Objects.requireNonNull(id, "history id is required");
		Objects.requireNonNull(newStatus, "new status is required");
		Objects.requireNonNull(changedAt, "change date is required");
	}
}
