package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.springframework.stereotype.Component;

/**
 * Stub that returns a hardcoded client regardless of the document number.
 *
 * <p>In production this becomes an HTTP call to the client-registry REST API.
 */
@Component
public class StubClientDataProvider implements ClientDataProvider {

	@Override
	public Client findByDocument(DocumentNumber documentNumber) {
		return new Client(
				"Stub Client",
				new Address("PR", "Curitiba", "Rua XV de Novembro", "1000",
						new ZipCode("80020-310"))
		);
	}
}
