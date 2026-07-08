package br.com.contratocredito.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monetary amount as a value object (amount + currency). Immutable.
 *
 * Encapsulates the basic invariants so they can never be violated
 * anywhere in the system: not null, not negative, fixed scale (2 decimals).
 */
public record ValorMonetario(BigDecimal valor, String moeda) {

	public ValorMonetario {
		Objects.requireNonNull(valor, "valor não pode ser nulo");
		Objects.requireNonNull(moeda, "moeda não pode ser nula");
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("valor monetário não pode ser negativo");
		}
		valor = valor.setScale(2, RoundingMode.HALF_UP);
	}

	public static ValorMonetario of(BigDecimal valor, String moeda) {
		return new ValorMonetario(valor, moeda);
	}

	public static ValorMonetario reais(BigDecimal valor) {
		return new ValorMonetario(valor, "BRL");
	}
}
