package br.com.creditcontract.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Transport input for a request to block an active credit contract. */
public record BlockCreditContractRequest(
		@NotBlank(message = "blocking reason is required")
		@Size(max = 255, message = "blocking reason cannot exceed 255 characters")
		String reason) {
}
