package br.com.creditcontract.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary amount in Brazilian reais. Immutable.
 *
 * Encapsulates the basic invariants so they can never be violated anywhere
 * in the system: not null, not negative, fixed scale (2 decimals).
 */
public record MonetaryAmount(BigDecimal amount) {

	public MonetaryAmount {
		if (amount == null) {
			throw new NullPointerException("amount cannot be null");
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("monetary amount cannot be negative");
		}
		amount = amount.setScale(2, RoundingMode.HALF_UP);
	}

	public static MonetaryAmount reais(BigDecimal amount) {
		return new MonetaryAmount(amount);
	}
}
