package br.com.creditcontract.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CreditContractJpaRepository extends JpaRepository<CreditContractJpaEntity, UUID> {

	@EntityGraph(attributePaths = "statusHistory")
	@Query("select contract from CreditContractJpaEntity contract where contract.id = :id")
	Optional<CreditContractJpaEntity> findDetailedById(UUID id);
}
