package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.CreditAnalysisApproved;
import br.com.creditcontract.domain.event.CreditAnalysisRejected;
import br.com.creditcontract.domain.event.EventContext;
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

import static org.junit.jupiter.api.Assertions.*;

class CreditContractTest {

	private CreditContract sample() {
		return CreditContract.create(
				ContractId.generate(),
				"CT-2026-000001",
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000")))
		);
	}

	@Test
	void create_starts_as_draft_with_version_zero() {
		CreditContract contract = sample();
		assertEquals(ContractStatus.DRAFT, contract.getStatus());
		assertNull(contract.getCreditLimit());
		assertEquals(0L, contract.getVersion());
		assertNotNull(contract.getCreatedAt());
		assertNotNull(contract.getUpdatedAt());
		assertEquals(1, contract.getStatusHistory().size());
		assertNull(contract.getStatusHistory().getFirst().previousStatus());
		assertEquals(ContractStatus.DRAFT, contract.getStatusHistory().getFirst().newStatus());
		assertEquals(1, contract.getDomainEvents().size());
		CreditContractCreated event = (CreditContractCreated) contract.getDomainEvents().getFirst();
		assertEquals(contract.getId(), event.aggregateId());
		assertEquals(contract.getContractNumber(), event.contractNumber());
		assertEquals(contract.getClient().documentNumber(), event.clientDocumentNumber());
		assertEquals(contract.getCreatedAt(), event.occurredAt());
		assertEquals(event.eventId(), event.correlationId());
		assertNull(event.causationId());
		assertEquals("CreditContract", event.aggregateType());
		assertEquals("CreditContractCreated", event.eventType());
		assertEquals(1, event.schemaVersion());
	}

	@Test
	void cannot_create_without_client() {
		assertThrows(NullPointerException.class, () -> CreditContract.create(
				ContractId.generate(),
				"CT-2026-000002",
				null
		));
	}

	@Test
	void shouldApproveOnlyAfterAnalysisStartsAndRecordOutcomeEvent() {
		CreditContract contract = sample();
		EventContext context = new EventContext(
				java.util.UUID.randomUUID(), java.util.UUID.randomUUID());

		contract.startCreditAnalysis();
		contract.approveCreditAnalysis(
				MonetaryAmount.reais(new BigDecimal("5000.00")), context);

		assertEquals(ContractStatus.APPROVED, contract.getStatus());
		assertEquals(new BigDecimal("5000.00"), contract.getCreditLimit().amount());
		assertEquals(3, contract.getStatusHistory().size());
		assertEquals(ContractStatus.UNDER_REVIEW,
				contract.getStatusHistory().get(1).newStatus());
		assertEquals(ContractStatus.APPROVED,
				contract.getStatusHistory().get(2).newStatus());
		CreditAnalysisApproved event = assertInstanceOf(
				CreditAnalysisApproved.class,
				contract.getDomainEvents().get(1));
		assertEquals(context.correlationId(), event.correlationId());
		assertEquals(context.causationId(), event.causationId());
	}

	@Test
	void shouldRejectOnlyAfterAnalysisStartsAndKeepReasonInHistory() {
		CreditContract contract = sample();
		EventContext context = new EventContext(
				java.util.UUID.randomUUID(), java.util.UUID.randomUUID());

		contract.startCreditAnalysis();
		contract.rejectCreditAnalysis("Credit policy criteria not met", context);

		assertEquals(ContractStatus.REJECTED, contract.getStatus());
		assertNull(contract.getCreditLimit());
		assertEquals("Credit policy criteria not met",
				contract.getStatusHistory().getLast().reason());
		assertInstanceOf(CreditAnalysisRejected.class, contract.getDomainEvents().get(1));
	}

	@Test
	void shouldRejectIllegalAnalysisTransitions() {
		CreditContract contract = sample();
		EventContext context = new EventContext(
				java.util.UUID.randomUUID(), java.util.UUID.randomUUID());

		assertThrows(InvalidContractTransitionException.class,
				() -> contract.approveCreditAnalysis(
						MonetaryAmount.reais(new BigDecimal("5000.00")), context));
		contract.startCreditAnalysis();
		assertThrows(InvalidContractTransitionException.class, contract::startCreditAnalysis);
		assertThrows(IllegalArgumentException.class,
				() -> contract.approveCreditAnalysis(
						MonetaryAmount.reais(BigDecimal.ZERO), context));
	}

	@Test
	void negative_monetary_amount_throws() {
		assertThrows(IllegalArgumentException.class,
				() -> MonetaryAmount.reais(new BigDecimal("-1.00")));
	}

	@Test
	void invalid_zip_code_throws() {
		assertThrows(IllegalArgumentException.class, () -> new ZipCode("123"));
	}
}
