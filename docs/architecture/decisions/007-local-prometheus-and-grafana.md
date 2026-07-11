# ADR 007: Local Prometheus and Grafana Observability Stack

- Status: Accepted
- Date: 2026-07-11

## Context

ADR 006 exposes application and messaging metrics through Micrometer and the
Prometheus actuator endpoint. A text endpoint is useful to collectors but does
not provide historical exploration or an accessible operational dashboard.

The project needs a reproducible, zero-manual-setup local observability path
that remains proportional to a learning-oriented modular monolith.

## Decision

- Run Prometheus and Grafana as local Docker Compose services.
- Let Prometheus scrape `credit-contract-manager:8080/actuator/prometheus`
  every five seconds and retain local data for seven days.
- Provision Grafana's Prometheus datasource and the initial messaging dashboard
  from version-controlled files.
- Persist Prometheus and Grafana runtime data in named Docker volumes while
  keeping configuration and dashboard definitions in the repository.
- Pin container image versions instead of relying on mutable `latest` tags.
- Use development-only Grafana credentials that can be overridden by environment
  variables. Production authentication and secrets remain out of scope.
- Do not add Loki in this increment. Loki is a separate log-storage backend,
  even though it is developed by Grafana Labs and visualized through Grafana.

## Consequences

### Positive

- A single `docker compose up` starts metrics production, collection, storage,
  querying, and visualization.
- Dashboards are reproducible, reviewable, and available without UI setup.
- Prometheus history makes rates and latency trends visible beyond the current
  application process lifetime.

### Trade-offs

- Local startup consumes more memory, CPU, disk, and container ports.
- Named volumes preserve state until explicitly removed with
  `docker compose down -v`.
- The initial dashboard covers messaging metrics only; alerts, traces, and logs
  require later increments.
- The default local Grafana password is intentionally not production-safe.

## Later amendment

ADR 008 adds Loki and Grafana Alloy in a subsequent increment. The Loki
exclusion above describes this ADR's original scope and is retained as decision
history.
