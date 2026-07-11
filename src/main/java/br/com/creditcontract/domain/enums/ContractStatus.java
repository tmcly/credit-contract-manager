package br.com.creditcontract.domain.enums;

/**
 * Lifecycle states of a credit contract (state machine).
 *
 * The enum only lists the states. Which transitions are legal is enforced
 * by {@link br.com.creditcontract.domain.entity.CreditContract}
 * (e.g. you cannot block a cancelled contract). Adjust this set as the real
 * business rules are confirmed.
 */
public enum ContractStatus {
	DRAFT,
	UNDER_REVIEW,
	APPROVED,
	REJECTED,
	ACCEPTED,
	ACTIVE,
	BLOCKED,
	CANCELLED
}
