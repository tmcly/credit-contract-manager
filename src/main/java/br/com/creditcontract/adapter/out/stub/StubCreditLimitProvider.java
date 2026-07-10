package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.domain.port.CreditLimitProvider;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stub that assigns a deterministic credit-limit band based on the final
 * digit of the client document. This keeps local responses varied while
 * returning the same limit whenever the same document is used.
 *
 * <p>In production this becomes a call to the risk-engine REST/queue API.
 */
@Component
public class StubCreditLimitProvider implements CreditLimitProvider {

	@Override
	public MonetaryAmount getLimitFor(String documentNumber) {
		int finalDigit = findFinalDigit(documentNumber);

		BigDecimal limit = switch (finalDigit) {
			case 0, 1 -> new BigDecimal("1000.00");
			case 2, 3 -> new BigDecimal("2500.00");
			case 4, 5 -> new BigDecimal("5000.00");
			case 6, 7 -> new BigDecimal("7500.00");
			case 8 -> new BigDecimal("10000.00");
			case 9 -> new BigDecimal("15000.00");
			default -> throw new IllegalStateException("unexpected document digit: " + finalDigit);
		};

		return MonetaryAmount.reais(limit);
	}

	private int findFinalDigit(String documentNumber) {
		if (documentNumber == null || documentNumber.isBlank()) {
			throw new IllegalArgumentException("documentNumber is required");
		}

		for (int index = documentNumber.length() - 1; index >= 0; index--) {
			char character = documentNumber.charAt(index);
			if (Character.isDigit(character)) {
				return Character.digit(character, 10);
			}
		}

		throw new IllegalArgumentException("documentNumber must contain at least one digit");
	}
}
