# ADR 002: Inward Dependencies and Explicit Mapping Boundaries

- Status: Accepted
- Date: 2026-07-10

## Context

The project needs to remain understandable as infrastructure grows. Direct
dependencies from business logic to REST, Spring Data, JPA, RabbitMQ, or local
stubs would make domain changes harder to test and integration technology harder
to replace.

The words `interface` and `adapter` have different roles here: application
ports are contracts for capabilities, while adapters translate concrete
external mechanisms into those contracts.

## Decision

- Organize external entry points under `adapter/in`.
- Organize database, service, and messaging integrations under `adapter/out`.
- Define required external capabilities under `application/port/out`.
- Keep use-case orchestration under `application/usecase`.
- Keep business invariants and transitions in the domain aggregate.
- Use transport DTOs at the REST boundary.
- Use JPA entities at the persistence boundary.
- Use explicit mappers between infrastructure models and domain models.

The persistence mapper is a Data Mapper boundary. It converts domain state into
the shape required by JPA without turning the domain aggregate into a database
record.

## Consequences

### Positive

- Unit tests can mock ports without starting infrastructure.
- Domain code stays focused on business language.
- PostgreSQL, stubs, and future RabbitMQ integration remain replaceable
  implementation details.
- Mapping decisions are visible and testable.

### Trade-offs

- More classes exist than in a conventional CRUD application.
- A read path requires explicit rehydration from JPA entities into aggregates.
- Boundaries must be maintained deliberately; package names alone do not create
  Clean Architecture.

## Guidance

For small, data-centric CRUD features, sharing a model can be pragmatic. For the
credit-contract aggregate, separate models are intentional because lifecycle
rules and persistence structure are expected to evolve independently.
