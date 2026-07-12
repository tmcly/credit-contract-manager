# ADR 015: Client-Requested Asynchronous Credit Reanalysis

- Status: Accepted; implemented
- Date: 2026-07-12

## Context

An active client may ask the credit provider to reassess the limit previously
approved for the contract. The command must not be available for blocked,
cancelled, or pre-activation contracts, and repeated requests need a cooldown so
the client cannot continuously trigger external credit work.

Reanalysis is different from initial analysis. The contract already has an
approved limit and remains usable while the new assessment is pending. Changing
its lifecycle status to `UNDER_REVIEW` would incorrectly disable an otherwise
active contract and violate the status/limit invariant established by ADR 005.

The result also needs durable business audit beyond transient logs or future
outbox-retention policies. A status-history row is not suitable because
successful and rejected reanalyses do not represent a contract-status change.

## Decision

- Expose `POST /api/contracts/{id}/credit-reanalysis` as an asynchronous client
  command and return `202 Accepted` with the request ID and next eligible date.
- Allow a request only while the contract is `ACTIVE`.
- Apply a configurable 30-day cooldown from every accepted request, regardless
  of whether its eventual result is approved or rejected.
- Return HTTP `429 Too Many Requests` with `nextEligibleAt` while the cooldown
  remains active.
- Keep the contract `ACTIVE` and its existing limit available while processing.
- Model each request as an auditable aggregate child in `credit_reanalyses`,
  with `REQUESTED`, `APPROVED`, or `REJECTED`, previous and resulting limits,
  reason, request time, and completion time.
- Emit `CreditReanalysisRequested` atomically through the outbox and route it to
  the durable `credit-reanalysis.requests` queue.
- Consume requests at least once, use the existing processed-message inbox, and
  route exhausted messages to `credit-reanalysis.requests.dlq`.
- Keep the external assessment behind a separate `CreditReanalysisProvider`
  because it evaluates an existing limit rather than issuing an initial offer.
- Make the local provider deterministic: CPF endings `0-1` reject; `2-4`
  multiply the current limit by `1.5`; `5-7` by `2`; and `8-9` by `3`.
- Cap the deterministic local result at R$ 100,000 and reject a request already
  at that cap. This is demonstration policy, not a universal credit rule.
- Emit mutually exclusive `CreditReanalysisApproved` and
  `CreditReanalysisRejected` result facts. Approved payloads contain the previous
  and new limits; rejected payloads contain the previous and retained limits plus
  the reason.
- Revalidate the aggregate when applying the provider result. If the contract is
  no longer active, reject the reanalysis and retain the current limit.
- Treat the request event ID as the provider idempotency key and the result
  event's causation ID.
- Keep caller authentication and proof that the authenticated client owns the
  contract outside this increment.

## Consequences

### Positive

- The endpoint cannot be spammed inside the configured policy window.
- Active credit remains usable while the eventually consistent assessment runs.
- Limit changes and rejected attempts have a durable, queryable audit record.
- Result events expose explicit before-and-after financial values.
- Duplicate deliveries cannot repeat a limit increase or audit outcome.
- A real provider can replace the deterministic adapter without changing the
  domain or REST contract.

### Trade-offs

- The response acknowledges a request, not its final outcome; clients must query
  later or consume a notification produced from result events.
- Reanalysis adds a table, three events, two queues, a DLQ, and another consumer.
- Simultaneous requests rely on aggregate optimistic locking; one can succeed
  while a conflicting request receives a concurrency failure until conflict
  mapping is improved.
- Loading the aggregate currently loads its reanalysis audit collection. A
  dedicated paginated read model may become useful if contract lifetime grows
  enough for that collection to become large.
