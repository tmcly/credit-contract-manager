package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.ContractId;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Persists credit-contract aggregates and their pending domain events without
 * exposing persistence or outbox technology to application and domain code.
 */
public interface CreditContractRepository {

	void save(CreditContract contract);

	Optional<CreditContract> findById(ContractId contractId);

	List<CreditContract> findBlockedUpdatedBefore(LocalDateTime cutoff, int limit);
}
