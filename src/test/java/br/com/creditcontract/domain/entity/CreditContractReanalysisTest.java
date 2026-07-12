package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.event.CreditReanalysisApproved;
import br.com.creditcontract.domain.event.CreditReanalysisRejected;
import br.com.creditcontract.domain.event.CreditReanalysisRequested;
import br.com.creditcontract.domain.event.EventContext;
import br.com.creditcontract.domain.exception.CreditReanalysisCooldownException;
import br.com.creditcontract.domain.exception.CreditReanalysisNotAllowedException;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditContractReanalysisTest {

	private static final Duration COOLDOWN = Duration.ofDays(30);

	@Test
	void shouldRequestAndApproveReanalysisWithoutChangingActiveStatus() {
		CreditContract contract = activeContract();
		LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 12, 12, 0);
		UUID correlationId = UUID.randomUUID();

		CreditReanalysis request = contract.requestCreditReanalysis(
				requestedAt, COOLDOWN, correlationId);

		assertEquals(ContractStatus.ACTIVE, contract.getStatus());
		assertEquals(CreditReanalysisStatus.REQUESTED, request.getStatus());
		CreditReanalysisRequested requestedEvent = assertInstanceOf(
				CreditReanalysisRequested.class, contract.getDomainEvents().getFirst());
		assertEquals(request.getId(), requestedEvent.eventId());
		assertEquals(correlationId, requestedEvent.correlationId());

		EventContext context = new EventContext(correlationId, request.getId());
		contract.approveCreditReanalysisRequest(
				request.getId(), money("7500.00"), requestedAt.plusMinutes(1), context);

		assertEquals(ContractStatus.ACTIVE, contract.getStatus());
		assertEquals(money("7500.00"), contract.getCreditLimit());
		assertEquals(CreditReanalysisStatus.APPROVED, request.getStatus());
		CreditReanalysisApproved approvedEvent = assertInstanceOf(
				CreditReanalysisApproved.class, contract.getDomainEvents().getLast());
		assertEquals(money("5000.00"), approvedEvent.previousLimit());
		assertEquals(money("7500.00"), approvedEvent.newLimit());
		assertEquals(request.getId(), approvedEvent.causationId());
	}

	@Test
	void shouldRejectRequestsDuringCooldownAndAllowExactBoundary() {
		CreditContract contract = activeContract();
		LocalDateTime firstRequest = LocalDateTime.of(2026, 7, 12, 12, 0);
		contract.requestCreditReanalysis(firstRequest, COOLDOWN, UUID.randomUUID());

		CreditReanalysisCooldownException exception = assertThrows(
				CreditReanalysisCooldownException.class,
				() -> contract.requestCreditReanalysis(
						firstRequest.plusDays(29), COOLDOWN, UUID.randomUUID()));
		assertEquals(firstRequest.plusDays(30), exception.getNextEligibleAt());

		CreditReanalysis second = contract.requestCreditReanalysis(
				firstRequest.plusDays(30), COOLDOWN, UUID.randomUUID());
		assertEquals(CreditReanalysisStatus.REQUESTED, second.getStatus());
		assertEquals(2, contract.getCreditReanalyses().size());
	}

	@Test
	void shouldRejectReanalysisWhenContractIsNotActive() {
		CreditContract blocked = activeContract();
		blocked.block("Payment overdue", UUID.randomUUID());

		assertThrows(CreditReanalysisNotAllowedException.class,
				() -> blocked.requestCreditReanalysis(
						LocalDateTime.now(), COOLDOWN, UUID.randomUUID()));
		assertTrue(blocked.getCreditReanalyses().isEmpty());
	}

	@Test
	void shouldRetainLimitWhenProviderRejectsOrContractStopsBeingActive() {
		CreditContract rejected = activeContract();
		LocalDateTime requestedAt = LocalDateTime.now();
		CreditReanalysis request = rejected.requestCreditReanalysis(
				requestedAt, COOLDOWN, UUID.randomUUID());
		EventContext context = new EventContext(UUID.randomUUID(), request.getId());

		rejected.rejectCreditReanalysisRequest(
				request.getId(), "Policy criteria not met", requestedAt.plusMinutes(1), context);

		assertEquals(money("5000.00"), rejected.getCreditLimit());
		assertEquals(CreditReanalysisStatus.REJECTED, request.getStatus());
		CreditReanalysisRejected event = assertInstanceOf(
				CreditReanalysisRejected.class, rejected.getDomainEvents().getLast());
		assertEquals(event.previousLimit(), event.retainedLimit());

		CreditContract blocked = activeContract();
		CreditReanalysis pending = blocked.requestCreditReanalysis(
				requestedAt, COOLDOWN, UUID.randomUUID());
		blocked.block("Payment overdue", UUID.randomUUID());
		blocked.approveCreditReanalysisRequest(
				pending.getId(), money("10000.00"), requestedAt.plusMinutes(2),
				new EventContext(UUID.randomUUID(), pending.getId()));
		assertEquals(ContractStatus.BLOCKED, blocked.getStatus());
		assertEquals(money("5000.00"), blocked.getCreditLimit());
		assertEquals(CreditReanalysisStatus.REJECTED, pending.getStatus());
	}

	private CreditContract activeContract() {
		LocalDateTime now = LocalDateTime.of(2026, 7, 1, 12, 0);
		return CreditContract.rehydrate(
				ContractId.generate(), "CT-2026-000801",
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				ContractStatus.ACTIVE, money("5000.00"), now, now, 4L, List.of());
	}

	private MonetaryAmount money(String amount) {
		return MonetaryAmount.reais(new BigDecimal(amount));
	}
}
