# Architecture Decision Records

Architecture Decision Records preserve the context, decision, and consequences
of choices that should survive individual tasks and conversations.

| ADR | Status | Decision |
|---|---|---|
| [001](001-postgresql-flyway-and-jpa.md) | Accepted | PostgreSQL, Flyway, and isolated JPA adapter |
| [002](002-clean-architecture-boundaries.md) | Accepted | Inward dependencies and explicit mapping boundaries |
| [003](003-contract-status-history.md) | Accepted | Generic status-transition history |
| [004](004-event-driven-evolution.md) | Accepted, implemented | Transactional outbox, RabbitMQ relay, and consumer resilience |
| [005](005-asynchronous-credit-analysis.md) | Accepted | Nullable pre-analysis limit and explicit approved/rejected outcomes |
| [006](006-at-least-once-resilience-and-observability.md) | Accepted | Inbox idempotency, bounded retries, DLQ, correlation, and messaging metrics |
| [007](007-local-prometheus-and-grafana.md) | Accepted | Reproducible local metric collection and provisioned messaging dashboards |
| [008](008-local-loki-and-alloy-log-aggregation.md) | Accepted | Local Docker log aggregation through Alloy, Loki, and Grafana |
| [009](009-structured-json-logging.md) | Accepted | Correlated JSON application logs with bounded Loki labels and sensitive-data safeguards |
| [010](010-contract-acceptance-before-activation.md) | Accepted, amended | Separate credit approval, client acceptance, and activation |
| [011](011-internal-asynchronous-contract-activation.md) | Accepted, implemented | Activate accepted contracts asynchronously inside this application |
| [012](012-synchronous-contract-blocking-command.md) | Accepted, implemented | Block active contracts through a synchronous command and emit the resulting fact |
| [013](013-synchronous-contract-unblocking-command.md) | Accepted, implemented | Unblock blocked contracts through a synchronous command and emit the resulting fact |
| [014](014-manual-and-expiring-contract-cancellation.md) | Accepted, implemented | Cancel manually by requester rules or automatically after 90 blocked days |
| [015](015-asynchronous-credit-reanalysis.md) | Accepted, implemented | Request active-contract credit reanalysis asynchronously with cooldown and audit |

When a decision changes, add a new ADR that supersedes the old one. Do not
rewrite history in a way that hides why the earlier decision was made.
