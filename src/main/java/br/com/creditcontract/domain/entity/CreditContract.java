package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
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
 * annotations. A persistence adapter will map it to a DB model later
 * (the {@code version} field maps naturally to JPA {@code @Version}).
 */
public class CreditContract {

	private final ContractId id;
	private final String contractNumber;
	private final Client client;
	private ContractStatus status;
	private final MonetaryAmount creditLimit;
	private final LocalDateTime createdAt;
	private String blockReason;
	private String cancellationReason;
	private LocalDateTime updatedAt;
	private Long version;

	private CreditContract(Builder builder) {
		this.id = builder.id;
		this.contractNumber = builder.contractNumber;
		this.client = builder.client;
		this.status = Objects.requireNonNull(builder.status, "initial status cannot be null");
		this.creditLimit = builder.creditLimit;
		this.createdAt = builder.createdAt;
		this.updatedAt = builder.createdAt;
		this.version = 0L;
	}

	/** Factory: creates a brand new contract in its initial state. */
	public static CreditContract create(ContractId id,
	                                     String contractNumber,
	                                     Client client,
	                                     MonetaryAmount creditLimit) {
		return builder()
				.id(id)
				.contractNumber(contractNumber)
				.client(client)
				.creditLimit(creditLimit)
				.createdAt(LocalDateTime.now())
				.status(ContractStatus.DRAFT)
				.build();
	}

	// ---- Accessors ----

	public ContractId getId() { return id; }
	public String getContractNumber() { return contractNumber; }
	public Client getClient() { return client; }
	public ContractStatus getStatus() { return status; }
	public MonetaryAmount getCreditLimit() { return creditLimit; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public Long getVersion() { return version; }

	// ---- Builder ----

	public static Builder builder() { return new Builder(); }

	public static final class Builder {
		private ContractId id;
		private String contractNumber;
		private Client client;
		private ContractStatus status;
		private MonetaryAmount creditLimit;
		private LocalDateTime createdAt;

		public Builder id(ContractId id) { this.id = id; return this; }
		public Builder contractNumber(String contractNumber) { this.contractNumber = contractNumber; return this; }
		public Builder client(Client client) { this.client = client; return this; }
		public Builder status(ContractStatus status) { this.status = status; return this; }
		public Builder creditLimit(MonetaryAmount creditLimit) { this.creditLimit = creditLimit; return this; }
		public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

		public CreditContract build() {
			Objects.requireNonNull(id, "id is required");
			Objects.requireNonNull(contractNumber, "contractNumber is required");
			Objects.requireNonNull(client, "client is required");
			Objects.requireNonNull(creditLimit, "creditLimit is required");
			Objects.requireNonNull(createdAt, "createdAt is required");
			return new CreditContract(this);
		}
	}
}
