package br.com.contratocredito.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Brazilian postal code (CEP) as a value object.
 */
public record Cep(String valor) {

	private static final Pattern FORMAT = Pattern.compile("\\d{5}-?\\d{3}");

	public Cep {
		Objects.requireNonNull(valor, "CEP não pode ser nulo");
		if (!FORMAT.matcher(valor).matches()) {
			throw new IllegalArgumentException("CEP inválido: " + valor);
		}
	}

	/** Returns the digits only, without the dash (e.g. 80010000). */
	public String numerico() {
		return valor.replace("-", "");
	}
}
