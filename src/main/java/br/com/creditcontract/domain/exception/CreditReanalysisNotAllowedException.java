package br.com.creditcontract.domain.exception;

import br.com.creditcontract.domain.enums.ContractStatus;

/** Raised when a reanalysis is requested for a non-active contract. */
public class CreditReanalysisNotAllowedException extends RuntimeException {

	public CreditReanalysisNotAllowedException(ContractStatus status) {
		super("credit reanalysis requires an ACTIVE contract, but contract is " + status);
	}
}
