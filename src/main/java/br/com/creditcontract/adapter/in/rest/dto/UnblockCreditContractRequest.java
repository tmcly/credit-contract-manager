package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Transport input for a request to unblock a blocked credit contract. */
@Schema(description = "Reason supplied by the external application requesting the unblock.")
public record UnblockCreditContractRequest(
		@Schema(description = "Auditable business reason stored in the status history.",
				example = "Overdue balance was settled", maxLength = 255)
		@NotBlank(message = "unblocking reason is required")
		@Size(max = 255, message = "unblocking reason cannot exceed 255 characters")
		String reason) {
}
