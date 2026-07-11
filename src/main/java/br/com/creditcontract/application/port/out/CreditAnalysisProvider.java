package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.valueobject.DocumentNumber;

/** External capability that decides whether a client receives credit. */
public interface CreditAnalysisProvider {

	CreditAnalysisResult analyze(DocumentNumber documentNumber);
}
