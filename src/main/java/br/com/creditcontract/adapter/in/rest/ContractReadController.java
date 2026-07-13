package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.ContractStatusHistoryResponse;
import br.com.creditcontract.adapter.in.rest.dto.ApiProblemResponse;
import br.com.creditcontract.adapter.in.rest.dto.CreditContractSummaryResponse;
import br.com.creditcontract.adapter.in.rest.dto.CreditReanalysisResponse;
import br.com.creditcontract.adapter.in.rest.dto.PageResponse;
import br.com.creditcontract.application.exception.InvalidQueryParameterException;
import br.com.creditcontract.application.query.ContractSortField;
import br.com.creditcontract.application.query.CreditContractSearchCriteria;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.SortDirection;
import br.com.creditcontract.application.usecase.FindContractStatusHistoryUseCase;
import br.com.creditcontract.application.usecase.FindCreditReanalysesUseCase;
import br.com.creditcontract.application.usecase.SearchCreditContractsQuery;
import br.com.creditcontract.application.usecase.SearchCreditContractsUseCase;
import br.com.creditcontract.domain.enums.ContractStatus;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract queries")
public class ContractReadController {

	private final SearchCreditContractsUseCase searchUseCase;
	private final FindContractStatusHistoryUseCase statusHistoryUseCase;
	private final FindCreditReanalysesUseCase creditReanalysesUseCase;

	public ContractReadController(
			SearchCreditContractsUseCase searchUseCase,
			FindContractStatusHistoryUseCase statusHistoryUseCase,
			FindCreditReanalysesUseCase creditReanalysesUseCase) {
		this.searchUseCase = Objects.requireNonNull(searchUseCase);
		this.statusHistoryUseCase = Objects.requireNonNull(statusHistoryUseCase);
		this.creditReanalysesUseCase = Objects.requireNonNull(creditReanalysesUseCase);
	}

	@GetMapping
	@Operation(
			operationId = "searchCreditContracts",
			summary = "Search credit contracts",
			description = """
					Returns privacy-conscious summaries with exact optional filters and bounded,
					zero-based pagination. CPF can select a contract but is never exposed in the
					collection response. Sorting uses ID as a deterministic tie-breaker.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Page of matching contract summaries.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_PAGE_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Unsupported filter, sort, direction, CPF, or pagination value.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.INVALID_QUERY_PARAMETER_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public PageResponse<CreditContractSummaryResponse> search(
			@Parameter(description = "Exact lifecycle state.", example = "ACTIVE",
					schema = @Schema(allowableValues = {"DRAFT", "UNDER_REVIEW", "APPROVED", "REJECTED", "ACCEPTED", "ACTIVE", "BLOCKED", "CANCELLED"}))
			@RequestParam(required = false) String status,
			@Parameter(description = "Exact CPF filter. Punctuation is optional; the value is not returned.", example = "529.982.247-25")
			@RequestParam(required = false) String documentNumber,
			@Parameter(description = "Exact human-facing contract number.", example = "CT-2026-000001")
			@RequestParam(required = false) String contractNumber,
			@Parameter(description = "Zero-based page number.", example = "0")
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Number of items, from 1 through 100.", example = "20")
			@RequestParam(defaultValue = "20") int size,
			@Parameter(description = "Indexed field used for ordering.", example = "createdAt",
					schema = @Schema(allowableValues = {"createdAt", "updatedAt"}))
			@RequestParam(defaultValue = "createdAt") String sort,
			@Parameter(description = "Sort direction.", example = "desc",
					schema = @Schema(allowableValues = {"asc", "desc"}))
			@RequestParam(defaultValue = "desc") String direction) {
		CreditContractSearchCriteria criteria = searchCriteria(
				status, documentNumber, contractNumber);
		SearchCreditContractsQuery query = new SearchCreditContractsQuery(
				criteria, pageQuery(page, size), parseSort(sort), parseDirection(direction));
		return PageResponse.from(searchUseCase.execute(query), CreditContractSummaryResponse::from);
	}

	private CreditContractSearchCriteria searchCriteria(
			String status, String documentNumber, String contractNumber) {
		DocumentNumber document = documentNumber == null || documentNumber.isBlank()
				? null : DocumentNumber.from(documentNumber);
		try {
			return new CreditContractSearchCriteria(
					parseStatus(status),
					document,
					contractNumber);
		} catch (InvalidQueryParameterException exception) {
			throw exception;
		} catch (IllegalArgumentException exception) {
			throw new InvalidQueryParameterException(exception.getMessage());
		}
	}

	@GetMapping("/{id}/history")
	@Operation(
			operationId = "findContractStatusHistory",
			summary = "List contract status history",
			description = "Returns auditable lifecycle transitions newest first. Existing contracts with no entries return an empty page.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Page of status transitions.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.STATUS_HISTORY_PAGE_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination value.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public PageResponse<ContractStatusHistoryResponse> findStatusHistory(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@Parameter(description = "Zero-based page number.", example = "0")
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Number of items, from 1 through 100.", example = "20")
			@RequestParam(defaultValue = "20") int size) {
		return PageResponse.from(
				statusHistoryUseCase.execute(ContractId.from(id), pageQuery(page, size)),
				ContractStatusHistoryResponse::from);
	}

	@GetMapping("/{id}/credit-reanalyses")
	@Operation(
			operationId = "findCreditReanalyses",
			summary = "List credit-reanalysis history",
			description = "Returns reanalysis requests and asynchronous outcomes newest first. Existing contracts with no requests return an empty page.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Page of credit-reanalysis requests and outcomes.",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.REANALYSIS_PAGE_RESPONSE))),
			@ApiResponse(responseCode = "400", description = "Invalid pagination value.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class))),
			@ApiResponse(responseCode = "404", description = "Contract does not exist.",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemResponse.class),
							examples = @ExampleObject(value = OpenApiExamples.CONTRACT_NOT_FOUND_PROBLEM)))
	})
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted and echoes it in the response header.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public PageResponse<CreditReanalysisResponse> findCreditReanalyses(
			@Parameter(description = "Contract identifier.", example = OpenApiExamples.CONTRACT_ID)
			@PathVariable UUID id,
			@Parameter(description = "Zero-based page number.", example = "0")
			@RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Number of items, from 1 through 100.", example = "20")
			@RequestParam(defaultValue = "20") int size) {
		return PageResponse.from(
				creditReanalysesUseCase.execute(ContractId.from(id), pageQuery(page, size)),
				CreditReanalysisResponse::from);
	}

	private PageQuery pageQuery(int page, int size) {
		try {
			return new PageQuery(page, size);
		} catch (IllegalArgumentException exception) {
			throw new InvalidQueryParameterException(exception.getMessage());
		}
	}

	private ContractStatus parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		try {
			return ContractStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new InvalidQueryParameterException("status is not supported: " + status);
		}
	}

	private ContractSortField parseSort(String sort) {
		return switch (sort.trim().toLowerCase(Locale.ROOT)) {
			case "createdat" -> ContractSortField.CREATED_AT;
			case "updatedat" -> ContractSortField.UPDATED_AT;
			default -> throw new InvalidQueryParameterException(
					"sort must be createdAt or updatedAt");
		};
	}

	private SortDirection parseDirection(String direction) {
		return switch (direction.trim().toLowerCase(Locale.ROOT)) {
			case "asc" -> SortDirection.ASC;
			case "desc" -> SortDirection.DESC;
			default -> throw new InvalidQueryParameterException(
					"direction must be asc or desc");
		};
	}
}
