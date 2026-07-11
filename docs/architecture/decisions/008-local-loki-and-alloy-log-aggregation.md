# ADR 008: Local Loki and Grafana Alloy Log Aggregation

- Status: Accepted
- Date: 2026-07-11
- Amends: ADR 007

## Context

ADR 007 deliberately limited the first local observability increment to metrics.
Application and infrastructure logs still required separate `docker compose logs`
commands and could not be searched historically or correlated in Grafana.

Promtail is no longer an appropriate new collector: it was deprecated in Loki
3.0 and removed in Loki 3.7.3, with its log-collection capabilities moved to
Grafana Alloy.

## Decision

- Run Loki in single-binary mode with local filesystem storage for development.
- Retain local logs for seven days, aligned with Prometheus metric retention.
- Use Grafana Alloy to discover Docker containers, read their log streams, add
  stable container and Compose service labels, and push entries to Loki.
- Mount the Docker socket read-only into Alloy. This is limited to the local
  development environment and must not be copied into production without a
  dedicated security review.
- Provision Loki as a Grafana datasource and version a **Credit Contract Logs**
  dashboard with service filtering and free-text/correlation-ID search.
- Keep logs as emitted by the containers. Structured JSON logging, redaction,
  and production shipping remain separate decisions.
- Pin Loki and Alloy image versions instead of using mutable tags.

This ADR removes ADR 007's temporary Loki non-goal without changing its
Prometheus and Grafana decisions.

## Consequences

### Positive

- Metrics and logs are explored from the same Grafana instance.
- Developers can search asynchronous processing by `correlationId` without
  manually combining output from several containers.
- All Compose service logs become available without application coupling to Loki.
- Alloy follows the supported Grafana collection path after Promtail removal.

### Trade-offs

- Two more containers and named volumes increase local resource consumption.
- Docker socket access is powerful even when mounted read-only and is unsuitable
  as a production default.
- Filesystem-backed, single-binary Loki is intentionally local-only and has no
  high availability.
- Current logfmt-style messages support text search, but JSON logging would
  enable richer field parsing and should include explicit CPF/LGPD safeguards.
