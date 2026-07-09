/**
 * Domain ports (interfaces the domain depends on but does not implement).
 *
 * A port declares WHAT the domain needs from the outside world (persistence,
 * external systems) without knowing HOW. The infrastructure layer provides
 * the concrete adapters.
 *
 * Example: ClientDataProvider — reads client data owned by the external
 * client-management context (Anti-Corruption Layer seam).
 */
package br.com.creditcontract.domain.port;
