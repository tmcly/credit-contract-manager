package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.CancellationOrigin;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCancelled;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelExpiredBlockedContractsUseCaseTest {

	@Test
	void shouldCancelOnlyContractsReturnedByExpirationQuery() {
		CreditContractRepository repository = mock(CreditContractRepository.class);
		CancelExpiredBlockedContractsUseCase useCase =
				new CancelExpiredBlockedContractsUseCase(repository);
		LocalDateTime cutoff = LocalDateTime.of(2026, 4, 13, 12, 0);
		CreditContract blocked = blockedContract();
		UUID correlationId = UUID.randomUUID();
		when(repository.findBlockedUpdatedBefore(cutoff, 50)).thenReturn(List.of(blocked));

		assertEquals(1, useCase.execute(cutoff, 50, correlationId));
		assertEquals(ContractStatus.CANCELLED, blocked.getStatus());
		CreditContractCancelled event = assertInstanceOf(
				CreditContractCancelled.class, blocked.getDomainEvents().getFirst());
		assertEquals(CancellationOrigin.BLOCKED_EXPIRATION, event.origin());
		assertEquals(correlationId, event.correlationId());
		verify(repository).save(blocked);
	}

	private CreditContract blockedContract() {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(ContractId.generate(), "CT-2026-000701",
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				ContractStatus.BLOCKED, MonetaryAmount.reais(new BigDecimal("5000.00")),
				now, now, 6L, List.of());
	}
}
