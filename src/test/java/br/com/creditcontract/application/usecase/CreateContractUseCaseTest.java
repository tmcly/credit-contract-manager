package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.ClientNotFoundException;
import br.com.creditcontract.application.exception.LimitNotAvailableException;
import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import br.com.creditcontract.application.port.out.CreditLimitProvider;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateContractUseCaseTest {

	@Mock
	private ClientDataProvider clientDataProvider;
	@Mock
	private CreditLimitProvider creditLimitProvider;
	@Mock
	private ContractNumberGenerator contractNumberGenerator;

	private CreateContractUseCase useCase;

	private static final DocumentNumber DOCUMENT = DocumentNumber.from("52998224725");
	private static final Client STUB_CLIENT = new Client(
			"Alice Oliveira",
			new Address("SP", "São Paulo", "Av. Paulista", "1000", new ZipCode("01310-000"))
	);
	private static final MonetaryAmount STUB_LIMIT = MonetaryAmount.reais(new BigDecimal("5000.00"));

	@BeforeEach
	void setUp() {
		useCase = new CreateContractUseCase(clientDataProvider, creditLimitProvider, contractNumberGenerator);
	}

	@Test
	void shouldCreateContractWithAllResolvedDependencies() {
		when(clientDataProvider.findByDocument(DOCUMENT)).thenReturn(STUB_CLIENT);
		when(creditLimitProvider.getLimitFor(DOCUMENT)).thenReturn(STUB_LIMIT);
		when(contractNumberGenerator.next()).thenReturn("CT-2026-000042");

		CreditContract contract = useCase.execute(new CreateContractInput(DOCUMENT));

		assertNotNull(contract.getId());
		assertEquals("CT-2026-000042", contract.getContractNumber());
		assertEquals("Alice Oliveira", contract.getClient().name());
		assertEquals(ContractStatus.DRAFT, contract.getStatus());
		assertEquals(new BigDecimal("5000.00"), contract.getCreditLimit().amount());
		assertEquals(0L, contract.getVersion());
		assertNotNull(contract.getCreatedAt());
	}

	@Test
	void shouldRejectNullInput() {
		assertThrows(NullPointerException.class, () -> useCase.execute(null));
	}

	@Test
	void shouldPropagateClientNotFoundException() {
		when(clientDataProvider.findByDocument(DOCUMENT))
				.thenThrow(new ClientNotFoundException(DOCUMENT));

		assertThrows(ClientNotFoundException.class,
				() -> useCase.execute(new CreateContractInput(DOCUMENT)));
	}

	@Test
	void shouldPropagateLimitNotAvailableException() {
		when(clientDataProvider.findByDocument(DOCUMENT)).thenReturn(STUB_CLIENT);
		when(creditLimitProvider.getLimitFor(DOCUMENT))
				.thenThrow(new LimitNotAvailableException(DOCUMENT));

		assertThrows(LimitNotAvailableException.class,
				() -> useCase.execute(new CreateContractInput(DOCUMENT)));
	}

	@Test
	void inputShouldRejectNullDocumentNumber() {
		assertThrows(NullPointerException.class, () -> new CreateContractInput(null));
	}
}
