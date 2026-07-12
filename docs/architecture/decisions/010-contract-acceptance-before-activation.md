# ADR 010: Contract Acceptance Before Activation

- Status: Accepted
- Date: 2026-07-11

## Context

Credit analysis approval only establishes that the client is eligible for a
positive credit limit. It does not prove that the client read the offer,
accepted its terms, or intends to use the credit. Moving directly from
`APPROVED` to `ACTIVE` would conflate a risk decision with client consent and
operational provisioning.

The approval event may be consumed by a separate notification application that
informs the client through e-mail, SMS, or a banking app. The client then needs
an explicit command to accept the contract. Activation may still depend on a
different service and may fail after acceptance.

## Decision

- Keep `CreditAnalysisApproved` as the fact that analysis approved a limit.
- Add `ACCEPTED` between `APPROVED` and `ACTIVE`.
- Expose `POST /api/contracts/{id}/acceptance` as the client acceptance command.
- Do not accept a caller-provided status or acceptance timestamp. The aggregate
  owns the transition and the application clock records its occurrence.
- Make repeated acceptance of an already `ACCEPTED` contract idempotent: return
  its current representation without another history entry or event.
- Record `APPROVED -> ACCEPTED` in status history and emit the versioned
  `CreditContractAccepted` event atomically through the transactional outbox.
- Route accepted events to the durable `credit-contract.activation.requests`
  queue for a future provisioning consumer.
- Reserve `CreditContractActivated` for the distinct future transition
  `ACCEPTED -> ACTIVE`. This increment does not emit it.
- Keep authentication, legal terms versioning, signature evidence, offer
  expiration, and refusal/withdrawal flows outside this first acceptance
  increment. The endpoint is not production-safe until caller ownership is
  authenticated.

## Consequences

### Positive

- Analysis approval, client consent, and operational activation remain distinct
  business facts.
- Client acceptance is auditable and asynchronously available to downstream
  provisioning.
- Retries do not duplicate history or events after acceptance succeeds.
- The activation phase can evolve independently without changing the meaning of
  `CreditContractAccepted`.

### Trade-offs

- The lifecycle and database constraints gain another state.
- A durable activation-request queue may retain messages until its future
  consumer is implemented.
- The initial endpoint records intent but not yet authenticated identity,
  contract-terms version, or legal signature evidence.
