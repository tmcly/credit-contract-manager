package br.com.contratocredito.domain;

import java.util.Objects;

/**
 * Client embedded within the contract.
 *
 * As in the original system, the unit of work was the contract, not a
 * standalone client aggregate — so Client is modeled here as a value object
 * (immutable), nested in {@link ContratoCredito}. If a client ever needs its
 * own lifecycle (multiple contracts per client), this can become an entity later.
 */
public record Cliente(String nome, Endereco endereco) {

	public Cliente {
		Objects.requireNonNull(nome, "nome do cliente não pode ser nulo");
		Objects.requireNonNull(endereco, "endereco do cliente não pode ser nulo");
	}
}
