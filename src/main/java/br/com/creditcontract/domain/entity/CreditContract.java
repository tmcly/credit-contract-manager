package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditAnalysisApproved;
import br.com.creditcontract.domain.event.CreditAnalysisRejected;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.CreditContractAccepted;
import br.com.creditcontract.domain.event.DomainEvent;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.exception.InvalidContractTransitionException;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
	private MonetaryAmount creditLimit;
	private final LocalDateTime createdAt;
	private final List<ContractStatusHistory> statusHistory;
	private final List<DomainEvent> domainEvents;
	private LocalDateTime updatedAt;
	private Long version;

	private CreditContract(Builder builder) {
		this.id = builder.id;
		this.contractNumber = builder.contractNumber;
		this.client = builder.client;
		this.status = Objects.requireNonNull(builder.status, "status cannot be null");
		this.creditLimit = builder.creditLimit;
		this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt is required");
		this.updatedAt = builder.updatedAt == null ? builder.createdAt : builder.updatedAt;
		this.version = builder.version == null ? 0L : builder.version;
		this.statusHistory = builder.statusHistory == null
				? new ArrayList<>()
				: new ArrayList<>(builder.statusHistory);
		if (this.statusHistory.isEmpty()) {
			this.statusHistory.add(ContractStatusHistory.initial(this.status, this.createdAt));
		}
		this.domainEvents = new ArrayList<>();
		validateCreditLimitForStatus();
	}

	/** Factory: creates a brand new contract in its initial state. */
	public static CreditContract create(ContractId id,
	                                     String contractNumber,
	                                     Client client) {
		CreditContract contract = newContract(id, contractNumber, client);
		contract.domainEvents.add(CreditContractCreated.initial(
				contract.id,
				contract.contractNumber,
				contract.client.documentNumber(),
				contract.createdAt));
		return contract;
	}

	public static CreditContract create(ContractId id,
	                                     String contractNumber,
	                                     Client client,
	                                     UUID correlationId) {
		CreditContract contract = newContract(id, contractNumber, client);
		contract.domainEvents.add(CreditContractCreated.create(
				contract.id,
				contract.contractNumber,
				contract.client.documentNumber(),
				contract.createdAt,
				correlationId));
		return contract;
	}

	private static CreditContract newContract(
			ContractId id,
			String contractNumber,
			Client client) {
		return builder()
				.id(id)
				.contractNumber(contractNumber)
				.client(client)
				.createdAt(LocalDateTime.now())
				.status(ContractStatus.DRAFT)
				.build();
	}

	/** Rebuilds a persisted aggregate without creating new history or events. */
	public static CreditContract rehydrate(
			ContractId id,
			String contractNumber,
			Client client,
			ContractStatus status,
			MonetaryAmount creditLimit,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			Long version,
			List<ContractStatusHistory> statusHistory) {
		return builder()
				.id(id)
				.contractNumber(contractNumber)
				.client(client)
				.status(status)
				.creditLimit(creditLimit)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.version(version)
				.statusHistory(statusHistory)
				.build();
	}

	public void startCreditAnalysis() {
		requireStatus(ContractStatus.DRAFT, ContractStatus.UNDER_REVIEW);
		transitionTo(ContractStatus.UNDER_REVIEW, "Credit analysis requested");
	}

	public void approveCreditAnalysis(MonetaryAmount approvedLimit, EventContext context) {
		requireStatus(ContractStatus.UNDER_REVIEW, ContractStatus.APPROVED);
		Objects.requireNonNull(approvedLimit, "approved limit is required");
		Objects.requireNonNull(context, "event context is required");
		if (approvedLimit.amount().signum() <= 0) {
			throw new IllegalArgumentException("approved limit must be positive");
		}
		this.creditLimit = approvedLimit;
		transitionTo(ContractStatus.APPROVED, "Credit analysis approved");
		domainEvents.add(CreditAnalysisApproved.create(id, approvedLimit, updatedAt, context));
	}

	public void rejectCreditAnalysis(String reason, EventContext context) {
		requireStatus(ContractStatus.UNDER_REVIEW, ContractStatus.REJECTED);
		Objects.requireNonNull(reason, "rejection reason is required");
		Objects.requireNonNull(context, "event context is required");
		if (reason.isBlank()) {
			throw new IllegalArgumentException("rejection reason cannot be blank");
		}
		this.creditLimit = null;
		transitionTo(ContractStatus.REJECTED, reason);
		domainEvents.add(CreditAnalysisRejected.create(id, reason, updatedAt, context));
	}

	public void accept(UUID correlationId) {
		requireStatus(ContractStatus.APPROVED, ContractStatus.ACCEPTED);
		Objects.requireNonNull(correlationId, "correlation id is required");
		transitionTo(ContractStatus.ACCEPTED, "Contract accepted by client");
		domainEvents.add(CreditContractAccepted.create(id, updatedAt, correlationId));
	}

	public boolean hasCreditAnalysisFinished() {
		return status == ContractStatus.APPROVED
				|| status == ContractStatus.REJECTED
				|| status == ContractStatus.ACCEPTED
				|| status == ContractStatus.ACTIVE
				|| status == ContractStatus.BLOCKED
				|| status == ContractStatus.CANCELLED;
	}

	private void requireStatus(ContractStatus expected, ContractStatus target) {
		if (status != expected) {
			throw new InvalidContractTransitionException(status, target);
		}
	}

	private void transitionTo(ContractStatus target, String reason) {
		ContractStatus previous = status;
		status = target;
		updatedAt = LocalDateTime.now();
		version++;
		statusHistory.add(ContractStatusHistory.transition(previous, target, reason, updatedAt));
		validateCreditLimitForStatus();
	}

	private void validateCreditLimitForStatus() {
		if ((status == ContractStatus.DRAFT
				|| status == ContractStatus.UNDER_REVIEW
				|| status == ContractStatus.REJECTED)
				&& creditLimit != null) {
			throw new IllegalArgumentException(status + " contract cannot have a credit limit");
		}
		if ((status == ContractStatus.APPROVED
				|| status == ContractStatus.ACCEPTED
				|| status == ContractStatus.ACTIVE
				|| status == ContractStatus.BLOCKED)
				&& (creditLimit == null || creditLimit.amount().signum() <= 0)) {
			throw new IllegalArgumentException(status + " contract requires a positive credit limit");
		}
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
		private LocalDateTime updatedAt;
		private Long version;
		private List<ContractStatusHistory> statusHistory;

		public Builder id(ContractId id) { this.id = id; return this; }
		public Builder contractNumber(String contractNumber) { this.contractNumber = contractNumber; return this; }
		public Builder client(Client client) { this.client = client; return this; }
		public Builder status(ContractStatus status) { this.status = status; return this; }
		public Builder creditLimit(MonetaryAmount creditLimit) { this.creditLimit = creditLimit; return this; }
		public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
		public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
		public Builder version(Long version) { this.version = version; return this; }
		public Builder statusHistory(List<ContractStatusHistory> statusHistory) {
			this.statusHistory = statusHistory;
			return this;
		}

		public CreditContract build() {
			Objects.requireNonNull(id, "id is required");
			Objects.requireNonNull(contractNumber, "contractNumber is required");
			Objects.requireNonNull(client, "client is required");
			Objects.requireNonNull(createdAt, "createdAt is required");
			return new CreditContract(this);
		}
	}
}
