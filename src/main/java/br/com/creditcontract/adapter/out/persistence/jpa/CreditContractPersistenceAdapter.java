package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.adapter.out.persistence.outbox.OutboxEventPersistenceAdapter;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.event.DomainEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

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

		repository.saveAndFlush(mapper.toJpaEntity(contract));
		outboxEventPersistenceAdapter.persist(eventsToPersist);
		clearPersistedEventsAfterCommit(contract, eventsToPersist);
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
