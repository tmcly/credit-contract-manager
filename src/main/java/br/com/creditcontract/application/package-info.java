/**
 * Application layer (use cases).
 *
 * Orchestrates the domain to fulfill a business need. Each use case is
 * represented by a single class (e.g. CreateContractUseCase), following the
 * Single Responsibility Principle (S - SOLID).
 *
 * Characteristics:
 *   - Does not contain complex business rules (that belongs to the domain).
 *   - Owns output ports for external capabilities required by its use cases.
 *   - Depends on the domain, never on concrete adapters.
 *   - Knows nothing about transport (HTTP, queue) or concrete persistence.
 *
 * Future examples: CreateContractUseCase, CancelContractUseCase,
 * BlockContractUseCase, ReanalyzeContractUseCase.
 */
package br.com.creditcontract.application;
