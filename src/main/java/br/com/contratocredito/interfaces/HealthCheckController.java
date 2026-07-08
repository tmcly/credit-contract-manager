package br.com.contratocredito.interfaces;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Único endpoint da aplicação neste estágio inicial.
 * Healthcheck simples para saber se a aplicação está no ar.
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
