package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.valueobject.DocumentNumber;

import java.util.Objects;
import java.util.UUID;

/**
 * Input data for {@link CreateContractUseCase}.
 *
 * <p>The only mandatory field is the client's CPF. Everything
 * else is resolved through application output ports: client snapshot, credit
 * limit and contract number.
 */
public record CreateContractInput(DocumentNumber documentNumber, UUID correlationId) {

	public CreateContractInput(DocumentNumber documentNumber) {
		this(documentNumber, UUID.randomUUID());
	}

	public CreateContractInput {
		Objects.requireNonNull(documentNumber, "documentNumber is required");
		Objects.requireNonNull(correlationId, "correlationId is required");
	}
}
