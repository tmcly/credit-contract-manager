package br.com.creditcontract.application.port.out;

import br.com.creditcontract.application.exception.LimitNotAvailableException;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;

/**
 * Obtains the credit limit for a client — the credit-engine seam.
 *
 * <p>In the real system every limit is computed by a risk engine. Here
 * the application declares <em>that</em> it needs a limit; an outbound
 * adapter provides the concrete engine (or a stub).
 */
public interface CreditLimitProvider {

	/**
	 * Returns the approved credit limit for the given client.
	 *
	 * @param documentNumber validated client document identifier
	 * @return a positive monetary amount in the contract's currency
	 * @throws LimitNotAvailableException if the engine cannot determine a limit
	 */
	MonetaryAmount getLimitFor(DocumentNumber documentNumber);
}
