package br.com.creditcontract.application.query;

import br.com.creditcontract.domain.enums.CreditReanalysisStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Read projection for one credit-reanalysis request and its eventual outcome. */
public record CreditReanalysisItem(
		UUID id,
		CreditReanalysisStatus status,
		BigDecimal previousLimit,
		BigDecimal newLimit,
		String reason,
		LocalDateTime requestedAt,
		LocalDateTime completedAt) {

	public CreditReanalysisItem {
		Objects.requireNonNull(id, "reanalysis id is required");
		Objects.requireNonNull(status, "reanalysis status is required");
		Objects.requireNonNull(previousLimit, "previous limit is required");
		Objects.requireNonNull(requestedAt, "request date is required");
	}
}
