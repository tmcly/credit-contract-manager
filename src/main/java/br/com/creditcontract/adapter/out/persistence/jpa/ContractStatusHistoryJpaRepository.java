package br.com.creditcontract.adapter.out.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ContractStatusHistoryJpaRepository
		extends JpaRepository<ContractStatusHistoryJpaEntity, UUID> {

	Page<ContractStatusHistoryJpaEntity> findByContractId(UUID contractId, Pageable pageable);
}
