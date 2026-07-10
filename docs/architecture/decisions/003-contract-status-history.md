# ADR 003: Generic Contract Status History

- Status: Accepted
- Date: 2026-07-10

## Context

Status-specific fields such as `blockReason` and `cancellationReason` couple the
contract table and aggregate to individual transitions. Every new lifecycle
state could create another nullable column and another special case.

The system needs to explain how a contract reached its current state and retain
the reason and time of each transition.

## Decision

- Keep only the current status on the credit contract.
- Record every transition in `contract_status_history`.
- A history entry contains an identifier, previous status, new status, optional
  reason, and transition timestamp.
- Record contract creation as the initial `null -> DRAFT` transition.
- Keep transition history inside the aggregate and expose it as an immutable
  collection.
- Do not create status-specific reason properties on `CreditContract`.

## Consequences

### Positive

- New statuses do not require new reason columns.
- The contract has a chronological audit trail.
- Blocking, cancellation, approval, and reanalysis share one model.
- Reasons are attached to the transition they explain.

### Trade-offs

- Queries need a join when both current state and transition details are
  required.
- The aggregate must enforce legal transitions; a history table alone cannot
  provide business correctness.
- Future read mapping must preserve history ordering and identity.

## Example

```text
null         -> DRAFT         reason: null
DRAFT        -> UNDER_REVIEW  reason: Credit analysis requested
UNDER_REVIEW -> APPROVED      reason: Credit engine approved the limit
ACTIVE       -> BLOCKED       reason: Payment overdue for more than 30 days
```
