/**
 * Camada de INTERFACES / ADAPTERS DE ENTRADA (camada mais externa).
 *
 * É a "porta de entrada" do sistema para o mundo externo:
 *   - REST controllers (HTTP)
 *   - Consumers de fila/mensageria
 *   - CLI, gRPC, GraphQL, etc.
 *
 * Responsabilidade: traduzir o mundo externo (JSON, protocolo) para chamadas
 * à camada de application. Não contém regra de negócio.
 *
 * Por ora contém apenas o HealthCheckController (endpoint de saúde).
 */
package br.com.contratocredito.interfaces;
