package br.com.creditcontract.domain.valueobject;

import java.util.Objects;

/**
 * Postal address as a value object (immutable).
 */
public record Address(String state, String city, String street, String number, ZipCode zipCode) {

	public Address {
		Objects.requireNonNull(state, "state cannot be null");
		Objects.requireNonNull(city, "city cannot be null");
		Objects.requireNonNull(street, "street cannot be null");
		Objects.requireNonNull(number, "number cannot be null");
		Objects.requireNonNull(zipCode, "zip code cannot be null");
	}
}
