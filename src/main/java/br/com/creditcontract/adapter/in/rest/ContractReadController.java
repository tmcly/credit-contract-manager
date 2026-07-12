package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.ContractStatusHistoryResponse;
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
	public PageResponse<CreditContractSummaryResponse> search(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String documentNumber,
			@RequestParam(required = false) String contractNumber,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "createdAt") String sort,
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
	public PageResponse<ContractStatusHistoryResponse> findStatusHistory(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return PageResponse.from(
				statusHistoryUseCase.execute(ContractId.from(id), pageQuery(page, size)),
				ContractStatusHistoryResponse::from);
	}

	@GetMapping("/{id}/credit-reanalyses")
	public PageResponse<CreditReanalysisResponse> findCreditReanalyses(
			@PathVariable UUID id,
			@RequestParam(defaultValue = "0") int page,
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
