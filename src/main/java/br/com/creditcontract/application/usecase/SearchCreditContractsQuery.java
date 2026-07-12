package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.query.ContractSortField;
import br.com.creditcontract.application.query.CreditContractSearchCriteria;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.SortDirection;

import java.util.Objects;

public record SearchCreditContractsQuery(
		CreditContractSearchCriteria criteria,
		PageQuery page,
		ContractSortField sortField,
		SortDirection direction) {

	public SearchCreditContractsQuery {
		Objects.requireNonNull(criteria, "search criteria is required");
		Objects.requireNonNull(page, "page is required");
		Objects.requireNonNull(sortField, "sort field is required");
		Objects.requireNonNull(direction, "sort direction is required");
	}
}
