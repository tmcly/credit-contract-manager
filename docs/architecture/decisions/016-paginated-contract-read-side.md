# ADR 016: Paginated Contract Read Side

- Status: Accepted; implemented
- Date: 2026-07-12

## Context

The original query endpoint retrieves one contract by ID by rehydrating the
aggregate. That is appropriate when an application command or detailed view
needs domain state, but it does not scale well for collection and audit reads.
The aggregate owns status history and credit-reanalysis children, so using it
for lists would load data that the response does not need and would make audit
collections grow without a bounded HTTP contract.

Consumers need to find contracts by lifecycle status, CPF, or public contract
number and inspect both audit trails. The API also needs deterministic page
ordering and an explicit upper bound to avoid unbounded database work.

## Decision

- Add a technology-independent `CreditContractQueryPort` beside the aggregate
  repository port.
- Represent pagination and list results with application records rather than
  leaking Spring Data types into application or HTTP contracts.
- Keep the read side in the same PostgreSQL database and transaction model. Do
  not introduce a second store, denormalized synchronization, or full CQRS.
- Use explicit JPA constructor projections for contract summaries so searches
  do not load client addresses, status history, reanalyses, or events.
- Support exact optional filters for status, normalized CPF, and contract
  number. CPF is a lookup predicate and is omitted from list responses.
- Use zero-based pages with a default size of 20 and maximum size of 100.
- Restrict contract sorting to `createdAt` and `updatedAt`, accept ascending or
  descending direction, and add ID as a stable tie-breaker.
- Return status history and credit reanalyses as separate paginated resources,
  newest first.
- Return `404 Not Found` when the contract does not exist, while returning an
  empty page for an existing contract without entries.
- Add PostgreSQL indexes for the supported contract-list ordering and common
  status-plus-creation-date access path.
- Own the JSON page envelope in REST DTOs instead of exposing Spring Data's
  serialization format.

## Consequences

### Positive

- Collection and audit reads remain bounded and avoid aggregate rehydration.
- HTTP pagination metadata is stable across framework upgrades.
- Filters and ordering are explicit, testable, and supported by indexes.
- The application can evolve query persistence independently while the domain
  remains free of database and framework concerns.
- CPF exposure is minimized in collection responses.

### Trade-offs

- Write and read mappings must evolve together when summary fields change.
- Offset pagination can become slower on very deep pages; cursor pagination is
  deferred until real volume or access patterns justify its added contract.
- Optional filters create multiple query shapes that should be monitored with
  production-like data before adding more composite indexes.
- The existing single-contract endpoint still rehydrates the aggregate; this
  decision optimizes the new collection and audit endpoints only.
