# ADR 001: PostgreSQL, Flyway, and an Isolated JPA Adapter

- Status: Accepted
- Date: 2026-07-10

## Context

Credit contracts have structured data, monetary values, unique public numbers,
lifecycle states, audit history, concurrency concerns, and transactional
consistency requirements. These characteristics benefit from a relational data
model and database-enforced integrity.

The domain must not depend directly on a persistence framework. The project
also needs repeatable local setup, controlled schema evolution, and integration
tests against the real database engine.

## Decision

- Use PostgreSQL as the operational database.
- Use Flyway as the only schema migration mechanism.
- Configure Hibernate with `ddl-auto=validate` and `open-in-view=false`.
- Use Spring Data JPA and Hibernate only inside the outbound persistence
  adapter.
- Keep domain aggregates separate from JPA entities and map explicitly between
  them.
- Use Testcontainers with PostgreSQL for persistence integration tests.
- Use Docker Compose for a persistent local development database.

Client name and address are stored in the contract table as a snapshot because
the client registry is external to this bounded context.

## Consequences

### Positive

- Transactions and constraints match the domain's consistency needs.
- Flyway makes schema changes reviewable and repeatable.
- Tests detect PostgreSQL-specific behavior that an in-memory substitute might
  hide.
- Persistence technology does not leak into the domain model.

### Trade-offs

- Separate domain and JPA models require explicit mapping code.
- Integration tests require Docker and are slower than pure unit tests.
- Every schema change needs a new forward-only migration.

## Rejected alternatives

- A document database was not selected because the current model is structured
  and transaction-heavy rather than schema-flexible.
- H2 was not selected as the primary test database because compatibility with
  PostgreSQL behavior is more valuable than a faster but different engine.
- Hibernate auto-DDL was rejected because it hides schema changes from review
  and is unsafe as an evolution strategy.
