package br.com.creditcontract.application.exception;

import br.com.creditcontract.domain.valueobject.ContractId;

/**
 * Signals that a command tried to persist a contract version that another
 * transaction had already replaced.
 */
public class ConcurrentCreditContractUpdateException extends RuntimeException {

	public ConcurrentCreditContractUpdateException(ContractId contractId, Throwable cause) {
		super("credit contract was modified by another operation: " + contractId.asString(), cause);
	}
}
