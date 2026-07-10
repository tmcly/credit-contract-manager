package br.com.creditcontract.infrastructure.stub;

import br.com.creditcontract.domain.port.CreditLimitProvider;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stub that returns a fixed credit limit (BRL 5 000,00) for every client.
 *
 * <p>In production this becomes a call to the risk-engine REST/queue API.
 */
@Component
public class StubCreditLimitProvider implements CreditLimitProvider {

	@Override
	public MonetaryAmount getLimitFor(String documentNumber) {
		return MonetaryAmount.reais(new BigDecimal("5000.00"));
	}
}
