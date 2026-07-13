package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractQueryPort;
import br.com.creditcontract.application.query.CreditReanalysisItem;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.PageResult;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class FindCreditReanalysesUseCase {

	private final CreditContractQueryPort queryPort;

	public FindCreditReanalysesUseCase(CreditContractQueryPort queryPort) {
		this.queryPort = Objects.requireNonNull(queryPort);
	}

	@Transactional(readOnly = true)
	public PageResult<CreditReanalysisItem> execute(ContractId contractId, PageQuery page) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(page, "page is required");
		if (!queryPort.existsById(contractId)) {
			throw new CreditContractNotFoundException(contractId);
		}
		return queryPort.findCreditReanalyses(contractId, page);
	}
}
