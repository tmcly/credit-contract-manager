# Architecture Decision Records

Architecture Decision Records preserve the context, decision, and consequences
of choices that should survive individual tasks and conversations.

| ADR | Status | Decision |
|---|---|---|
| [001](001-postgresql-flyway-and-jpa.md) | Accepted | PostgreSQL, Flyway, and isolated JPA adapter |
| [002](002-clean-architecture-boundaries.md) | Accepted | Inward dependencies and explicit mapping boundaries |
| [003](003-contract-status-history.md) | Accepted | Generic status-transition history |
| [004](004-event-driven-evolution.md) | Accepted, partially implemented | Transactional outbox and RabbitMQ relay implemented; consumer resilience pending |
| [005](005-asynchronous-credit-analysis.md) | Accepted | Nullable pre-analysis limit and explicit approved/rejected outcomes |
| [006](006-at-least-once-resilience-and-observability.md) | Accepted | Inbox idempotency, bounded retries, DLQ, correlation, and messaging metrics |

When a decision changes, add a new ADR that supersedes the old one. Do not
rewrite history in a way that hides why the earlier decision was made.
