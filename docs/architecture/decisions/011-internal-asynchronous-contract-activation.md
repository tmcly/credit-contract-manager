# ADR 011: Internal Asynchronous Contract Activation

- Status: Accepted; implemented
- Date: 2026-07-12
- Amends: ADR 010

## Context

ADR 010 separated credit approval, client acceptance, and activation but left
activation open to a future operational provisioner. The confirmed business
flow does not currently create, reserve, or confirm a resource in another
system. After the client accepts, this application consumes the accepted event
and makes the contract active in its own database.

Calling that internal state transition "provisioning" would imply an external
capability that does not exist. Activation must still remain asynchronous so
the API records client consent independently from processing the resulting
business fact.

## Decision

- Keep `APPROVED`, `ACCEPTED`, and `ACTIVE` as distinct states and facts.
- Continue emitting `CreditContractAccepted` atomically with
  `APPROVED -> ACCEPTED`.
- Consume new accepted events from `credit-contract.activation.requests.v2`
  inside this application.
- Let an inbound RabbitMQ adapter call `ActivateCreditContractUseCase`; the
  consumer must not update persistence models directly.
- Make the aggregate own the legal `ACCEPTED -> ACTIVE` transition and append
  it to status history.
- Emit `CreditContractActivated` through the transactional outbox with the
  accepted event as its causation ID.
- Record the consumed accepted-event ID in the inbox in the same transaction as
  the active state and activated event.
- Treat an already processed event as a no-op. Treat an already active
  aggregate as a recovery safeguard and record the delivery as processed.
- Apply the existing bounded retry policy and route exhausted activation
  messages to a dedicated dead-letter queue.
- Route activated events to a durable activation-results queue so mandatory
  publication always has an explicit destination.
- Keep consuming the legacy `credit-contract.activation.requests` queue during
  migration. Existing brokers may retain its old binding, so inbox idempotency
  safely handles the same event arriving through both queues. New bindings use
  only the versioned queue because RabbitMQ cannot add DLX arguments by
  redeclaring an existing queue.
- Do not introduce a provisioning port or service until a real external
  resource or integration becomes a business requirement.

## Consequences

### Positive

- Names match the implemented business capability instead of anticipating an
  external system.
- Client acceptance remains auditable even when activation follows quickly.
- At-least-once delivery cannot duplicate the transition, history, or event.
- Downstream consumers can react to the separate activated fact.
- Infrastructure remains outside the domain and application boundaries.

### Trade-offs

- The API can briefly return `ACCEPTED` before a query observes `ACTIVE`.
- Activation adds another consumer, result queue, DLQ, inbox row, and outbox
  event to operate.
- An external provisioner introduced later will require a new deliberate
  decision about whether `ACTIVE` occurs before or after its confirmation.
