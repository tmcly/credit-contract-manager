package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.event.CreditContractCreated;
import br.com.creditcontract.domain.event.CreditContractAccepted;
import br.com.creditcontract.domain.event.CreditContractActivated;
import br.com.creditcontract.domain.event.CreditContractBlocked;
import br.com.creditcontract.domain.event.CreditContractUnblocked;
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
import java.util.UUID;

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
	void shouldAcceptOnlyApprovedContractAndRecordOutcomeEvent() {
		CreditContract contract = sample();
		EventContext analysisContext = new EventContext(UUID.randomUUID(), UUID.randomUUID());
		UUID acceptanceCorrelationId = UUID.randomUUID();
		contract.startCreditAnalysis();
		contract.approveCreditAnalysis(
				MonetaryAmount.reais(new BigDecimal("5000.00")), analysisContext);

		contract.accept(acceptanceCorrelationId);

		assertEquals(ContractStatus.ACCEPTED, contract.getStatus());
		assertEquals(new BigDecimal("5000.00"), contract.getCreditLimit().amount());
		assertEquals(ContractStatus.APPROVED,
				contract.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.ACCEPTED,
				contract.getStatusHistory().getLast().newStatus());
		assertEquals("Contract accepted by client",
				contract.getStatusHistory().getLast().reason());
		CreditContractAccepted event = assertInstanceOf(
				CreditContractAccepted.class, contract.getDomainEvents().getLast());
		assertEquals(acceptanceCorrelationId, event.correlationId());
		assertNull(event.causationId());
	}

	@Test
	void shouldRejectAcceptanceBeforeApproval() {
		CreditContract contract = sample();
		assertThrows(InvalidContractTransitionException.class,
				() -> contract.accept(UUID.randomUUID()));
	}

	@Test
	void shouldActivateOnlyAcceptedContractAndRecordCausation() {
		CreditContract contract = sample();
		EventContext analysisContext = new EventContext(UUID.randomUUID(), UUID.randomUUID());
		contract.startCreditAnalysis();
		contract.approveCreditAnalysis(
				MonetaryAmount.reais(new BigDecimal("5000.00")), analysisContext);
		contract.accept(UUID.randomUUID());
		EventContext activationContext = new EventContext(UUID.randomUUID(), UUID.randomUUID());

		contract.activate(activationContext);

		assertEquals(ContractStatus.ACTIVE, contract.getStatus());
		assertEquals(ContractStatus.ACCEPTED,
				contract.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.ACTIVE,
				contract.getStatusHistory().getLast().newStatus());
		assertEquals("Contract activated after client acceptance",
				contract.getStatusHistory().getLast().reason());
		CreditContractActivated event = assertInstanceOf(
				CreditContractActivated.class, contract.getDomainEvents().getLast());
		assertEquals(activationContext.correlationId(), event.correlationId());
		assertEquals(activationContext.causationId(), event.causationId());
	}

	@Test
	void shouldRejectActivationBeforeAcceptance() {
		CreditContract contract = sample();
		assertThrows(InvalidContractTransitionException.class,
				() -> contract.activate(new EventContext(UUID.randomUUID(), UUID.randomUUID())));
	}

	@Test
	void shouldBlockOnlyActiveContractAndRecordReasonAndEvent() {
		CreditContract contract = activeContract();
		UUID correlationId = UUID.randomUUID();

		contract.block("  Payment overdue for more than 30 days  ", correlationId);

		assertEquals(ContractStatus.BLOCKED, contract.getStatus());
		assertEquals(ContractStatus.ACTIVE,
				contract.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.BLOCKED,
				contract.getStatusHistory().getLast().newStatus());
		assertEquals("Payment overdue for more than 30 days",
				contract.getStatusHistory().getLast().reason());
		CreditContractBlocked event = assertInstanceOf(
				CreditContractBlocked.class, contract.getDomainEvents().getLast());
		assertEquals("Payment overdue for more than 30 days", event.reason());
		assertEquals(correlationId, event.correlationId());
		assertNull(event.causationId());
	}

	@Test
	void shouldRejectBlockingWhenContractIsNotActive() {
		CreditContract contract = sample();
		assertThrows(InvalidContractTransitionException.class,
				() -> contract.block("Collection policy", UUID.randomUUID()));
	}

	@Test
	void shouldRejectInvalidBlockingReason() {
		CreditContract contract = activeContract();
		assertThrows(IllegalArgumentException.class,
				() -> contract.block("   ", UUID.randomUUID()));
		assertThrows(IllegalArgumentException.class,
				() -> contract.block("x".repeat(256), UUID.randomUUID()));
	}

	@Test
	void shouldUnblockOnlyBlockedContractAndRecordReasonAndEvent() {
		CreditContract contract = activeContract();
		contract.block("Payment overdue", UUID.randomUUID());
		UUID correlationId = UUID.randomUUID();

		contract.unblock("  Outstanding balance settled  ", correlationId);

		assertEquals(ContractStatus.ACTIVE, contract.getStatus());
		assertEquals(ContractStatus.BLOCKED,
				contract.getStatusHistory().getLast().previousStatus());
		assertEquals(ContractStatus.ACTIVE,
				contract.getStatusHistory().getLast().newStatus());
		assertEquals("Outstanding balance settled",
				contract.getStatusHistory().getLast().reason());
		CreditContractUnblocked event = assertInstanceOf(
				CreditContractUnblocked.class, contract.getDomainEvents().getLast());
		assertEquals("Outstanding balance settled", event.reason());
		assertEquals(correlationId, event.correlationId());
		assertNull(event.causationId());
	}

	@Test
	void shouldRejectUnblockingWhenContractIsNotBlocked() {
		CreditContract contract = activeContract();
		assertThrows(InvalidContractTransitionException.class,
				() -> contract.unblock("Manual review completed", UUID.randomUUID()));
	}

	@Test
	void shouldRejectInvalidUnblockingReason() {
		CreditContract contract = activeContract();
		contract.block("Payment overdue", UUID.randomUUID());
		assertThrows(IllegalArgumentException.class,
				() -> contract.unblock("   ", UUID.randomUUID()));
		assertThrows(IllegalArgumentException.class,
				() -> contract.unblock("x".repeat(256), UUID.randomUUID()));
	}

	private CreditContract activeContract() {
		CreditContract contract = sample();
		contract.startCreditAnalysis();
		contract.approveCreditAnalysis(
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				new EventContext(UUID.randomUUID(), UUID.randomUUID()));
		contract.accept(UUID.randomUUID());
		contract.activate(new EventContext(UUID.randomUUID(), UUID.randomUUID()));
		return contract;
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
