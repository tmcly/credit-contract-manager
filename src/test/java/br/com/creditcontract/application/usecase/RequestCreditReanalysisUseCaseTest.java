package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.exception.CreditReanalysisCooldownException;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestCreditReanalysisUseCaseTest {

	private final CreditContractRepository repository = mock(CreditContractRepository.class);
	private final Clock clock = Clock.fixed(
			Instant.parse("2026-07-12T15:00:00Z"), ZoneOffset.UTC);
	private final RequestCreditReanalysisUseCase useCase =
			new RequestCreditReanalysisUseCase(repository, Duration.ofDays(30), clock);

	@Test
	void shouldPersistRequestAndReturnNextEligibleDate() {
		CreditContract contract = activeContract();
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		CreditReanalysisRequestResult result = useCase.execute(
				contract.getId(), UUID.randomUUID());

		assertEquals(LocalDateTime.of(2026, 7, 12, 15, 0), result.requestedAt());
		assertEquals(result.requestedAt().plusDays(30), result.nextEligibleAt());
		assertEquals(result.requestId(), contract.getCreditReanalyses().getFirst().getId());
		verify(repository).save(contract);
	}

	@Test
	void shouldNotPersistSecondRequestDuringCooldown() {
		CreditContract contract = activeContract();
		contract.requestCreditReanalysis(
				LocalDateTime.of(2026, 7, 1, 15, 0), Duration.ofDays(30), UUID.randomUUID());
		when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

		assertThrows(CreditReanalysisCooldownException.class,
				() -> useCase.execute(contract.getId(), UUID.randomUUID()));
		verify(repository, never()).save(contract);
	}

	private CreditContract activeContract() {
		LocalDateTime now = LocalDateTime.of(2026, 6, 1, 12, 0);
		return CreditContract.rehydrate(ContractId.generate(), "CT-2026-000802",
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.ACTIVE, MonetaryAmount.reais(new BigDecimal("5000.00")),
				now, now, 4L, List.of());
	}
}
