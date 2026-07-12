package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Transport input for a request to unblock a blocked credit contract. */
public record UnblockCreditContractRequest(
		@NotBlank(message = "unblocking reason is required")
		@Size(max = 255, message = "unblocking reason cannot exceed 255 characters")
		String reason) {
}
