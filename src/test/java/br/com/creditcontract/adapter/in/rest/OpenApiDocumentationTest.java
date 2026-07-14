package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.application.usecase.AcceptCreditContractUseCase;
import br.com.creditcontract.application.usecase.BlockCreditContractUseCase;
import br.com.creditcontract.application.usecase.CancelCreditContractUseCase;
import br.com.creditcontract.application.usecase.CreateContractUseCase;
import br.com.creditcontract.application.usecase.FindContractStatusHistoryUseCase;
import br.com.creditcontract.application.usecase.FindCreditContractUseCase;
import br.com.creditcontract.application.usecase.FindCreditReanalysesUseCase;
import br.com.creditcontract.application.usecase.RequestCreditReanalysisUseCase;
import br.com.creditcontract.application.usecase.SearchCreditContractsUseCase;
import br.com.creditcontract.application.usecase.UnblockCreditContractUseCase;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
		AcceptCreditContractController.class,
		BlockCreditContractController.class,
		CancelCreditContractController.class,
		ContractReadController.class,
		CreateContractController.class,
		FindCreditContractController.class,
		HealthCheckController.class,
		RequestCreditReanalysisController.class,
		UnblockCreditContractController.class
})
@Import({
		OpenApiConfiguration.class,
		SpringDocConfiguration.class,
		SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class
})
class OpenApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private AcceptCreditContractUseCase acceptCreditContractUseCase;
	@MockitoBean private BlockCreditContractUseCase blockCreditContractUseCase;
	@MockitoBean private CancelCreditContractUseCase cancelCreditContractUseCase;
	@MockitoBean private CreateContractUseCase createContractUseCase;
	@MockitoBean private FindContractStatusHistoryUseCase findContractStatusHistoryUseCase;
	@MockitoBean private FindCreditContractUseCase findCreditContractUseCase;
	@MockitoBean private FindCreditReanalysesUseCase findCreditReanalysesUseCase;
	@MockitoBean private RequestCreditReanalysisUseCase requestCreditReanalysisUseCase;
	@MockitoBean private SearchCreditContractsUseCase searchCreditContractsUseCase;
	@MockitoBean private UnblockCreditContractUseCase unblockCreditContractUseCase;

	@Test
	void shouldExposeHumanReadableMetadataAndEveryPublicApiPath() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Credit Contract Manager API"))
				.andExpect(jsonPath("$.info.version").value("v1"))
				.andExpect(jsonPath("$.externalDocs.url")
						.value("https://github.com/tmcly/credit-contract-manager"))
				.andExpect(jsonPath("$.paths['/api/contracts'].post.operationId")
						.value("createCreditContract"))
				.andExpect(jsonPath("$.paths['/api/contracts'].get.operationId")
						.value("searchCreditContracts"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}'].get.operationId")
						.value("findCreditContractById"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/acceptance'].post.operationId")
						.value("acceptCreditContract"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/blocking'].post.operationId")
						.value("blockCreditContract"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/unblocking'].post.operationId")
						.value("unblockCreditContract"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/cancellation'].post.operationId")
						.value("cancelCreditContract"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/credit-reanalysis'].post.operationId")
						.value("requestCreditReanalysis"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/history'].get.operationId")
						.value("findContractStatusHistory"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/credit-reanalyses'].get.operationId")
						.value("findCreditReanalyses"));
	}

	@Test
	void shouldDocumentExamplesAndBusinessErrorContracts() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/api/contracts'].post.requestBody.content['application/json'].example.documentNumber")
						.value("529.982.247-25"))
				.andExpect(jsonPath("$.paths['/api/contracts'].post.responses['201'].content['application/json'].examples['Draft contract'].value.status")
						.value("DRAFT"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/blocking'].post.responses['409'].content['application/problem+json'].schema['$ref']")
						.value("#/components/schemas/ApiProblem"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/blocking'].post.responses['409'].content['application/problem+json'].examples['Concurrent update'].value.type")
						.value("/errors/concurrent-contract-update"))
				.andExpect(jsonPath("$.paths['/api/contracts/{id}/credit-reanalysis'].post.responses['429'].content['application/problem+json'].example.nextEligibleAt")
						.value("2026-08-12T10:20:00"))
				.andExpect(jsonPath("$.components.schemas.ApiProblem.properties.nextEligibleAt").exists())
				.andExpect(jsonPath("$.components.schemas.CreditContractResponse.properties.status.enum").isArray());
	}
}
