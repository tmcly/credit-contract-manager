# ADR 012: Synchronous Contract Blocking Command

- Status: Accepted; implemented
- Date: 2026-07-12

## Context

An active credit contract may need to be blocked by another application. In a
larger credit platform, a collection-rules service could evaluate overdue
invoices, coordinate collection offices, and eventually emit a blocking event.
This bounded context currently has no billing or collection-rules application,
so introducing that event source would create behavior the project does not
actually own.

The contract manager must still enforce its own lifecycle rules and publish the
resulting business fact for future consumers.

## Decision

- Receive blocking requests synchronously through
  `POST /api/contracts/{id}/blocking`.
- Require a non-blank blocking reason of at most 255 characters, aligned with
  the existing status-history schema.
- Allow only `ACTIVE -> BLOCKED`. In particular, contracts under analysis must
  not be blocked.
- Keep the transition and validation in `CreditContract`, not in the controller
  or persistence adapter.
- Let `BlockCreditContractUseCase` orchestrate repository access so a future
  message consumer can invoke the same application behavior.
- Treat a repeated request for an already blocked contract as idempotent and do
  not append another history entry or event.
- Store the blocking reason on the `ACTIVE -> BLOCKED` history transition; do
  not add a block-reason field to the aggregate or contract table.
- Emit `CreditContractBlocked` atomically through the transactional outbox.
- Preserve the HTTP correlation ID on the event. Its causation ID is absent
  because the current command is not caused by an inbound integration event.
- Route blocked events to the durable `credit-contract.lifecycle.events` queue
  as a local demonstration sink. A real downstream microservice should declare
  its own queue and bind to `credit-contract.blocked.v1`.
- Keep caller authentication, authorization, source-system identity, and a
  client-supplied idempotency key outside this increment.

## Consequences

### Positive

- The implemented entry point matches the integrations currently available.
- Invalid states such as `UNDER_REVIEW` fail before persistence changes.
- History retains why the contract was blocked without nullable status-specific
  columns.
- State and event intent commit atomically.
- A future collection event requires a new inbound adapter, not a duplicate
  domain rule.

### Trade-offs

- The endpoint trusts its caller until authentication and authorization exist.
- Idempotency is state-based; two distinct block commands cannot be
  distinguished after the first transition.
- The local lifecycle queue accumulates events until a demonstration or real
  consumer reads them.
- Unblocking remains a separate future business decision.
