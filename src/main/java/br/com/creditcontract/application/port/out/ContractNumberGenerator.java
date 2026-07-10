package br.com.creditcontract.application.port.out;

/**
 * Generates sequential, human-readable contract numbers.
 *
 * <p>The format is deliberately opaque to the domain — only the
 * outbound adapter knows whether numbers come from a database sequence,
 * Redis INCR, or a distributed ID generator. The domain just calls
 * {@link #next()} and receives a non-null, non-blank string.
 */
public interface ContractNumberGenerator {

	/**
	 * Returns the next available contract number.
	 *
	 * @return a non-null, non-blank contract number (e.g. {@code "CT-2026-000042"})
	 */
	String next();
}
