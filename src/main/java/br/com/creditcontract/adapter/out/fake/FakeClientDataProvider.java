package br.com.creditcontract.adapter.out.fake;

import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import net.datafaker.Faker;
import net.datafaker.service.RandomService;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Local client-registry fake backed by reproducible Brazilian fake data.
 *
 * <p>Each normalized CPF becomes the seed of a new generator. This makes
 * different documents produce varied snapshots while guaranteeing that the
 * same document produces the same client across calls, instances and runs.
 */
@Component
public class FakeClientDataProvider implements ClientDataProvider {

	private static final Locale BRAZILIAN_PORTUGUESE = Locale.forLanguageTag("pt-BR");

	@Override
	public Client findByDocument(DocumentNumber documentNumber) {
		Objects.requireNonNull(documentNumber, "document number cannot be null");

		Faker faker = fakerFor(documentNumber);
		return new Client(
				documentNumber,
				faker.name().fullName(),
				new Address(
						faker.address().stateAbbr(),
						faker.address().city(),
						faker.address().streetName(),
						faker.address().buildingNumber(),
						new ZipCode(faker.address().zipCode())));
	}

	private Faker fakerFor(DocumentNumber documentNumber) {
		long seed = Long.parseLong(documentNumber.value());
		return new Faker(
				BRAZILIAN_PORTUGUESE,
				new RandomService(new Random(seed)));
	}
}
