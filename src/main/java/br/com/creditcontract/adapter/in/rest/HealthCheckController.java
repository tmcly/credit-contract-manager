package br.com.creditcontract.adapter.in.rest;

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
public class HealthCheckController {

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}

}
