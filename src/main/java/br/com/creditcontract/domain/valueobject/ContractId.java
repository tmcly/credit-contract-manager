package br.com.creditcontract.domain.valueobject;

import java.util.UUID;

/**
 * Identity of a credit contract, modeled as a value object.
 *
 * The domain never deals with raw Strings/primitives for identifiers
 * (DDD: strong typing avoids mix-ups and keeps invariants close to the id).
 */
public record ContractId(UUID value) {

	public ContractId {
		if (value == null) {
			throw new IllegalArgumentException("ContractId cannot be null");
		}
	}

	public static ContractId generate() {
		return new ContractId(UUID.randomUUID());
	}

	public static ContractId from(UUID value) {
		return new ContractId(value);
	}

	public String asString() {
		return value.toString();
	}
}
