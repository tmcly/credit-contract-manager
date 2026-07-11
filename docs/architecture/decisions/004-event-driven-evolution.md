# ADR 004: Transactional Outbox and RabbitMQ-First Event Architecture

- Status: Accepted; partially implemented
- Date: 2026-07-10

## Context

The planned contract lifecycle contains work that can happen asynchronously:
credit analysis, notifications, downstream document generation, audit, and
future integrations. Direct synchronous calls couple availability and latency
across components.

Writing the database and publishing to a broker as two independent operations
creates a dual-write failure mode: the contract can commit while its event is
lost, or an event can be published for a transaction that later fails.

## Decision

- Evolve the application toward domain and integration events expressed in the
  past tense, such as `CreditContractCreated` and
  `CreditAnalysisCompleted`.
- Use RabbitMQ first because the near-term requirements emphasize work queues,
  routing, acknowledgements, retries, and dead-letter handling.
- Keep broker access behind an application output port.
- Persist outbound events in an `outbox_events` table in the same PostgreSQL
  transaction as aggregate changes.
- Publish pending outbox records asynchronously and mark them as published only
  after broker confirmation.
- Assume at-least-once delivery and require idempotent consumers.
- Add event identifiers, aggregate identifiers, occurrence time, correlation
  and causation metadata, and an explicit schema version.
- Add retry with backoff and dead-letter queues for poison messages.

Kafka is not part of the first implementation. It should be reconsidered only
when durable replay, long retention, stream processing, or a broad independent
consumer ecosystem becomes a concrete requirement.

## Consequences

### Positive

- Database and event intent are committed atomically.
- Consumers can evolve independently from the HTTP request.
- Failures become retryable and observable.
- The repository demonstrates production-oriented messaging concerns rather
  than broker-only sample code.

### Trade-offs

- Delivery is eventually consistent rather than immediate.
- Duplicate delivery is expected and must be handled.
- Outbox cleanup, retries, monitoring, and dead-letter operations add
  operational complexity.
- Asynchronous credit analysis requires explicit changes to current aggregate
  invariants and API expectations.

## Non-goals

- Event sourcing is not being adopted. PostgreSQL aggregate tables remain the
  source of current state.
- Microservices are not required. The project may remain a modular monolith
  while using durable asynchronous boundaries.
- Exactly-once delivery is not claimed end to end.

## Implementation status

The transactional outbox and RabbitMQ relay are implemented. `CreditContract`
records the versioned `CreditContractCreated` event, PostgreSQL stores it in the
same transaction as the aggregate, and a scheduled bounded relay publishes it
through a durable RabbitMQ topology. Rows become `PUBLISHED` only after a
publisher confirmation; failures remain eligible for retry.

JSON serialization, outbox storage, scheduling, and RabbitMQ access remain in
outbound adapters. Consumer idempotency, exponential backoff, bounded poison
message attempts, and dead-letter topology remain pending for later roadmap
phases.
