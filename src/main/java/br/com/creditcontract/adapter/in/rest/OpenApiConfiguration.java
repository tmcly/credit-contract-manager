package br.com.creditcontract.adapter.in.rest;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/** Human-facing metadata and navigation groups for the generated API contract. */
@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Credit Contract Manager API",
				version = "v1",
				description = """
						API for the lifecycle of Brazilian personal credit contracts.

						Commands enforce aggregate business rules and persist integration events through
						a transactional outbox. Credit analysis, activation, and credit reanalysis are
						asynchronous flows, so a successful command response may represent an accepted
						request rather than the final processing outcome.
						"""),
		tags = {
				@Tag(name = "Contract commands", description = "Create contracts and request lifecycle transitions."),
				@Tag(name = "Contract queries", description = "Find contracts and inspect lifecycle audit data."),
				@Tag(name = "Operations", description = "Simple application availability endpoints.")
		},
		externalDocs = @ExternalDocumentation(
				description = "Architecture, business rules, and local execution guide",
				url = "https://github.com/tmcly/credit-contract-manager"))
public class OpenApiConfiguration {
}
