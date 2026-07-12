package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.util.UUID;

/** External capability that reassesses an existing approved credit limit. */
public interface CreditReanalysisProvider {

	CreditAnalysisResult reanalyze(
			DocumentNumber documentNumber, MonetaryAmount currentLimit, UUID requestId);
}
