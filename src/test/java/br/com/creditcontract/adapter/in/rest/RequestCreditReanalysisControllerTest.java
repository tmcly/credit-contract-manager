package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;
import br.com.creditcontract.application.usecase.RequestCreditReanalysisUseCase;
import br.com.creditcontract.domain.exception.CreditReanalysisCooldownException;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestCreditReanalysisController.class)
class RequestCreditReanalysisControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private RequestCreditReanalysisUseCase useCase;

	@TestConfiguration
	static class TestConfig {
		@Bean @Primary
		RequestCreditReanalysisUseCase requestCreditReanalysisUseCase() {
			return mock(RequestCreditReanalysisUseCase.class);
		}
	}

	@Test
	void shouldReturn202ForAcceptedReanalysisRequest() throws Exception {
		UUID contractId = UUID.randomUUID();
		UUID requestId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 12, 12, 0);
		when(useCase.execute(ContractId.from(contractId), correlationId)).thenReturn(
				new CreditReanalysisRequestResult(
						requestId, ContractId.from(contractId), requestedAt, requestedAt.plusDays(30)));

		mockMvc.perform(post("/api/contracts/{id}/credit-reanalysis", contractId)
						.header("X-Correlation-ID", correlationId))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.requestId").value(requestId.toString()))
				.andExpect(jsonPath("$.contractId").value(contractId.toString()))
				.andExpect(jsonPath("$.status").value("REQUESTED"))
				.andExpect(jsonPath("$.nextEligibleAt").value("2026-08-11T12:00:00"));
		verify(useCase).execute(ContractId.from(contractId), correlationId);
	}

	@Test
	void shouldReturn429AndNextEligibleDateDuringCooldown() throws Exception {
		UUID contractId = UUID.randomUUID();
		LocalDateTime nextEligibleAt = LocalDateTime.of(2026, 8, 11, 12, 0);
		when(useCase.execute(eq(ContractId.from(contractId)), any())).thenThrow(
				new CreditReanalysisCooldownException(nextEligibleAt));

		mockMvc.perform(post("/api/contracts/{id}/credit-reanalysis", contractId))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.type").value("/errors/credit-reanalysis-cooldown"))
				.andExpect(jsonPath("$.nextEligibleAt").value("2026-08-11T12:00:00"));
	}
}
