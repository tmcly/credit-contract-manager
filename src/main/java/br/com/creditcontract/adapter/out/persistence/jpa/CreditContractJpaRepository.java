package br.com.creditcontract.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditContractJpaRepository extends JpaRepository<CreditContractJpaEntity, UUID> {
}
