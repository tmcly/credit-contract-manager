package br.com.creditcontract.application.query;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.DocumentNumber;

/** Optional exact-match filters for the paginated contract read model. */
public record CreditContractSearchCriteria(
		ContractStatus status,
		DocumentNumber documentNumber,
		String contractNumber) {

	public CreditContractSearchCriteria {
		if (contractNumber != null) {
			contractNumber = contractNumber.trim();
			if (contractNumber.isEmpty()) {
				contractNumber = null;
			} else if (contractNumber.length() > 30) {
				throw new IllegalArgumentException("contractNumber cannot exceed 30 characters");
			}
		}
	}
}
