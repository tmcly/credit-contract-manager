/**
 * Domain layer (DDD + business rules).
 *
 * This is the heart of the system: entities, value objects, enumerations,
 * domain events and the invariants that define what is valid.
 *
 * CLEAN ARCHITECTURE / DDD RULE:
 *   - Do NOT import Spring, JPA, Web or any external framework here.
 *   - The domain does not know who consumes it nor where data is persisted.
 *   - If an external adapter is needed, declare an INTERFACE (port) here and
 *     provide the concrete implementation in the infrastructure layer.
 *
 * Sub-packages:
 *   - entity/        : aggregate roots and entities (CreditContract)
 *   - valueobject/   : immutable value types (ContractId, MonetaryAmount, ...)
 *   - enumeration/   : domain enumerations (ContractStatus)
 */
package br.com.creditcontract.domain;
