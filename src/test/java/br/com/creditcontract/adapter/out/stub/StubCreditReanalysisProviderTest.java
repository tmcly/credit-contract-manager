package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StubCreditReanalysisProviderTest {

	private final StubCreditReanalysisProvider provider = new StubCreditReanalysisProvider();

	@ParameterizedTest
	@CsvSource({
			"10000000280, 5000.00, REJECTED",
			"10000000361, 5000.00, REJECTED",
			"10000000442, 5000.00, 7500.00",
			"10000000795, 5000.00, 10000.00",
			"10000000019, 5000.00, 15000.00"
	})
	void shouldApplyDeterministicCpfBands(
			String document, String currentLimit, String expected) {
		CreditAnalysisResult result = provider.reanalyze(
				DocumentNumber.from(document), money(currentLimit), UUID.randomUUID());
		if ("REJECTED".equals(expected)) {
			CreditAnalysisResult.Rejected rejected = assertInstanceOf(
					CreditAnalysisResult.Rejected.class, result);
			assertEquals(StubCreditReanalysisProvider.POLICY_REJECTION_REASON, rejected.reason());
		} else {
			CreditAnalysisResult.Approved approved = assertInstanceOf(
					CreditAnalysisResult.Approved.class, result);
			assertEquals(new BigDecimal(expected), approved.limit().amount());
		}
	}

	@Test
	void shouldCapGrowthAndRejectWhenAlreadyAtMaximum() {
		CreditAnalysisResult.Approved capped = assertInstanceOf(
				CreditAnalysisResult.Approved.class,
				provider.reanalyze(DocumentNumber.from("10000000019"),
						money("50000.00"), UUID.randomUUID()));
		assertEquals(new BigDecimal("100000.00"), capped.limit().amount());

		CreditAnalysisResult.Rejected rejected = assertInstanceOf(
				CreditAnalysisResult.Rejected.class,
				provider.reanalyze(DocumentNumber.from("10000000019"),
						money("100000.00"), UUID.randomUUID()));
		assertEquals(StubCreditReanalysisProvider.MAXIMUM_LIMIT_REASON, rejected.reason());
	}

	private MonetaryAmount money(String amount) {
		return MonetaryAmount.reais(new BigDecimal(amount));
	}
}
