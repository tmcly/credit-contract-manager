ALTER TABLE credit_contracts
    DROP CONSTRAINT chk_credit_contract_status,
    DROP CONSTRAINT chk_credit_contract_limit;

ALTER TABLE contract_status_history
    DROP CONSTRAINT chk_contract_status_history_previous_status,
    DROP CONSTRAINT chk_contract_status_history_new_status;

ALTER TABLE credit_contracts
    ADD CONSTRAINT chk_credit_contract_status
        CHECK (status IN (
            'DRAFT', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ACCEPTED',
            'ACTIVE', 'BLOCKED', 'CANCELLED')),
    ADD CONSTRAINT chk_credit_contract_limit
        CHECK (
            (status IN ('DRAFT', 'UNDER_REVIEW', 'REJECTED') AND credit_limit IS NULL)
            OR (status IN ('APPROVED', 'ACCEPTED', 'ACTIVE', 'BLOCKED') AND credit_limit > 0)
            OR status = 'CANCELLED'
        );

ALTER TABLE contract_status_history
    ADD CONSTRAINT chk_contract_status_history_previous_status
        CHECK (previous_status IS NULL OR previous_status IN (
            'DRAFT', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ACCEPTED',
            'ACTIVE', 'BLOCKED', 'CANCELLED')),
    ADD CONSTRAINT chk_contract_status_history_new_status
        CHECK (new_status IN (
            'DRAFT', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ACCEPTED',
            'ACTIVE', 'BLOCKED', 'CANCELLED'));
