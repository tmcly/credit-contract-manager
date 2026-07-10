CREATE TABLE credit_contracts (
    id UUID PRIMARY KEY,
    contract_number VARCHAR(30) NOT NULL UNIQUE,
    client_document_number VARCHAR(11) NOT NULL,
    client_name VARCHAR(150) NOT NULL,
    client_state VARCHAR(2) NOT NULL,
    client_city VARCHAR(100) NOT NULL,
    client_street VARCHAR(150) NOT NULL,
    client_address_number VARCHAR(20) NOT NULL,
    client_zip_code VARCHAR(8) NOT NULL,
    status VARCHAR(30) NOT NULL,
    credit_limit NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_credit_contract_document_number
        CHECK (client_document_number ~ '^[0-9]{11}$'),
    CONSTRAINT chk_credit_contract_status
        CHECK (status IN ('DRAFT', 'UNDER_REVIEW', 'APPROVED', 'ACTIVE', 'BLOCKED', 'CANCELLED')),
    CONSTRAINT chk_credit_contract_limit
        CHECK (credit_limit >= 0)
);

CREATE INDEX idx_credit_contracts_client_document_number
    ON credit_contracts (client_document_number);

CREATE TABLE contract_status_history (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(255),
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_contract_status_history_contract
        FOREIGN KEY (contract_id) REFERENCES credit_contracts (id) ON DELETE CASCADE,
    CONSTRAINT chk_contract_status_history_previous_status
        CHECK (previous_status IS NULL OR previous_status IN
            ('DRAFT', 'UNDER_REVIEW', 'APPROVED', 'ACTIVE', 'BLOCKED', 'CANCELLED')),
    CONSTRAINT chk_contract_status_history_new_status
        CHECK (new_status IN
            ('DRAFT', 'UNDER_REVIEW', 'APPROVED', 'ACTIVE', 'BLOCKED', 'CANCELLED')),
    CONSTRAINT chk_contract_status_history_changed_status
        CHECK (previous_status IS NULL OR previous_status <> new_status)
);

CREATE INDEX idx_contract_status_history_contract_date
    ON contract_status_history (contract_id, changed_at);
