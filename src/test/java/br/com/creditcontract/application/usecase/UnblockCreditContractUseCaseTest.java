package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractUnblocked;
import br.com.creditcontract.domain.exception.InvalidContractTransitionException;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnblockCreditContractUseCaseTest {

	private final CreditContractRepository repository = mock(CreditContractRepository.class);
	private final UnblockCreditContractUseCase useCase = new UnblockCreditContractUseCase(repository);

	@Test
	void shouldUnblockBlockedContractAndPersistIt() {
		CreditContract contract = contractIn(ContractStatus.BLOCKED);
		UUID correlationId = UUID.randomUUID();
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		CreditContract result = useCase.execute(
				contract.getId(), "Balance settled", correlationId);

		assertEquals(ContractStatus.ACTIVE, result.getStatus());
		CreditContractUnblocked event = assertInstanceOf(
				CreditContractUnblocked.class, result.getDomainEvents().getFirst());
		assertEquals(correlationId, event.correlationId());
		verify(repository).save(contract);
	}

	@Test
	void shouldRejectUnblockingWhenContractIsNotBlocked() {
		CreditContract contract = contractIn(ContractStatus.ACTIVE);
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		assertThrows(InvalidContractTransitionException.class, () -> useCase.execute(
				contract.getId(), "Repeated request", UUID.randomUUID()));

		verify(repository, never()).save(contract);
	}

	private CreditContract contractIn(ContractStatus status) {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(
				ContractId.generate(),
				"CT-2026-000600",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				status,
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				now,
				now,
				5L,
				List.of());
	}
}
