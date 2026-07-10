package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.domain.valueobject.DocumentNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StubCreditLimitProviderTest {

	private final StubCreditLimitProvider provider = new StubCreditLimitProvider();

	@ParameterizedTest
	@CsvSource({
			"10000000280, 1000.00",
			"10000000361, 1000.00",
			"10000000442, 2500.00",
			"10000000523, 2500.00",
			"10000000604, 5000.00",
			"10000000795, 5000.00",
			"10000000876, 7500.00",
			"10000000957, 7500.00",
			"10000000108, 10000.00",
			"10000000019, 15000.00"
	})
	void shouldAssignLimitAccordingToFinalDocumentDigit(String documentNumber, BigDecimal expectedLimit) {
		assertEquals(expectedLimit, provider.getLimitFor(DocumentNumber.from(documentNumber)).amount());
	}

	@Test
	void shouldSupportFormattedDocumentNumber() {
		assertEquals(new BigDecimal("5000.00"),
				provider.getLimitFor(DocumentNumber.from("529.982.247-25")).amount());
	}
}
