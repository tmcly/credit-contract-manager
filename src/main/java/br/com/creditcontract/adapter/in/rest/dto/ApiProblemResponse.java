package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.LocalDateTime;

/** Documentation model for the RFC 7807 errors emitted by the REST adapter. */
@Schema(name = "ApiProblem", description = "RFC 7807 problem detail returned when a request cannot be fulfilled.")
public record ApiProblemResponse(
		@Schema(description = "Stable URI identifying the error category.", example = "/errors/invalid-contract-transition")
		URI type,
		@Schema(description = "Short human-readable error category.", example = "Invalid contract transition")
		String title,
		@Schema(description = "HTTP status code.", example = "409")
		int status,
		@Schema(description = "Specific explanation for this occurrence.",
				example = "credit contract cannot transition from UNDER_REVIEW to BLOCKED")
		String detail,
		@Schema(description = "Request path that produced the problem.", example = "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/blocking")
		URI instance,
		@Schema(description = "Present only when a credit-reanalysis cooldown is active.",
				example = "2026-08-12T10:20:00", nullable = true)
		LocalDateTime nextEligibleAt) {
}
