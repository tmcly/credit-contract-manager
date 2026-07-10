package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.entity.CreditContract;

/**
 * Persists credit-contract aggregates without exposing persistence technology
 * to the application or domain layers.
 */
public interface CreditContractRepository {

	void save(CreditContract contract);
}
