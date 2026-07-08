package br.com.creditcontract.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Brazilian postal code (CEP) as a value object.
 */
public record ZipCode(String value) {

	private static final Pattern FORMAT = Pattern.compile("\\d{5}-?\\d{3}");

	public ZipCode {
		Objects.requireNonNull(value, "zip code cannot be null");
		if (!FORMAT.matcher(value).matches()) {
			throw new IllegalArgumentException("invalid zip code: " + value);
		}
	}

	/** Returns the digits only, without the dash (e.g. 80010000). */
	public String digits() {
		return value.replace("-", "");
	}
}
