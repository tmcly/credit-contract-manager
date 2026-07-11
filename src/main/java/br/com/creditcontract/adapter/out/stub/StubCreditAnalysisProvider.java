package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.CreditAnalysisProvider;
import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Deterministic local substitute for a credit-analysis engine. */
@Component
public class StubCreditAnalysisProvider implements CreditAnalysisProvider {

	public static final String REJECTION_REASON = "Credit policy criteria not met";

	@Override
	public CreditAnalysisResult analyze(DocumentNumber documentNumber) {
		return switch (documentNumber.finalDigit()) {
			case 0, 1 -> CreditAnalysisResult.rejected(REJECTION_REASON);
			case 2, 3 -> approved("2500.00");
			case 4, 5 -> approved("5000.00");
			case 6, 7 -> approved("7500.00");
			case 8 -> approved("10000.00");
			case 9 -> approved("15000.00");
			default -> throw new IllegalStateException("unexpected document digit");
		};
	}

	private CreditAnalysisResult approved(String amount) {
		return CreditAnalysisResult.approved(MonetaryAmount.reais(new BigDecimal(amount)));
	}
}
