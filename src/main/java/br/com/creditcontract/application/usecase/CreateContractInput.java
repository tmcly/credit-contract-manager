package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.Cpf;

import java.util.Objects;

/**
 * Input data for {@link CreateContractUseCase}.
 *
 * <p>The only mandatory field is the client's CPF. Everything
 * else is resolved through application output ports: client snapshot, credit
 * limit and contract number.
 */
public record CreateContractInput(Cpf cpf) {

	public CreateContractInput {
		Objects.requireNonNull(cpf, "cpf is required");
	}
}
