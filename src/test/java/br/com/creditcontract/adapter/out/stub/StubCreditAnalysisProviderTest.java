package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.CreditAnalysisResult;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StubCreditAnalysisProviderTest {

	private final StubCreditAnalysisProvider provider = new StubCreditAnalysisProvider();

	@ParameterizedTest
	@CsvSource({"10000000280", "10000000361"})
	void shouldRejectCpfEndingsZeroAndOne(String documentNumber) {
		CreditAnalysisResult.Rejected result = assertInstanceOf(
				CreditAnalysisResult.Rejected.class,
				provider.analyze(DocumentNumber.from(documentNumber)));
		assertEquals(StubCreditAnalysisProvider.REJECTION_REASON, result.reason());
	}

	@ParameterizedTest
	@CsvSource({
			"10000000442, 2500.00",
			"10000000523, 2500.00",
			"10000000604, 5000.00",
			"10000000795, 5000.00",
			"10000000876, 7500.00",
			"10000000957, 7500.00",
			"10000000108, 10000.00",
			"10000000019, 15000.00"
	})
	void shouldApproveOtherCpfEndingsWithDeterministicLimit(
			String documentNumber,
			BigDecimal expectedLimit) {
		CreditAnalysisResult.Approved result = assertInstanceOf(
				CreditAnalysisResult.Approved.class,
				provider.analyze(DocumentNumber.from(documentNumber)));
		assertEquals(expectedLimit, result.limit().amount());
	}
}
