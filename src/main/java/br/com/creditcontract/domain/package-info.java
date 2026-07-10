/**
 * Domain layer (DDD + business rules).
 *
 * This is the heart of the system: entities, value objects, enumerations,
 * domain events and the invariants that define what is valid.
 *
 * CLEAN ARCHITECTURE / DDD RULE:
 *   - Do NOT import Spring, JPA, Web or any external framework here.
 *   - The domain does not know who consumes it nor where data is persisted.
 *   - External capabilities required by use cases are declared as output
 *     ports in the application layer and implemented by outbound adapters.
 *
 * Sub-packages:
 *   - entity/        : aggregate roots and entities (CreditContract)
 *   - valueobject/   : immutable value types (ContractId, MonetaryAmount, ...)
 *   - enums/         : domain enumerations (ContractStatus)
 *   - exception/     : errors raised by domain rules and value objects
 */
package br.com.creditcontract.domain;
