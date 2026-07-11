package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "credit_contracts")
public class CreditContractJpaEntity {

	@Id
	private UUID id;

	@Column(name = "contract_number", nullable = false, unique = true, length = 30)
	private String contractNumber;

	@Column(name = "client_document_number", nullable = false, length = 11)
	private String clientDocumentNumber;

	@Column(name = "client_name", nullable = false, length = 150)
	private String clientName;

	@Column(name = "client_state", nullable = false, length = 2)
	private String clientState;

	@Column(name = "client_city", nullable = false, length = 100)
	private String clientCity;

	@Column(name = "client_street", nullable = false, length = 150)
	private String clientStreet;

	@Column(name = "client_address_number", nullable = false, length = 20)
	private String clientAddressNumber;

	@Column(name = "client_zip_code", nullable = false, length = 8)
	private String clientZipCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ContractStatus status;

	@Column(name = "credit_limit", precision = 19, scale = 2)
	private BigDecimal creditLimit;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	@Column(nullable = false)
	private Long version;

	@OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("changedAt ASC")
	private List<ContractStatusHistoryJpaEntity> statusHistory = new ArrayList<>();

	protected CreditContractJpaEntity() {
	}

	CreditContractJpaEntity(UUID id,
	                        String contractNumber,
	                        String clientDocumentNumber,
	                        String clientName,
	                        String clientState,
	                        String clientCity,
	                        String clientStreet,
	                        String clientAddressNumber,
	                        String clientZipCode,
	                        ContractStatus status,
	                        BigDecimal creditLimit,
	                        LocalDateTime createdAt,
	                        LocalDateTime updatedAt) {
		this.id = id;
		this.contractNumber = contractNumber;
		this.clientDocumentNumber = clientDocumentNumber;
		this.clientName = clientName;
		this.clientState = clientState;
		this.clientCity = clientCity;
		this.clientStreet = clientStreet;
		this.clientAddressNumber = clientAddressNumber;
		this.clientZipCode = clientZipCode;
		this.status = status;
		this.creditLimit = creditLimit;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	void addStatusHistory(ContractStatusHistoryJpaEntity history) {
		history.attachTo(this);
		statusHistory.add(history);
	}

	void updateFrom(CreditContract contract) {
		this.status = contract.getStatus();
		this.creditLimit = contract.getCreditLimit() == null
				? null
				: contract.getCreditLimit().amount();
		this.updatedAt = contract.getUpdatedAt();
	}

	boolean hasStatusHistory(UUID historyId) {
		return statusHistory.stream().anyMatch(history -> history.getId().equals(historyId));
	}

	public UUID getId() { return id; }
	public String getContractNumber() { return contractNumber; }
	public String getClientDocumentNumber() { return clientDocumentNumber; }
	public String getClientName() { return clientName; }
	public String getClientState() { return clientState; }
	public String getClientCity() { return clientCity; }
	public String getClientStreet() { return clientStreet; }
	public String getClientAddressNumber() { return clientAddressNumber; }
	public String getClientZipCode() { return clientZipCode; }
	public ContractStatus getStatus() { return status; }
	public BigDecimal getCreditLimit() { return creditLimit; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public Long getVersion() { return version; }
	public List<ContractStatusHistoryJpaEntity> getStatusHistory() {
		return Collections.unmodifiableList(statusHistory);
	}
}
