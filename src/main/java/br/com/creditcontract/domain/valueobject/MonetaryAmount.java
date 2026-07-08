package br.com.creditcontract.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monetary amount as a value object (amount + currency). Immutable.
 *
 * Encapsulates the basic invariants so they can never be violated anywhere
 * in the system: not null, not negative, fixed scale (2 decimals).
 */
public record MonetaryAmount(BigDecimal amount, String currency) {

	public MonetaryAmount {
		Objects.requireNonNull(amount, "amount cannot be null");
		Objects.requireNonNull(currency, "currency cannot be null");
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("monetary amount cannot be negative");
		}
		amount = amount.setScale(2, RoundingMode.HALF_UP);
	}

	public static MonetaryAmount of(BigDecimal amount, String currency) {
		return new MonetaryAmount(amount, currency);
	}

	public static MonetaryAmount reais(BigDecimal amount) {
		return new MonetaryAmount(amount, "BRL");
	}
}
