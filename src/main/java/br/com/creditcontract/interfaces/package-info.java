/**
 * Interfaces / input adapters layer (outermost layer).
 *
 * The "entry point" of the system to the external world:
 *   - REST controllers (HTTP)
 *   - Queue/messaging consumers
 *   - CLI, gRPC, GraphQL, etc.
 *
 * Responsibility: translate the external world (JSON, protocol) into calls
 * to the application layer. Contains no business rules.
 *
 * For now it only holds the HealthCheckController (health endpoint).
 */
package br.com.creditcontract.interfaces;
