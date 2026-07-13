package br.com.creditcontract.application.port.out;

import br.com.creditcontract.application.query.ContractSortField;
import br.com.creditcontract.application.query.ContractStatusHistoryItem;
import br.com.creditcontract.application.query.CreditContractSearchCriteria;
import br.com.creditcontract.application.query.CreditContractSummary;
import br.com.creditcontract.application.query.CreditReanalysisItem;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.PageResult;
import br.com.creditcontract.application.query.SortDirection;
import br.com.creditcontract.domain.valueobject.ContractId;

/** Read-side capabilities that do not require aggregate rehydration. */
public interface CreditContractQueryPort {

	PageResult<CreditContractSummary> search(
			CreditContractSearchCriteria criteria,
			PageQuery page,
			ContractSortField sortField,
			SortDirection direction);

	boolean existsById(ContractId contractId);

	PageResult<ContractStatusHistoryItem> findStatusHistory(
			ContractId contractId, PageQuery page);

	PageResult<CreditReanalysisItem> findCreditReanalyses(
			ContractId contractId, PageQuery page);
}
