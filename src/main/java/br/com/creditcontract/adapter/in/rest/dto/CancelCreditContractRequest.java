package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Transport input for a manual credit-contract cancellation. */
@Schema(description = "Manual cancellation request and the party authorizing it.")
public record CancelCreditContractRequest(
		@Schema(description = "CLIENT can cancel only ACTIVE contracts; LEGAL can cancel ACTIVE or BLOCKED contracts.",
				example = "LEGAL")
		@NotNull(message = "requestedBy is required") CancellationRequester requestedBy,
		@Schema(description = "Auditable cancellation reason stored in status history.",
				example = "Cancellation requested by court order", maxLength = 255)
		@NotBlank(message = "cancellation reason is required")
		@Size(max = 255, message = "cancellation reason cannot exceed 255 characters")
		String reason) {
}
