CREATE TABLE outbox_events (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    payload JSONB NOT NULL,
    schema_version INTEGER NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    correlation_id UUID NOT NULL,
    causation_id UUID,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    publication_attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    published_at TIMESTAMP,
    last_error VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_outbox_events_schema_version
        CHECK (schema_version > 0),
    CONSTRAINT chk_outbox_events_publication_status
        CHECK (publication_status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT chk_outbox_events_publication_attempts
        CHECK (publication_attempts >= 0)
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (occurred_at)
    WHERE publication_status = 'PENDING';

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id, occurred_at);
