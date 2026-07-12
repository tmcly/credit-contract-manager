package br.com.creditcontract.domain.exception;

import java.time.LocalDateTime;
import java.util.Objects;

/** Raised when the client requests reanalysis before the configured cooldown ends. */
public class CreditReanalysisCooldownException extends RuntimeException {

	private final LocalDateTime nextEligibleAt;

	public CreditReanalysisCooldownException(LocalDateTime nextEligibleAt) {
		super("credit reanalysis can be requested again at " + nextEligibleAt);
		this.nextEligibleAt = Objects.requireNonNull(nextEligibleAt);
	}

	public LocalDateTime getNextEligibleAt() {
		return nextEligibleAt;
	}
}
