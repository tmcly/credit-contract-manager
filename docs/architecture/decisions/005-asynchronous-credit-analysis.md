# ADR 005: Asynchronous Credit Analysis Outcomes

- Status: Accepted
- Date: 2026-07-11

## Context

Contract creation currently resolves a credit limit synchronously and persists
it while the aggregate is still `DRAFT`. The RabbitMQ workflow introduced by
ADR 004 now allows credit analysis to happen after the HTTP request, so a new
contract has not yet earned a limit.

Analysis can approve or reject credit. These outcomes drive different contract
states, carry different data, and may interest different downstream consumers.

## Decision

This ADR refines the generic completion-event example in ADR 004 for the
credit-analysis workflow; it does not change ADR 004's outbox and delivery
decisions.

- New contracts start in `DRAFT` without a credit limit.
- `DRAFT` and `UNDER_REVIEW` contracts must not have a credit limit.
- Analysis transitions are explicit aggregate operations:
  `DRAFT -> UNDER_REVIEW`, then either `UNDER_REVIEW -> APPROVED` or
  `UNDER_REVIEW -> REJECTED`.
- `APPROVED` requires a positive limit. `REJECTED` has no limit and stores its
  reason on the status-history transition, not on the contract.
- Replace the limit-only provider with a credit-analysis provider that returns
  an explicit approved or rejected result.
- The local stub deterministically rejects CPFs ending in 0 or 1 and approves
  endings 2 through 9 using the existing limit bands.
- Publish mutually exclusive `CreditAnalysisApproved` and
  `CreditAnalysisRejected` events. Do not publish a generic
  `CreditAnalysisCompleted`; consumers interested in both outcomes can bind
  both routing keys.
- Persist `UNDER_REVIEW` before invoking the analysis provider, then persist the
  terminal result and its outbox event in a second transaction.
- Re-delivery is state-aware: terminal contracts are ignored and
  `UNDER_REVIEW` contracts resume analysis. A general processed-message inbox
  remains planned for the resilience phase.

## Consequences

### Positive

- The API no longer waits for credit analysis.
- Absence of a limit means "not approved" instead of being represented by a
  misleading zero value.
- Event names expose the business outcome without nullable discriminator data.
- A crash after entering `UNDER_REVIEW` can be recovered by message re-delivery.
- Status history records every lifecycle transition.

### Trade-offs

- Creation responses are eventually consistent and initially contain no limit.
- Clients need a query endpoint to observe the final result.
- Two database transactions are used around the external analysis call.
- State-aware idempotency covers this workflow, but broader consumer
  idempotency still requires the inbox planned for Phase 6.
