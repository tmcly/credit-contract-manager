package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/contracts}.
 *
 * <p>The only required field is the client's document number (CPF/CNPJ).
 * Everything else — client snapshot, credit limit, contract number — is
 * resolved by the use case through application output ports.
 */
public record CreateContractRequest(
		@NotBlank(message = "documentNumber is required") String documentNumber
) {
}
