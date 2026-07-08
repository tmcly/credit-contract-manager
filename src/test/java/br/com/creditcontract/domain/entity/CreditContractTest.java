package br.com.creditcontract.domain.entity;

import br.com.creditcontract.domain.enumeration.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
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
				new Client("Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				MonetaryAmount.reais(new BigDecimal("5000.00")),
				LocalDateTime.now().minusDays(1)
		);
	}

	@Test
	void create_starts_as_draft_with_version_zero() {
		CreditContract contract = sample();
		assertEquals(ContractStatus.DRAFT, contract.getStatus());
		assertEquals(0L, contract.getVersion());
		assertNotNull(contract.getCreationDate());
		assertNotNull(contract.getLastUpdateDate());
	}

	@Test
	void block_updates_status_reason_and_version() {
		CreditContract contract = sample();
		contract.block("Suspected fraud");
		assertEquals(ContractStatus.BLOCKED, contract.getStatus());
		assertEquals("Suspected fraud", contract.getBlockReason());
		assertEquals(1L, contract.getVersion());
	}

	@Test
	void block_without_reason_throws() {
		CreditContract contract = sample();
		assertThrows(NullPointerException.class, () -> contract.block(null));
	}

	@Test
	void cancel_after_block_works_and_increments_version() {
		CreditContract contract = sample();
		contract.block("Fraud");
		contract.cancel("Confirmed fraud");
		assertEquals(ContractStatus.CANCELLED, contract.getStatus());
		assertEquals("Confirmed fraud", contract.getCancellationReason());
		assertEquals(2L, contract.getVersion());
	}

	@Test
	void cannot_block_already_cancelled() {
		CreditContract contract = sample();
		contract.cancel("Customer request");
		assertThrows(IllegalStateException.class, () -> contract.block("Too late"));
	}

	@Test
	void cannot_cancel_already_cancelled() {
		CreditContract contract = sample();
		contract.cancel("Customer request");
		assertThrows(IllegalStateException.class, () -> contract.cancel("Again"));
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
