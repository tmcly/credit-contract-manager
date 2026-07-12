CREATE INDEX idx_credit_contracts_created_at_id
    ON credit_contracts (created_at DESC, id);

CREATE INDEX idx_credit_contracts_updated_at_id
    ON credit_contracts (updated_at DESC, id);

CREATE INDEX idx_credit_contracts_status_created_at_id
    ON credit_contracts (status, created_at DESC, id);
