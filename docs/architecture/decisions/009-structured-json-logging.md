# ADR 009: Structured JSON Application Logging

- Status: Accepted
- Date: 2026-07-11
- Amends: ADR 008

## Context

ADR 008 established local collection with Alloy and Loki but deliberately kept
the container log lines unchanged. Application messages therefore remained
human-oriented text: following one request across HTTP, outbox publication, and
RabbitMQ consumption depended on regular-expression searches instead of stable
fields.

The credit domain also contains personal and financial data. Improving search
must not make CPF, client snapshots, payloads, credit limits, rejection reasons,
or exception messages easier to leak into the logging platform.

## Decision

- Emit each Spring Boot application log as one JSON object through Logback and
  `logstash-logback-encoder`.
- Use stable operational fields such as `event`, `correlationId`, `eventId`,
  `contractId`, `eventType`, statuses, HTTP route, status, and duration.
- Accept an optional UUID `X-Correlation-ID` on every API request. Generate one
  when absent, return the effective value in the response, and keep it in MDC
  for the duration of request processing.
- Emit one completion log per business HTTP request. Exclude health and
  Actuator endpoints to avoid telemetry feedback noise.
- Log business lifecycle facts at application boundaries and messaging facts in
  their adapters. The domain model remains independent of logging technology.
- Do not log CPF/document numbers, client data, addresses, broker payloads,
  credit limits, analysis reasons, or raw exception messages.
- Parse application JSON in Alloy. Promote only bounded fields (`level` and
  `event`) to Loki labels; IDs remain JSON fields queried with LogQL to avoid
  high-cardinality indexes.
- Provision Grafana filters for level, event, correlation ID, contract ID, and
  free text.

## Consequences

### Positive

- One correlation ID follows contract creation through outbox and consumption.
- Operators can filter by named events and identifiers without fragile text
  patterns.
- The repository carries an explicit minimum logging-data policy.
- Infrastructure logs remain collectible even when they are not JSON.

### Trade-offs

- The application gains a runtime encoder dependency.
- Logging fields are an operational contract that should be changed
  deliberately.
- JSON logs are optimized for collectors and are less compact in a plain local
  terminal.
- This policy reduces obvious exposure but does not replace a broader LGPD,
  retention, and access-control review.
