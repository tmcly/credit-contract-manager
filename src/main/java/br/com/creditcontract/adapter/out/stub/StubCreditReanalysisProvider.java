package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.application.port.out.CreditReanalysisProvider;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Deterministic local substitute for an existing-limit reanalysis engine. */
@Component
public class StubCreditReanalysisProvider implements CreditReanalysisProvider {

	public static final String POLICY_REJECTION_REASON = "Credit reanalysis policy criteria not met";
	public static final String MAXIMUM_LIMIT_REASON = "Maximum supported credit limit reached";
	static final BigDecimal MAXIMUM_LIMIT = new BigDecimal("100000.00");

	@Override
	public CreditAnalysisResult reanalyze(
			DocumentNumber documentNumber, MonetaryAmount currentLimit, UUID requestId) {
		Objects.requireNonNull(documentNumber, "document number is required");
		Objects.requireNonNull(currentLimit, "current limit is required");
		Objects.requireNonNull(requestId, "request id is required");
		if (currentLimit.amount().compareTo(MAXIMUM_LIMIT) >= 0) {
			return CreditAnalysisResult.rejected(MAXIMUM_LIMIT_REASON);
		}
		BigDecimal multiplier = switch (documentNumber.finalDigit()) {
			case 0, 1 -> null;
			case 2, 3, 4 -> new BigDecimal("1.5");
			case 5, 6, 7 -> new BigDecimal("2");
			case 8, 9 -> new BigDecimal("3");
			default -> throw new IllegalStateException("unexpected document digit");
		};
		if (multiplier == null) {
			return CreditAnalysisResult.rejected(POLICY_REJECTION_REASON);
		}
		BigDecimal increased = currentLimit.amount().multiply(multiplier).min(MAXIMUM_LIMIT);
		return CreditAnalysisResult.approved(MonetaryAmount.reais(increased));
	}
}
