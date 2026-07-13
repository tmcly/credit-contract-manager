package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Transport input for a request to block an active credit contract. */
@Schema(description = "Reason supplied by the external application requesting the block.")
public record BlockCreditContractRequest(
		@Schema(description = "Auditable business reason stored in the status history.",
				example = "Payment overdue for more than 30 days", maxLength = 255)
		@NotBlank(message = "blocking reason is required")
		@Size(max = 255, message = "blocking reason cannot exceed 255 characters")
		String reason) {
}
