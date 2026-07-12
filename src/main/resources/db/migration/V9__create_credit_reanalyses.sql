CREATE TABLE credit_reanalyses (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    previous_limit NUMERIC(19, 2) NOT NULL,
    new_limit NUMERIC(19, 2),
    reason VARCHAR(255),
    requested_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_credit_reanalyses_contract
        FOREIGN KEY (contract_id) REFERENCES credit_contracts (id) ON DELETE CASCADE,
    CONSTRAINT chk_credit_reanalyses_status
        CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_credit_reanalyses_previous_limit
        CHECK (previous_limit > 0),
    CONSTRAINT chk_credit_reanalyses_new_limit
        CHECK (new_limit IS NULL OR new_limit > 0),
    CONSTRAINT chk_credit_reanalyses_outcome
        CHECK (
            (status = 'REQUESTED'
                AND new_limit IS NULL AND reason IS NULL AND completed_at IS NULL)
            OR (status = 'APPROVED'
                AND new_limit > previous_limit AND reason IS NULL AND completed_at IS NOT NULL)
            OR (status = 'REJECTED'
                AND new_limit IS NOT NULL AND reason IS NOT NULL AND completed_at IS NOT NULL)
        )
);

CREATE INDEX idx_credit_reanalyses_contract_requested_at
    ON credit_reanalyses (contract_id, requested_at DESC);
