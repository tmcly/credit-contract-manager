package br.com.creditcontract.domain.port;

import br.com.creditcontract.domain.valueobject.MonetaryAmount;

/**
 * Obtains the credit limit for a client — the credit-engine seam.
 *
 * <p>In the real system every limit is computed by a risk engine. Here
 * the domain only declares <em>that</em> it needs a limit; the
 * infrastructure provides the concrete engine (or a stub).
 */
public interface CreditLimitProvider {

	/**
	 * Returns the approved credit limit for the given client.
	 *
	 * @param documentNumber non-null, non-blank client document identifier
	 * @return a positive monetary amount in the contract's currency
	 * @throws LimitNotAvailableException if the engine cannot determine a limit
	 */
	MonetaryAmount getLimitFor(String documentNumber);
}
