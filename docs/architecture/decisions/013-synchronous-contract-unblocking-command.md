# ADR 013: Synchronous Contract Unblocking Command

- Status: Accepted; implemented
- Date: 2026-07-12

## Context

ADR 012 introduced synchronous blocking because the current bounded context has
no collection-rules service that could originate the command as an event. A
blocked contract also needs an explicit way to return to active status after an
external application determines that the blocking condition no longer applies.

The contract manager remains responsible for deciding whether the lifecycle
transition is legal, recording why it happened, and publishing the resulting
business fact. An `ACTIVE` contract is ambiguous: it may have been activated for
the first time or previously unblocked. Treating every `ACTIVE` state as an
idempotent unblocking success would weaken the rule that only blocked contracts
can be unblocked.

## Decision

- Receive unblocking requests synchronously through
  `POST /api/contracts/{id}/unblocking`.
- Require a non-blank unblocking reason of at most 255 characters, aligned with
  the generic status-history schema and the blocking command.
- Allow only `BLOCKED -> ACTIVE` and keep the rule inside `CreditContract`.
- Let `UnblockCreditContractUseCase` orchestrate repository access so future
  inbound adapters can reuse the same domain behavior.
- Do not treat an `ACTIVE` contract as an idempotent success. Every non-`BLOCKED`
  state returns an invalid-transition conflict without persistence or an event.
- Store the unblocking reason on the `BLOCKED -> ACTIVE` history transition;
  do not add an unblocking-specific field to the aggregate or contract table.
- Emit `CreditContractUnblocked` atomically through the transactional outbox.
- Preserve the HTTP correlation ID on the event and leave causation absent
  because the current command is not caused by an inbound integration event.
- Route unblocked events to the existing durable
  `credit-contract.lifecycle.events` demonstration queue with routing key
  `credit-contract.unblocked.v1`.
- Keep caller authentication, authorization, source-system identity, and a
  client-supplied idempotency key outside this increment.

## Consequences

### Positive

- The aggregate enforces a precise and auditable return to active status.
- Invalid requests cannot fabricate a successful unblocking operation.
- History explains why the restriction ended without status-specific columns.
- State and event intent commit atomically for downstream consumers.
- A future event-driven entry point can reuse the application and domain rules.

### Trade-offs

- A repeated HTTP request after a successful unblock returns a conflict rather
  than a state-based idempotent success.
- True command idempotency requires a caller-provided idempotency key and
  persisted command identity in a future increment.
- The endpoint trusts its caller until authentication and authorization exist.
- The shared lifecycle queue accumulates both blocked and unblocked facts until
  a demonstration or real consumer reads them.
