# Architecture Decision Records

Architecture Decision Records preserve the context, decision, and consequences
of choices that should survive individual tasks and conversations.

| ADR | Status | Decision |
|---|---|---|
| [001](001-postgresql-flyway-and-jpa.md) | Accepted | PostgreSQL, Flyway, and isolated JPA adapter |
| [002](002-clean-architecture-boundaries.md) | Accepted | Inward dependencies and explicit mapping boundaries |
| [003](003-contract-status-history.md) | Accepted | Generic status-transition history |
| [004](004-event-driven-evolution.md) | Accepted, pending implementation | Transactional outbox and RabbitMQ-first event architecture |

When a decision changes, add a new ADR that supersedes the old one. Do not
rewrite history in a way that hides why the earlier decision was made.
