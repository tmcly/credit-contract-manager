package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import br.com.creditcontract.adapter.in.rest.dto.CreateContractRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateContractController.class)
class CreateContractControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		CreateContractUseCase createContractUseCase() {
			return mock(CreateContractUseCase.class);
		}
	}

	@Autowired
	private CreateContractUseCase useCase;

	@Test
	void shouldCreateContractAndReturn201WithLocation() throws Exception {
		CreditContract contract = CreditContract.create(
				ContractId.generate(),
				"CT-2026-000001",
				new Client("Maria Silva",
						new Address("PR", "Curitiba", "Rua das Flores", "123",
								new ZipCode("80010-000"))),
				MonetaryAmount.reais(new BigDecimal("5000.00"))
		);

		when(useCase.execute(any())).thenReturn(contract);

		CreateContractRequest request = new CreateContractRequest("529.982.247-25");

		mockMvc.perform(post("/api/contracts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.contractNumber").value("CT-2026-000001"))
				.andExpect(jsonPath("$.clientName").value("Maria Silva"))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.currency").value("BRL"))
				.andExpect(jsonPath("$.creditLimit").value("5000.00"))
				.andExpect(jsonPath("$.version").value(0));

		verify(useCase).execute(argThat(input ->
				input.documentNumber().value().equals("52998224725")));
	}

	@Test
	void shouldReturn400WhenCpfIsMissing() throws Exception {
		String json = "{\"documentNumber\": \"\"}";

		mockMvc.perform(post("/api/contracts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.detail").value("documentNumber is required"));
	}

	@Test
	void shouldReturn400WhenCpfIsInvalid() throws Exception {
		String json = "{\"documentNumber\": \"529.982.247-24\"}";

		mockMvc.perform(post("/api/contracts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.detail").value("documentNumber must be valid"));
	}
}
