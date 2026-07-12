package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Auditable child entity for one credit-limit reanalysis request. */
public class CreditReanalysis {

	private final UUID id;
	private final MonetaryAmount previousLimit;
	private final LocalDateTime requestedAt;
	private CreditReanalysisStatus status;
	private MonetaryAmount newLimit;
	private String reason;
	private LocalDateTime completedAt;

	private CreditReanalysis(
			UUID id,
			MonetaryAmount previousLimit,
			LocalDateTime requestedAt,
			CreditReanalysisStatus status,
			MonetaryAmount newLimit,
			String reason,
			LocalDateTime completedAt) {
		this.id = Objects.requireNonNull(id, "reanalysis id is required");
		this.previousLimit = Objects.requireNonNull(previousLimit, "previous limit is required");
		this.requestedAt = Objects.requireNonNull(requestedAt, "request date is required");
		this.status = Objects.requireNonNull(status, "reanalysis status is required");
		this.newLimit = newLimit;
		this.reason = reason;
		this.completedAt = completedAt;
		validateState();
	}

	public static CreditReanalysis request(
			UUID id, MonetaryAmount previousLimit, LocalDateTime requestedAt) {
		return new CreditReanalysis(
				id, previousLimit, requestedAt, CreditReanalysisStatus.REQUESTED,
				null, null, null);
	}

	public static CreditReanalysis rehydrate(
			UUID id,
			MonetaryAmount previousLimit,
			LocalDateTime requestedAt,
			CreditReanalysisStatus status,
			MonetaryAmount newLimit,
			String reason,
			LocalDateTime completedAt) {
		return new CreditReanalysis(
				id, previousLimit, requestedAt, status, newLimit, reason, completedAt);
	}

	void approve(MonetaryAmount approvedLimit, LocalDateTime completionDate) {
		requireRequested();
		Objects.requireNonNull(approvedLimit, "approved limit is required");
		if (approvedLimit.amount().compareTo(previousLimit.amount()) <= 0) {
			throw new IllegalArgumentException("reanalysis must increase the credit limit");
		}
		status = CreditReanalysisStatus.APPROVED;
		newLimit = approvedLimit;
		completedAt = Objects.requireNonNull(completionDate, "completion date is required");
	}

	void reject(String rejectionReason, MonetaryAmount retainedLimit, LocalDateTime completionDate) {
		requireRequested();
		Objects.requireNonNull(rejectionReason, "rejection reason is required");
		String normalizedReason = rejectionReason.trim();
		if (normalizedReason.isEmpty()) {
			throw new IllegalArgumentException("rejection reason cannot be blank");
		}
		if (normalizedReason.length() > 255) {
			throw new IllegalArgumentException("rejection reason cannot exceed 255 characters");
		}
		status = CreditReanalysisStatus.REJECTED;
		newLimit = Objects.requireNonNull(retainedLimit, "retained limit is required");
		reason = normalizedReason;
		completedAt = Objects.requireNonNull(completionDate, "completion date is required");
	}

	private void requireRequested() {
		if (status != CreditReanalysisStatus.REQUESTED) {
			throw new IllegalStateException("credit reanalysis is already complete");
		}
	}

	private void validateState() {
		if (status == CreditReanalysisStatus.REQUESTED
				&& (newLimit != null || reason != null || completedAt != null)) {
			throw new IllegalArgumentException("requested reanalysis cannot have an outcome");
		}
		if (status == CreditReanalysisStatus.APPROVED
				&& (newLimit == null || reason != null || completedAt == null
				|| newLimit.amount().compareTo(previousLimit.amount()) <= 0)) {
			throw new IllegalArgumentException("approved reanalysis requires an increased limit");
		}
		if (status == CreditReanalysisStatus.REJECTED
				&& (newLimit == null || reason == null || reason.isBlank() || completedAt == null)) {
			throw new IllegalArgumentException("rejected reanalysis requires its retained limit and reason");
		}
	}

	public UUID getId() { return id; }
	public MonetaryAmount getPreviousLimit() { return previousLimit; }
	public LocalDateTime getRequestedAt() { return requestedAt; }
	public CreditReanalysisStatus getStatus() { return status; }
	public MonetaryAmount getNewLimit() { return newLimit; }
	public String getReason() { return reason; }
	public LocalDateTime getCompletedAt() { return completedAt; }
}
