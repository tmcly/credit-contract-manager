package br.com.creditcontract.application.port.out;

import br.com.creditcontract.domain.valueobject.MonetaryAmount;

import java.util.Objects;

/** Mutually exclusive result returned by the credit-analysis provider. */
public sealed interface CreditAnalysisResult {

	record Approved(MonetaryAmount limit) implements CreditAnalysisResult {
		public Approved {
			Objects.requireNonNull(limit, "approved limit is required");
			if (limit.amount().signum() <= 0) {
				throw new IllegalArgumentException("approved limit must be positive");
			}
		}
	}

	record Rejected(String reason) implements CreditAnalysisResult {
		public Rejected {
			Objects.requireNonNull(reason, "rejection reason is required");
			if (reason.isBlank()) {
				throw new IllegalArgumentException("rejection reason cannot be blank");
			}
		}

	}

	static CreditAnalysisResult approved(MonetaryAmount limit) {
		return new Approved(limit);
	}

	static CreditAnalysisResult rejected(String reason) {
		return new Rejected(reason);
	}
}
