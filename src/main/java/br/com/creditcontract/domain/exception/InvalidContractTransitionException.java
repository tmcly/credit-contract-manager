package br.com.creditcontract.domain.exception;

import br.com.creditcontract.domain.enums.ContractStatus;

public class InvalidContractTransitionException extends RuntimeException {

	public InvalidContractTransitionException(ContractStatus current, ContractStatus target) {
		super("credit contract cannot transition from " + current + " to " + target);
	}
}
