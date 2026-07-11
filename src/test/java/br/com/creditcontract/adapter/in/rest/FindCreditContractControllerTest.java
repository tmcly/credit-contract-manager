package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.FindCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FindCreditContractController.class)
class FindCreditContractControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private FindCreditContractUseCase useCase;

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		FindCreditContractUseCase findCreditContractUseCase() {
			return mock(FindCreditContractUseCase.class);
		}
	}

	@Test
	void shouldReturnCurrentContractState() throws Exception {
		UUID id = UUID.randomUUID();
		CreditContract contract = CreditContract.rehydrate(
				ContractId.from(id),
				"CT-2026-000200",
				new Client(
						DocumentNumber.from("52998224725"),
						"Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123", new ZipCode("80010-000"))),
				ContractStatus.DRAFT,
				null,
				LocalDateTime.now(),
				LocalDateTime.now(),
				0L,
				List.of());
		when(useCase.execute(ContractId.from(id))).thenReturn(contract);

		mockMvc.perform(get("/api/contracts/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.creditLimit").doesNotExist());
	}
}
