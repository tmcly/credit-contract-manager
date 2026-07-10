package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/contracts}.
 *
 * <p>The only required field is the client's CPF.
 * Everything else — client snapshot, credit limit, contract number — is
 * resolved by the use case through application output ports.
 */
public record CreateContractRequest(
		@NotBlank(message = "cpf is required") String cpf
) {
}
