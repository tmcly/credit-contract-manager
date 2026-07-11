package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateContractUseCaseTest {

	@Mock
	private ClientDataProvider clientDataProvider;
	@Mock
	private ContractNumberGenerator contractNumberGenerator;
	@Mock
	private CreditContractRepository creditContractRepository;

	private CreateContractUseCase useCase;

	private static final DocumentNumber CPF = DocumentNumber.from("52998224725");
	private static final Client STUB_CLIENT = new Client(
			CPF,
			"Alice Oliveira",
			new Address("SP", "São Paulo", "Av. Paulista", "1000", new ZipCode("01310-000"))
	);
	@BeforeEach
	void setUp() {
		useCase = new CreateContractUseCase(
				clientDataProvider,
				contractNumberGenerator,
				creditContractRepository);
	}

	@Test
	void shouldCreateContractWithAllResolvedDependencies() {
		when(clientDataProvider.findByDocument(CPF)).thenReturn(STUB_CLIENT);
		when(contractNumberGenerator.next()).thenReturn("CT-2026-000042");

		CreditContract contract = useCase.execute(new CreateContractInput(CPF));

		assertNotNull(contract.getId());
		assertEquals("CT-2026-000042", contract.getContractNumber());
		assertEquals("Alice Oliveira", contract.getClient().name());
		assertEquals(ContractStatus.DRAFT, contract.getStatus());
		assertNull(contract.getCreditLimit());
		assertEquals(0L, contract.getVersion());
		assertNotNull(contract.getCreatedAt());
		assertEquals(1, contract.getDomainEvents().size());
		assertEquals(CreditContractCreated.EVENT_TYPE,
				contract.getDomainEvents().getFirst().eventType());
		verify(creditContractRepository).save(contract);
	}

	@Test
	void shouldRejectNullInput() {
		assertThrows(NullPointerException.class, () -> useCase.execute(null));
	}

	@Test
	void shouldPropagateClientNotFoundException() {
		when(clientDataProvider.findByDocument(CPF))
				.thenThrow(new ClientNotFoundException(CPF));

		assertThrows(ClientNotFoundException.class,
				() -> useCase.execute(new CreateContractInput(CPF)));
	}

	@Test
	void inputShouldRejectNullCpf() {
		assertThrows(NullPointerException.class, () -> new CreateContractInput(null));
	}
}
