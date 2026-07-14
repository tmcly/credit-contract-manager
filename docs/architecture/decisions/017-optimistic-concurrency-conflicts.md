# ADR 017: Optimistic Concurrency Conflicts at the API Boundary

- Status: Accepted; implemented
- Date: 2026-07-13

## Context

HTTP commands, RabbitMQ consumers, and scheduled work can load the same credit
contract version and attempt different valid transitions concurrently. The JPA
persistence model already uses `@Version`, so PostgreSQL and Hibernate prevent
a stale transaction from silently overwriting a committed change. However, the
resulting Spring persistence exception previously escaped the application and
appeared to API clients as an internal server failure.

A conflicting command cannot always be retried safely. Once the winning
transaction changes the contract status, the losing command may no longer be
valid under the aggregate's lifecycle rules.

## Decision

- Keep optimistic locking through the existing JPA `@Version` column.
- Do not add pessimistic row locks to normal contract command reads.
- Translate Spring's optimistic-lock failure inside the outbound persistence
  adapter into a technology-independent application exception.
- Return that application error from REST as RFC 7807 `409 Conflict` with the
  stable type `/errors/concurrent-contract-update`.
- Tell callers to fetch the current contract state before deciding whether to
  retry; do not retry conflicting business commands automatically.
- Preserve the existing transaction boundary so the losing contract change,
  status history, and outbox event roll back together.
- Verify the behavior with a PostgreSQL integration test in which two separate
  transactions load the same version and exactly one commits.
- Defer client-supplied preconditions such as HTTP `ETag` and `If-Match` until a
  concrete API requirement needs stale-client detection before command work.

## Consequences

### Positive

- Concurrent writes cannot silently replace each other.
- API clients can distinguish contention from an unexpected server failure.
- Spring and Hibernate exception types remain outside the application and
  domain boundaries.
- The winning transition keeps one consistent history entry and outbox event.

### Trade-offs

- The losing caller must read the latest state and make a new business decision.
- High contention would create repeated conflicts; pessimistic locking or a
  different command-serialization strategy would need reconsideration if that
  becomes a measured access pattern.
- `409 Conflict` reports a race detected during server-side processing; it does
  not prove that a client originally submitted the latest representation.

## Rejected alternatives

- Pessimistic `SELECT ... FOR UPDATE` locking was rejected because expected
  per-contract contention is low and blocking requests would add waits,
  timeouts, and deadlock risk.
- Automatic retries were rejected because a new status can change whether the
  original command is legal.
- Global `SERIALIZABLE` transaction isolation was rejected as broader and more
  expensive than versioning the aggregate that owns the contested state.
