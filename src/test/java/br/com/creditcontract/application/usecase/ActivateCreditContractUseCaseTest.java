package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.application.port.out.ProcessedMessageStore;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractActivated;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivateCreditContractUseCaseTest {

	private final CreditContractRepository repository = mock(CreditContractRepository.class);
	private final ProcessedMessageStore processedMessageStore = mock(ProcessedMessageStore.class);
	private final ActivateCreditContractUseCase useCase =
			new ActivateCreditContractUseCase(repository, processedMessageStore);

	@Test
	void shouldActivateAcceptedContractAndRecordConsumedEvent() {
		CreditContract contract = contractIn(ContractStatus.ACCEPTED);
		ActivateCreditContractCommand command = new ActivateCreditContractCommand(
				contract.getId(), UUID.randomUUID(), UUID.randomUUID());
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		useCase.execute(command);

		assertEquals(ContractStatus.ACTIVE, contract.getStatus());
		CreditContractActivated event = assertInstanceOf(
				CreditContractActivated.class, contract.getDomainEvents().getFirst());
		assertEquals(command.correlationId(), event.correlationId());
		assertEquals(command.eventId(), event.causationId());
		verify(repository).save(contract);
		verify(processedMessageStore).record(
				command.eventId(), "contract-activation", contract.getId(), command.correlationId());
	}

	@Test
	void shouldIgnoreMessageAlreadyInInbox() {
		ActivateCreditContractCommand command = new ActivateCreditContractCommand(
				ContractId.generate(), UUID.randomUUID(), UUID.randomUUID());
		when(processedMessageStore.contains(command.eventId())).thenReturn(true);

		useCase.execute(command);

		verify(repository, never()).findById(command.contractId());
		verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void shouldRecordRedeliveryWhenContractIsAlreadyActive() {
		CreditContract contract = contractIn(ContractStatus.ACTIVE);
		ActivateCreditContractCommand command = new ActivateCreditContractCommand(
				contract.getId(), UUID.randomUUID(), UUID.randomUUID());
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		useCase.execute(command);

		verify(repository, never()).save(contract);
		verify(processedMessageStore).record(
				command.eventId(), "contract-activation", contract.getId(), command.correlationId());
	}

	private CreditContract contractIn(ContractStatus status) {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(
				ContractId.generate(),
				"CT-2026-000400",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				status,
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				now,
				now,
				3L,
				List.of());
	}
}
