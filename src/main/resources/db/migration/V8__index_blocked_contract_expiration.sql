CREATE INDEX idx_credit_contracts_blocked_expiration
    ON credit_contracts (updated_at, id)
    WHERE status = 'BLOCKED';
