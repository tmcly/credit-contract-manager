package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.query.ContractStatusHistoryItem;
import br.com.creditcontract.application.query.CreditContractSummary;
import br.com.creditcontract.application.query.CreditReanalysisItem;
import br.com.creditcontract.application.query.PageResult;
import br.com.creditcontract.application.usecase.FindContractStatusHistoryUseCase;
import br.com.creditcontract.application.usecase.FindCreditReanalysesUseCase;
import br.com.creditcontract.application.usecase.SearchCreditContractsQuery;
import br.com.creditcontract.application.usecase.SearchCreditContractsUseCase;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.enums.CreditReanalysisStatus;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractReadController.class)
class ContractReadControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private SearchCreditContractsUseCase searchUseCase;
	@Autowired private FindContractStatusHistoryUseCase statusHistoryUseCase;
	@Autowired private FindCreditReanalysesUseCase creditReanalysesUseCase;

	@BeforeEach
	void resetMocks() {
		reset(searchUseCase, statusHistoryUseCase, creditReanalysesUseCase);
	}

	@TestConfiguration
	static class TestConfig {
		@Bean @Primary SearchCreditContractsUseCase searchCreditContractsUseCase() {
			return mock(SearchCreditContractsUseCase.class);
		}
		@Bean @Primary FindContractStatusHistoryUseCase findContractStatusHistoryUseCase() {
			return mock(FindContractStatusHistoryUseCase.class);
		}
		@Bean @Primary FindCreditReanalysesUseCase findCreditReanalysesUseCase() {
			return mock(FindCreditReanalysesUseCase.class);
		}
	}

	@Test
	void shouldListContractsWithFiltersAndStablePageEnvelope() throws Exception {
		UUID contractId = UUID.randomUUID();
		LocalDateTime createdAt = LocalDateTime.of(2026, 7, 1, 10, 0);
		when(searchUseCase.execute(any())).thenReturn(new PageResult<>(List.of(
				new CreditContractSummary(
						ContractId.from(contractId), "CT-2026-000001", "Maria Silva",
						ContractStatus.ACTIVE, new BigDecimal("7500.00"),
						createdAt, createdAt.plusDays(1), 4L)), 1, 1, 3, 3));

		mockMvc.perform(get("/api/contracts")
						.param("status", "active")
						.param("documentNumber", "529.982.247-25")
						.param("contractNumber", " CT-2026-000001 ")
						.param("page", "1")
						.param("size", "1")
						.param("sort", "updatedAt")
						.param("direction", "asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(contractId.toString()))
				.andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$.content[0].creditLimit").value("7500.00"))
				.andExpect(jsonPath("$.page").value(1))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.first").value(false))
				.andExpect(jsonPath("$.last").value(false));

		ArgumentCaptor<SearchCreditContractsQuery> captor =
				ArgumentCaptor.forClass(SearchCreditContractsQuery.class);
		verify(searchUseCase).execute(captor.capture());
		SearchCreditContractsQuery query = captor.getValue();
		assertEquals(ContractStatus.ACTIVE, query.criteria().status());
		assertEquals("52998224725", query.criteria().documentNumber().value());
		assertEquals("CT-2026-000001", query.criteria().contractNumber());
		assertEquals(1, query.page().page());
		assertEquals(1, query.page().size());
		assertEquals("UPDATED_AT", query.sortField().name());
		assertEquals("ASC", query.direction().name());
	}

	@Test
	void shouldNotExposeDocumentNumberInListResponse() throws Exception {
		LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
		when(searchUseCase.execute(any())).thenReturn(new PageResult<>(List.of(
				new CreditContractSummary(ContractId.generate(), "CT-1", "Maria",
						ContractStatus.DRAFT, null, now, now, 0)), 0, 20, 1, 1));

		mockMvc.perform(get("/api/contracts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].documentNumber").doesNotExist())
				.andExpect(jsonPath("$.content[0].creditLimit").doesNotExist());
	}

	@Test
	void shouldReturn400ForUnsupportedFiltersOrPagination() throws Exception {
		mockMvc.perform(get("/api/contracts").param("status", "UNKNOWN"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type").value("/errors/invalid-query-parameter"));
		mockMvc.perform(get("/api/contracts").param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("size must be between 1 and 100"));
		mockMvc.perform(get("/api/contracts").param("sort", "creditLimit"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnStatusHistoryNewestFirstAsProvidedByUseCase() throws Exception {
		UUID contractId = UUID.randomUUID();
		UUID historyId = UUID.randomUUID();
		LocalDateTime changedAt = LocalDateTime.of(2026, 7, 2, 12, 0);
		when(statusHistoryUseCase.execute(any(), any())).thenReturn(new PageResult<>(List.of(
				new ContractStatusHistoryItem(historyId, ContractStatus.ACTIVE,
						ContractStatus.BLOCKED, "Payment overdue", changedAt)), 0, 20, 1, 1));

		mockMvc.perform(get("/api/contracts/{id}/history", contractId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(historyId.toString()))
				.andExpect(jsonPath("$.content[0].previousStatus").value("ACTIVE"))
				.andExpect(jsonPath("$.content[0].newStatus").value("BLOCKED"))
				.andExpect(jsonPath("$.content[0].reason").value("Payment overdue"));
	}

	@Test
	void shouldReturnCreditReanalysisHistoryAnd404ForMissingContract() throws Exception {
		UUID contractId = UUID.randomUUID();
		UUID reanalysisId = UUID.randomUUID();
		LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 2, 12, 0);
		when(creditReanalysesUseCase.execute(any(), any())).thenReturn(new PageResult<>(List.of(
				new CreditReanalysisItem(reanalysisId, CreditReanalysisStatus.APPROVED,
						new BigDecimal("5000.00"), new BigDecimal("7500.00"), null,
						requestedAt, requestedAt.plusSeconds(1))), 0, 20, 1, 1));

		mockMvc.perform(get("/api/contracts/{id}/credit-reanalyses", contractId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].status").value("APPROVED"))
				.andExpect(jsonPath("$.content[0].previousLimit").value("5000.00"))
				.andExpect(jsonPath("$.content[0].newLimit").value("7500.00"));

		when(statusHistoryUseCase.execute(any(), any())).thenThrow(
				new CreditContractNotFoundException(ContractId.from(contractId)));
		mockMvc.perform(get("/api/contracts/{id}/history", contractId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.type").value("/errors/credit-contract-not-found"));
	}
}
