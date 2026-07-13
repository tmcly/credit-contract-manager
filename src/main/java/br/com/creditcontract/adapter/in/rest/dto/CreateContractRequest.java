package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/contracts}.
 *
 * <p>The only required field is the client's document number (CPF).
 * Everything else — client snapshot and contract number — is resolved during
 * creation. Credit analysis happens asynchronously after the response.
 */
@Schema(description = "Minimum input required to create a credit contract.")
public record CreateContractRequest(
		@Schema(description = "Brazilian CPF. Punctuation is optional and check digits are validated.",
				example = "529.982.247-25")
		@NotBlank(message = "documentNumber is required") String documentNumber
) {
}
