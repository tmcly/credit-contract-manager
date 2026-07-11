CREATE TABLE processed_messages (
    event_id UUID PRIMARY KEY,
    consumer_name VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    correlation_id UUID NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_processed_messages_consumer_processed
    ON processed_messages (consumer_name, processed_at);
