package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.port.out.CreditContractQueryPort;
import br.com.creditcontract.application.query.CreditContractSummary;
import br.com.creditcontract.application.query.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SearchCreditContractsUseCase {

	private final CreditContractQueryPort queryPort;

	public SearchCreditContractsUseCase(CreditContractQueryPort queryPort) {
		this.queryPort = Objects.requireNonNull(queryPort);
	}

	@Transactional(readOnly = true)
	public PageResult<CreditContractSummary> execute(SearchCreditContractsQuery query) {
		Objects.requireNonNull(query, "search query is required");
		return queryPort.search(
				query.criteria(), query.page(), query.sortField(), query.direction());
	}
}
