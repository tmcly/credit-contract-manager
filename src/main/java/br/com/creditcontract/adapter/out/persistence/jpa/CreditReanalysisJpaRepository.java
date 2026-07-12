package br.com.creditcontract.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CreditReanalysisJpaRepository extends JpaRepository<CreditReanalysisJpaEntity, UUID> {

	Page<CreditReanalysisJpaEntity> findByContractId(UUID contractId, Pageable pageable);
}
