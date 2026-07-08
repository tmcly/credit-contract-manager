package br.com.contratocredito.domain;

import java.util.Objects;

/**
 * Postal address as a value object (immutable).
 */
public record Endereco(String estado, String cidade, String rua, String numero, Cep cep) {

	public Endereco {
		Objects.requireNonNull(estado, "estado não pode ser nulo");
		Objects.requireNonNull(cidade, "cidade não pode ser nula");
		Objects.requireNonNull(rua, "rua não pode ser nula");
		Objects.requireNonNull(numero, "numero não pode ser nulo");
		Objects.requireNonNull(cep, "cep não pode ser nulo");
	}
}
