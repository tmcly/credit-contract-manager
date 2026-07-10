package br.com.creditcontract.adapter.out.stub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StubCreditLimitProviderTest {

	private final StubCreditLimitProvider provider = new StubCreditLimitProvider();

	@ParameterizedTest
	@CsvSource({
			"12345678900, 1000.00",
			"12345678901, 1000.00",
			"12345678902, 2500.00",
			"12345678903, 2500.00",
			"12345678904, 5000.00",
			"12345678905, 5000.00",
			"12345678906, 7500.00",
			"12345678907, 7500.00",
			"12345678908, 10000.00",
			"12345678909, 15000.00"
	})
	void shouldAssignLimitAccordingToFinalDocumentDigit(String documentNumber, BigDecimal expectedLimit) {
		assertEquals(expectedLimit, provider.getLimitFor(documentNumber).amount());
	}

	@Test
	void shouldSupportFormattedDocumentNumber() {
		assertEquals(new BigDecimal("15000.00"),
				provider.getLimitFor("123.456.789-09").amount());
	}

	@Test
	void shouldRejectDocumentWithoutDigits() {
		assertThrows(IllegalArgumentException.class, () -> provider.getLimitFor("invalid"));
	}
}
