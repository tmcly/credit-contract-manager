package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Repository
public class CreditContractPersistenceAdapter implements CreditContractRepository {

	private final CreditContractJpaRepository repository;
	private final CreditContractPersistenceMapper mapper;

	public CreditContractPersistenceAdapter(CreditContractJpaRepository repository,
	                                        CreditContractPersistenceMapper mapper) {
		this.repository = Objects.requireNonNull(repository);
		this.mapper = Objects.requireNonNull(mapper);
	}

	@Override
	@Transactional
	public void save(CreditContract contract) {
		repository.save(mapper.toJpaEntity(contract));
	}
}
