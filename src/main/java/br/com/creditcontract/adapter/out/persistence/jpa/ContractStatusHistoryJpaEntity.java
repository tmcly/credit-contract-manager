package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.enums.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contract_status_history")
public class ContractStatusHistoryJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "contract_id", nullable = false)
	private CreditContractJpaEntity contract;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status", length = 30)
	private ContractStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 30)
	private ContractStatus newStatus;

	@Column(length = 255)
	private String reason;

	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;

	protected ContractStatusHistoryJpaEntity() {
	}

	ContractStatusHistoryJpaEntity(UUID id,
	                               ContractStatus previousStatus,
	                               ContractStatus newStatus,
	                               String reason,
	                               LocalDateTime changedAt) {
		this.id = id;
		this.previousStatus = previousStatus;
		this.newStatus = newStatus;
		this.reason = reason;
		this.changedAt = changedAt;
	}

	void attachTo(CreditContractJpaEntity contract) {
		this.contract = contract;
	}

	public UUID getId() { return id; }
	public ContractStatus getPreviousStatus() { return previousStatus; }
	public ContractStatus getNewStatus() { return newStatus; }
	public String getReason() { return reason; }
	public LocalDateTime getChangedAt() { return changedAt; }
}
