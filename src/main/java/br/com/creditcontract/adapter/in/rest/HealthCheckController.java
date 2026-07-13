package br.com.creditcontract.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Only endpoint of the application at this early stage.
 * Simple healthcheck to know whether the app is up.
 *
 * GET /health  ->  { "status": "UP" }
 */
@RestController
@Tag(name = "Operations")
public class HealthCheckController {

	@GetMapping("/health")
	@Operation(
			operationId = "healthCheck",
			summary = "Check application availability",
			description = "Lightweight custom liveness endpoint. Use /actuator/health for dependency details.")
	@ApiResponse(responseCode = "200", description = "Application process is available.",
			content = @Content(mediaType = "application/json", schema = @Schema(type = "object"),
					examples = @ExampleObject(value = "{\"status\":\"UP\"}")))
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

}
