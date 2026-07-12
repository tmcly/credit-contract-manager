# ADR 014: Manual and Expiring Contract Cancellation

- Status: Accepted; implemented
- Date: 2026-07-12

## Context

Credit contracts can be cancelled after a client request, a legal request, or
prolonged failure to regularize a block. These origins have different authority:
a client must not bypass a payment-related block, while legal action can end an
active or blocked relationship.

Brazilian law does not establish one universal number of days after which every
consumer credit contract must be cancelled for default. Civil Code arts. 474
and 475 address contractual resolution for breach without fixing that period;
the Consumer Protection Code requires disclosure of credit terms and default
consequences. The Central Bank uses arrears over 90 days as a statistical
definition of credit delinquency, not as a statutory cancellation deadline.

References: [Civil Code](https://www.planalto.gov.br/ccivil_03/leis/2002/l10406compilada.htm),
[Consumer Protection Code](https://www.planalto.gov.br/ccivil_03/leis/l8078compilado.htm),
and [Central Bank credit methodology](https://www.bcb.gov.br/content/estatisticas/notas_metodologicas/emprestimos-sfn/notaempr.pdf).

## Decision

- Expose `POST /api/contracts/{id}/cancellation` with `requestedBy` and a
  required reason of at most 255 characters.
- Allow a `CLIENT` request only from `ACTIVE`.
- Allow a `LEGAL` request from `ACTIVE` or `BLOCKED`.
- Automatically cancel only `BLOCKED` contracts whose last aggregate update is
  at or before the expiration cutoff.
- Use 90 calendar days as the default blocked-expiration business policy and
  make it configurable through
  `credit-contract.cancellation.blocked-expiration`.
- Scan a bounded batch periodically through an inbound scheduling adapter.
- Support the expiration lookup with a partial PostgreSQL index over blocked
  contracts ordered by update time.
- Keep all three transition rules in `CreditContract` and store the reason in
  generic status history.
- Emit versioned `CreditContractCancelled` with previous status, origin,
  reason, occurrence time, and correlation ID through the transactional outbox.
- Route cancelled facts with `credit-contract.cancelled.v1` to the existing
  lifecycle-events demonstration queue.
- Do not clear the approved limit or imply that cancellation pays, forgives, or
  otherwise settles outstanding debt.

## Consequences

### Positive

- Client requests cannot evade a block while legal authority remains explicit.
- Expiration is automated, configurable, auditable, and testable.
- Downstream services receive one stable fact for every cancellation origin.
- The repository does not misrepresent a business policy as Brazilian law.

### Trade-offs

- A polling scheduler introduces eventual timing bounded by its scan interval.
- Multiple application instances can race; optimistic locking prevents silent
  conflicting writes, but a future database claim strategy would improve batch
  coordination at scale.
- Cancellation does not model debt collection, settlement, or balance payoff.
- Authentication, authorization, legal-document evidence, client identity, and
  command idempotency remain future requirements.
