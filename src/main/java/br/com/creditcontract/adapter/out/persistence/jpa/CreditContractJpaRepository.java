package br.com.creditcontract.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import br.com.creditcontract.domain.enums.ContractStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditContractJpaRepository extends JpaRepository<CreditContractJpaEntity, UUID> {

	@EntityGraph(attributePaths = "statusHistory")
	@Query("select contract from CreditContractJpaEntity contract where contract.id = :id")
	Optional<CreditContractJpaEntity> findDetailedById(UUID id);

	@Query("""
			select contract.id from CreditContractJpaEntity contract
			where contract.status = :status and contract.updatedAt <= :cutoff
			order by contract.updatedAt asc
			""")
	List<UUID> findIdsByStatusUpdatedBefore(
			ContractStatus status, LocalDateTime cutoff, Pageable pageable);
}
