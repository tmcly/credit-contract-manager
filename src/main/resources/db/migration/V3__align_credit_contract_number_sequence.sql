DO $$
DECLARE
    highest_persisted_value BIGINT;
    current_sequence_value BIGINT;
    sequence_was_called BOOLEAN;
BEGIN
    SELECT MAX(SUBSTRING(contract_number FROM '^CT-[0-9]{4}-([0-9]+)$')::BIGINT)
      INTO highest_persisted_value
      FROM credit_contracts
     WHERE contract_number ~ '^CT-[0-9]{4}-[0-9]+$';

    IF highest_persisted_value IS NULL THEN
        RETURN;
    END IF;

    SELECT last_value, is_called
      INTO current_sequence_value, sequence_was_called
      FROM credit_contract_number_seq;

    PERFORM setval(
            'credit_contract_number_seq',
            GREATEST(
                    highest_persisted_value,
                    CASE WHEN sequence_was_called THEN current_sequence_value ELSE 0 END),
            TRUE);
END
$$;
