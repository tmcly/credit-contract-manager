package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enumeration.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ClientId;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root representing a credit contract.
 *
 * <p>Domain invariants:
 * <ul>
 *   <li>A contract is born via {@link #create(...)} in its initial state.</li>
 *   <li>Blocking / cancellation only make sense from specific states
 *       (enforced here, not by the enum).</li>
 *   <li>Every meaningful change bumps {@code version} and refreshes
 *       {@code lastUpdateDate} — this is what gave the original system
 *       contract traceability (versioned contracts).</li>
 * </ul>
 *
 * <p>This class is persistence-agnostic: no JPA, no Spring, no database
 * annotations. The infrastructure layer will map it to a DB model later
 * (the {@code version} field maps naturally to JPA {@code @Version}).
 */
public class CreditContract {

	private final ContractId id;
	private final String contractNumber;
	private final ClientId clientId;
	private final Client client;
	private ContractStatus status;
	private final MonetaryAmount creditLimit;
	private final LocalDateTime creditAnalysisDate;
	private final LocalDateTime creationDate;

	private String blockReason;
	private String cancellationReason;
	private LocalDateTime lastUpdateDate;
	private Long version;

	private CreditContract(Builder builder) {
		this.id = builder.id;
		this.contractNumber = builder.contractNumber;
		this.clientId = Objects.requireNonNull(builder.clientId, "clientId is required");
		this.client = builder.client;
		this.status = Objects.requireNonNull(builder.status, "initial status cannot be null");
		this.creditLimit = builder.creditLimit;
		this.creditAnalysisDate = builder.creditAnalysisDate;
		this.creationDate = builder.creationDate;
		this.version = 0L;
		this.lastUpdateDate = builder.creationDate;
	}

	/** Factory: creates a brand new contract in its initial state. */
	public static CreditContract create(ContractId id,
	                                     ClientId clientId,
	                                     String contractNumber,
	                                     Client client,
	                                     MonetaryAmount creditLimit,
	                                     LocalDateTime creditAnalysisDate) {
		return builder()
				.id(id)
				.clientId(clientId)
				.contractNumber(contractNumber)
				.client(client)
				.creditLimit(creditLimit)
				.creditAnalysisDate(creditAnalysisDate)
				.creationDate(LocalDateTime.now())
				.status(ContractStatus.DRAFT)
				.build();
	}

	// ---- State transitions (the contract "state machine") ----

	public void block(String reason) {
		if (this.status == ContractStatus.CANCELLED) {
			throw new IllegalStateException("Cannot block a cancelled contract");
		}
		if (this.status == ContractStatus.BLOCKED) {
			throw new IllegalStateException("Contract is already blocked");
		}
		this.blockReason = Objects.requireNonNull(reason, "block reason is required");
		this.status = ContractStatus.BLOCKED;
		this.touch();
	}

	public void cancel(String reason) {
		if (this.status == ContractStatus.CANCELLED) {
			throw new IllegalStateException("Contract is already cancelled");
		}
		this.cancellationReason = Objects.requireNonNull(reason, "cancellation reason is required");
		this.status = ContractStatus.CANCELLED;
		this.touch();
	}

	/** Bumps version + audit timestamp on every meaningful change. */
	private void touch() {
		this.version += 1;
		this.lastUpdateDate = LocalDateTime.now();
	}

	// ---- Accessors ----

	public ContractId getId() { return id; }
	public String getContractNumber() { return contractNumber; }
	public ClientId getClientId() { return clientId; }
	public Client getClient() { return client; }
	public ContractStatus getStatus() { return status; }
	public MonetaryAmount getCreditLimit() { return creditLimit; }
	public LocalDateTime getCreditAnalysisDate() { return creditAnalysisDate; }
	public LocalDateTime getCreationDate() { return creationDate; }
	public String getBlockReason() { return blockReason; }
	public String getCancellationReason() { return cancellationReason; }
	public LocalDateTime getLastUpdateDate() { return lastUpdateDate; }
	public Long getVersion() { return version; }

	// ---- Builder ----

	public static Builder builder() { return new Builder(); }

	public static final class Builder {
		private ContractId id;
		private String contractNumber;
		private ClientId clientId;
		private Client client;
		private ContractStatus status;
		private MonetaryAmount creditLimit;
		private LocalDateTime creditAnalysisDate;
		private LocalDateTime creationDate;

		public Builder id(ContractId id) { this.id = id; return this; }
		public Builder contractNumber(String contractNumber) { this.contractNumber = contractNumber; return this; }
		public Builder clientId(ClientId clientId) { this.clientId = clientId; return this; }
		public Builder client(Client client) { this.client = client; return this; }
		public Builder status(ContractStatus status) { this.status = status; return this; }
		public Builder creditLimit(MonetaryAmount creditLimit) { this.creditLimit = creditLimit; return this; }
		public Builder creditAnalysisDate(LocalDateTime creditAnalysisDate) { this.creditAnalysisDate = creditAnalysisDate; return this; }
		public Builder creationDate(LocalDateTime creationDate) { this.creationDate = creationDate; return this; }

		public CreditContract build() {
			Objects.requireNonNull(id, "id is required");
			Objects.requireNonNull(contractNumber, "contractNumber is required");
			Objects.requireNonNull(clientId, "clientId is required");
			Objects.requireNonNull(client, "client is required");
			Objects.requireNonNull(creditLimit, "creditLimit is required");
			Objects.requireNonNull(creditAnalysisDate, "creditAnalysisDate is required");
			Objects.requireNonNull(creationDate, "creationDate is required");
			return new CreditContract(this);
		}
	}
}
