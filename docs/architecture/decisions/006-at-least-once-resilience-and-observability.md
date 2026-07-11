# ADR 006: At-Least-Once Resilience and Messaging Observability

- Status: Accepted
- Date: 2026-07-11

## Context

RabbitMQ and the transactional outbox guarantee durable intent and at-least-once
delivery, not exactly-once effects. A message can be delivered again after a
consumer crash, and a permanently invalid message must not retry forever.
Operators also need to follow one HTTP request through publication and analysis.

## Decision

- Store successfully consumed event IDs in `processed_messages` (the inbox).
- Record the inbox row in the same PostgreSQL transaction as the terminal
  contract state and its outgoing event.
- Treat the inbox event ID as the durable idempotency key. Existing aggregate
  state remains a secondary safeguard and supports recovery from a crash after
  entering `UNDER_REVIEW`.
- Retry consumer failures four times by default with exponential backoff, then
  reject the message so RabbitMQ routes it to a durable dead-letter queue.
- Retry outbox publication with bounded exponential backoff. After eight
  failures by default, mark the row `FAILED` for operator intervention.
- Accept or generate `X-Correlation-ID` on contract creation, return it to the
  caller, and propagate it through event metadata. Preserve the consumed event
  ID as causation metadata on analysis outcomes.
- Emit structured key-value logs and Micrometer counters, timers, and gauges for
  publication, consumption, pending outbox work, and queue depth.
- Expose Prometheus-formatted metrics through `/actuator/prometheus`.

## Consequences

### Positive

- Duplicate delivery does not repeat a completed business transition.
- Poison messages become isolated and inspectable instead of blocking useful work.
- Temporary failures retry automatically without tight loops.
- Logs and metrics expose the state and lineage of asynchronous processing.

### Trade-offs

- Inbox rows require retention and cleanup policy as volume grows.
- Retry happens in the consumer process; long retry intervals occupy a listener
  thread. A delayed-message topology can replace it if operational scale demands it.
- Queue-depth gauges query RabbitMQ when metrics are scraped and report `NaN`
  while the broker is unavailable.
- Replay is an explicit operator action because automatically replaying poison
  messages can recreate the original incident.

## Dead-letter replay procedure

1. Inspect the message, `x-death` header, application logs, and correlation ID.
2. Correct the data or deploy the code/configuration fix that caused the failure.
3. Publish the original body and application headers back to
   `credit-analysis.requests`, preserving `messageId` and `correlationId`.
4. Acknowledge/remove the DLQ copy only after the republished message is accepted.
5. Confirm the inbox row, terminal contract state, outgoing outbox event, and
   consumer-success metric. Replaying an already processed event is safe.
