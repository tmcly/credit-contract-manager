package br.com.contratocredito.domain;

import java.util.UUID;

/**
 * Identity of a credit contract, modeled as a value object.
 *
 * The domain never deals with raw Strings/primitives for identifiers
 * (DDD: strong typing avoids mix-ups and keeps invariants close to the id).
 */
public record ContratoId(UUID valor) {

	public ContratoId {
		if (valor == null) {
			throw new IllegalArgumentException("ContratoId não pode ser nulo");
		}
	}

	public static ContratoId generate() {
		return new ContratoId(UUID.randomUUID());
	}

	public static ContratoId from(UUID valor) {
		return new ContratoId(valor);
	}

	public String asString() {
		return valor.toString();
	}
}
