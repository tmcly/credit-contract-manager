package br.com.creditcontract.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Requesting party accepted by the manual cancellation endpoint. */
@Schema(description = "Party that authorized a manual cancellation.")
public enum CancellationRequester {
	CLIENT,
	LEGAL
}
