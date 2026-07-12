package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CancelCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.CancellationOrigin;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CancelCreditContractController.class)
class CancelCreditContractControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private CancelCreditContractUseCase useCase;

	@TestConfiguration
	static class TestConfig {
		@Bean @Primary
		CancelCreditContractUseCase cancelCreditContractUseCase() {
			return mock(CancelCreditContractUseCase.class);
		}
	}

	@Test
	void shouldCancelAtClientRequest() throws Exception {
		UUID id = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		when(useCase.execute(ContractId.from(id), CancellationOrigin.CLIENT_REQUEST,
				"Client request", correlationId)).thenReturn(cancelledContract(id));

		mockMvc.perform(post("/api/contracts/{id}/cancellation", id)
						.header("X-Correlation-ID", correlationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"requestedBy\":\"CLIENT\",\"reason\":\"Client request\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		verify(useCase).execute(ContractId.from(id), CancellationOrigin.CLIENT_REQUEST,
				"Client request", correlationId);
	}

	@Test
	void shouldReturn400WhenReasonOrRequesterIsMissing() throws Exception {
		mockMvc.perform(post("/api/contracts/{id}/cancellation", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type").value("/errors/request-validation"));
	}

	@Test
	void shouldReturn409WhenClientTriesToCancelBlockedContract() throws Exception {
		UUID id = UUID.randomUUID();
		when(useCase.execute(eq(ContractId.from(id)), eq(CancellationOrigin.CLIENT_REQUEST),
				eq("Client request"), any())).thenThrow(new InvalidContractTransitionException(
				ContractStatus.BLOCKED, ContractStatus.CANCELLED));

		mockMvc.perform(post("/api/contracts/{id}/cancellation", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"requestedBy\":\"CLIENT\",\"reason\":\"Client request\"}"))
				.andExpect(status().isConflict());
	}

	private CreditContract cancelledContract(UUID id) {
		LocalDateTime now = LocalDateTime.now();
		return CreditContract.rehydrate(ContractId.from(id), "CT-2026-000702",
				new Client(DocumentNumber.from("52998224725"), "Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				ContractStatus.CANCELLED, MonetaryAmount.reais(new BigDecimal("5000.00")),
				now, now, 7L, List.of());
	}
}
