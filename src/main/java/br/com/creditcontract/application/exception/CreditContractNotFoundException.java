package br.com.creditcontract.application.exception;

import br.com.creditcontract.domain.valueobject.ContractId;

public class CreditContractNotFoundException extends RuntimeException {

	public CreditContractNotFoundException(ContractId contractId) {
		super("credit contract not found: " + contractId.asString());
	}
}
