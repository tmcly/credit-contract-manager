package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.DomainEvent;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 *   <li>New business facts are recorded as domain events and remain pending
 *       until an adapter confirms that their transaction committed.</li>
 * </ul>
 *
 * <p>This class is persistence-agnostic: no JPA, no Spring, no database
 * annotations. Outbound adapters map it to infrastructure models.
 */
public class CreditContract {

	private final ContractId id;
	private final String contractNumber;
	private final Client client;
	private ContractStatus status;
	private final MonetaryAmount creditLimit;
	private final LocalDateTime createdAt;
	private final List<ContractStatusHistory> statusHistory;
	private final List<DomainEvent> domainEvents;
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
		this.statusHistory = new ArrayList<>();
		this.statusHistory.add(ContractStatusHistory.initial(this.status, this.createdAt));
		this.domainEvents = new ArrayList<>();
	}

	/** Factory: creates a brand new contract in its initial state. */
	public static CreditContract create(ContractId id,
	                                     String contractNumber,
	                                     Client client,
	                                     MonetaryAmount creditLimit) {
		CreditContract contract = builder()
				.id(id)
				.contractNumber(contractNumber)
				.client(client)
				.creditLimit(creditLimit)
				.createdAt(LocalDateTime.now())
				.status(ContractStatus.DRAFT)
				.build();
		contract.domainEvents.add(CreditContractCreated.initial(
				contract.id,
				contract.contractNumber,
				contract.client.documentNumber(),
				contract.createdAt));
		return contract;
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
	public List<ContractStatusHistory> getStatusHistory() {
		return Collections.unmodifiableList(statusHistory);
	}
	public List<DomainEvent> getDomainEvents() {
		return Collections.unmodifiableList(domainEvents);
	}

	/** Removes only events confirmed in a successfully committed transaction. */
	public void markDomainEventsCommitted(List<DomainEvent> committedEvents) {
		domainEvents.removeAll(List.copyOf(committedEvents));
	}

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
