package br.com.creditcontract.adapter.out.fake;

import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeClientDataProviderTest {

	private static final DocumentNumber CPF = DocumentNumber.from("52998224725");

	private final FakeClientDataProvider provider = new FakeClientDataProvider();

	@Test
	void shouldGenerateTheSameSnapshotForTheSameCpfAcrossCallsAndInstances() {
		Client firstSnapshot = provider.findByDocument(CPF);
		Client repeatedSnapshot = provider.findByDocument(CPF);
		Client snapshotFromAnotherInstance = new FakeClientDataProvider().findByDocument(CPF);

		assertEquals(firstSnapshot, repeatedSnapshot);
		assertEquals(firstSnapshot, snapshotFromAnotherInstance);
		assertEquals(
				new Client(
						CPF,
						"Daniel Caldas",
						new Address(
								"PB",
								"Marliéria",
								"Alameda Laís",
								"784",
								new ZipCode("51192-726"))),
				firstSnapshot);
	}

	@Test
	void shouldNormallyGenerateDifferentSnapshotsForDifferentCpfs() {
		List<Client> snapshots = List.of(
				provider.findByDocument(DocumentNumber.from("52998224725")),
				provider.findByDocument(DocumentNumber.from("11144477735")),
				provider.findByDocument(DocumentNumber.from("10000000280")),
				provider.findByDocument(DocumentNumber.from("10000000361")));

		assertEquals(snapshots.size(), snapshots.stream().map(Client::name).distinct().count());
		assertEquals(snapshots.size(), snapshots.stream().map(Client::address).distinct().count());
		assertNotEquals(snapshots.get(0).name(), snapshots.get(1).name());
	}

	@Test
	void shouldGenerateAValidBrazilianClientSnapshot() {
		Client client = provider.findByDocument(CPF);

		assertEquals(CPF, client.documentNumber());
		assertFalse(client.name().isBlank());
		assertTrue(client.address().state().matches("[A-Z]{2}"));
		assertFalse(client.address().city().isBlank());
		assertFalse(client.address().street().isBlank());
		assertFalse(client.address().number().isBlank());
		assertTrue(client.address().zipCode().value().matches("\\d{5}-?\\d{3}"));
	}

	@Test
	void shouldRejectNullDocumentNumber() {
		assertThrows(NullPointerException.class, () -> provider.findByDocument(null));
	}
}
