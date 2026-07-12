package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Transport input for a manual credit-contract cancellation. */
public record CancelCreditContractRequest(
		@NotNull(message = "requestedBy is required") CancellationRequester requestedBy,
		@NotBlank(message = "cancellation reason is required")
		@Size(max = 255, message = "cancellation reason cannot exceed 255 characters")
		String reason) {
}
