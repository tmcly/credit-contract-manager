package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventPersistenceAdapter;
import br.com.creditcontract.application.exception.ConcurrentCreditContractUpdateException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.DomainEvent;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class CreditContractPersistenceAdapter implements CreditContractRepository {

	private final CreditContractJpaRepository repository;
	private final CreditContractPersistenceMapper mapper;
	private final OutboxEventPersistenceAdapter outboxEventPersistenceAdapter;

	public CreditContractPersistenceAdapter(CreditContractJpaRepository repository,
	                                        CreditContractPersistenceMapper mapper,
	                                        OutboxEventPersistenceAdapter outboxEventPersistenceAdapter) {
		this.repository = Objects.requireNonNull(repository);
		this.mapper = Objects.requireNonNull(mapper);
		this.outboxEventPersistenceAdapter = Objects.requireNonNull(outboxEventPersistenceAdapter);
	}

	@Override
	@Transactional
	public void save(CreditContract contract) {
		List<DomainEvent> eventsToPersist = List.copyOf(contract.getDomainEvents());

		CreditContractJpaEntity entity = repository.findDetailedById(contract.getId().value())
				.map(existing -> {
					mapper.updateJpaEntity(contract, existing);
					return existing;
				})
				.orElseGet(() -> mapper.toJpaEntity(contract));

		try {
			repository.saveAndFlush(entity);
		} catch (OptimisticLockingFailureException exception) {
			throw new ConcurrentCreditContractUpdateException(contract.getId(), exception);
		}
		outboxEventPersistenceAdapter.persist(eventsToPersist);
		clearPersistedEventsAfterCommit(contract, eventsToPersist);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CreditContract> findById(ContractId contractId) {
		return repository.findDetailedById(contractId.value()).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CreditContract> findBlockedUpdatedBefore(LocalDateTime cutoff, int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("limit must be positive");
		}
		return repository.findIdsByStatusUpdatedBefore(
				ContractStatus.BLOCKED, Objects.requireNonNull(cutoff), PageRequest.of(0, limit))
				.stream()
				.map(repository::findDetailedById)
				.flatMap(Optional::stream)
				.map(mapper::toDomain)
				.toList();
	}

	private void clearPersistedEventsAfterCommit(
			CreditContract contract,
			List<DomainEvent> persistedEvents) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				contract.markDomainEventsCommitted(persistedEvents);
			}
		});
	}
}
