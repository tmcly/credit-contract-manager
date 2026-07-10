package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.DocumentNumber;

import java.util.Objects;

/**
 * Input data for {@link CreateContractUseCase}.
 *
 * <p>The only mandatory field is the client's CPF. Everything
 * else is resolved through application output ports: client snapshot, credit
 * limit and contract number.
 */
public record CreateContractInput(DocumentNumber documentNumber) {

	public CreateContractInput {
		Objects.requireNonNull(documentNumber, "documentNumber is required");
	}
}
