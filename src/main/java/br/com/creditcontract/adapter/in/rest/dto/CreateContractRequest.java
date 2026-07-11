package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/contracts}.
 *
 * <p>The only required field is the client's document number (CPF).
 * Everything else — client snapshot and contract number — is resolved during
 * creation. Credit analysis happens asynchronously after the response.
 */
public record CreateContractRequest(
		@NotBlank(message = "documentNumber is required") String documentNumber
) {
}
