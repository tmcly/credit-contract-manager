package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.entity.CreditContract;

/**
 * Persists credit-contract aggregates and their pending domain events without
 * exposing persistence or outbox technology to application and domain code.
 */
public interface CreditContractRepository {

	void save(CreditContract contract);
}
