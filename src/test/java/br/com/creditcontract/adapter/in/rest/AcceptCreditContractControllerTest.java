package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.AcceptCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.exception.InvalidContractTransitionException;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcceptCreditContractController.class)
class AcceptCreditContractControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private AcceptCreditContractUseCase useCase;

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		AcceptCreditContractUseCase acceptCreditContractUseCase() {
			return mock(AcceptCreditContractUseCase.class);
		}
	}

	@Test
	void shouldAcceptApprovedContract() throws Exception {
		UUID id = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		when(useCase.execute(ContractId.from(id), correlationId))
				.thenReturn(contractIn(id, ContractStatus.ACCEPTED));

		mockMvc.perform(post("/api/contracts/{id}/acceptance", id)
						.header("X-Correlation-ID", correlationId))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Correlation-ID", correlationId.toString()))
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.creditLimit").value("5000.00"));

		verify(useCase).execute(ContractId.from(id), correlationId);
	}

	@Test
	void shouldReturn409WhenContractCannotBeAccepted() throws Exception {
		UUID id = UUID.randomUUID();
		when(useCase.execute(org.mockito.ArgumentMatchers.eq(ContractId.from(id)),
				org.mockito.ArgumentMatchers.any()))
				.thenThrow(new InvalidContractTransitionException(
						ContractStatus.REJECTED, ContractStatus.ACCEPTED));

		mockMvc.perform(post("/api/contracts/{id}/acceptance", id))
				.andExpect(status().isConflict())
				.andExpect(header().exists("X-Correlation-ID"))
				.andExpect(jsonPath("$.type").value("/errors/invalid-contract-transition"));
	}

	private CreditContract contractIn(UUID id, ContractStatus status) {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(
				ContractId.from(id),
				"CT-2026-000301",
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
