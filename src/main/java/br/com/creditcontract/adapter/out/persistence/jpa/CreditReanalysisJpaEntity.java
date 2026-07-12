package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.entity.CreditReanalysis;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_reanalyses")
public class CreditReanalysisJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "contract_id", nullable = false)
	private CreditContractJpaEntity contract;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CreditReanalysisStatus status;

	@Column(name = "previous_limit", nullable = false, precision = 19, scale = 2)
	private BigDecimal previousLimit;

	@Column(name = "new_limit", precision = 19, scale = 2)
	private BigDecimal newLimit;

	@Column(length = 255)
	private String reason;

	@Column(name = "requested_at", nullable = false)
	private LocalDateTime requestedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	protected CreditReanalysisJpaEntity() {
	}

	CreditReanalysisJpaEntity(
			UUID id,
			CreditReanalysisStatus status,
			BigDecimal previousLimit,
			BigDecimal newLimit,
			String reason,
			LocalDateTime requestedAt,
			LocalDateTime completedAt) {
		this.id = id;
		this.status = status;
		this.previousLimit = previousLimit;
		this.newLimit = newLimit;
		this.reason = reason;
		this.requestedAt = requestedAt;
		this.completedAt = completedAt;
	}

	void attachTo(CreditContractJpaEntity contract) {
		this.contract = contract;
	}

	void updateFrom(CreditReanalysis reanalysis) {
		this.status = reanalysis.getStatus();
		this.newLimit = reanalysis.getNewLimit() == null
				? null : reanalysis.getNewLimit().amount();
		this.reason = reanalysis.getReason();
		this.completedAt = reanalysis.getCompletedAt();
	}

	public UUID getId() { return id; }
	public CreditReanalysisStatus getStatus() { return status; }
	public BigDecimal getPreviousLimit() { return previousLimit; }
	public BigDecimal getNewLimit() { return newLimit; }
	public String getReason() { return reason; }
	public LocalDateTime getRequestedAt() { return requestedAt; }
	public LocalDateTime getCompletedAt() { return completedAt; }
}
