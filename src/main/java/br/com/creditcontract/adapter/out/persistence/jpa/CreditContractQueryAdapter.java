package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.application.port.out.CreditContractQueryPort;
import br.com.creditcontract.application.query.ContractSortField;
import br.com.creditcontract.application.query.ContractStatusHistoryItem;
import br.com.creditcontract.application.query.CreditContractSearchCriteria;
import br.com.creditcontract.application.query.CreditContractSummary;
import br.com.creditcontract.application.query.CreditReanalysisItem;
import br.com.creditcontract.application.query.PageQuery;
import br.com.creditcontract.application.query.PageResult;
import br.com.creditcontract.application.query.SortDirection;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class CreditContractQueryAdapter implements CreditContractQueryPort {

	private final CreditContractJpaRepository contractRepository;
	private final ContractStatusHistoryJpaRepository statusHistoryRepository;
	private final CreditReanalysisJpaRepository creditReanalysisRepository;

	public CreditContractQueryAdapter(
			CreditContractJpaRepository contractRepository,
			ContractStatusHistoryJpaRepository statusHistoryRepository,
			CreditReanalysisJpaRepository creditReanalysisRepository) {
		this.contractRepository = Objects.requireNonNull(contractRepository);
		this.statusHistoryRepository = Objects.requireNonNull(statusHistoryRepository);
		this.creditReanalysisRepository = Objects.requireNonNull(creditReanalysisRepository);
	}

	@Override
	public PageResult<CreditContractSummary> search(
			CreditContractSearchCriteria criteria,
			PageQuery page,
			ContractSortField sortField,
			SortDirection direction) {
		String property = sortField == ContractSortField.CREATED_AT ? "createdAt" : "updatedAt";
		Sort.Direction springDirection = direction == SortDirection.ASC
				? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(new Sort.Order(springDirection, property), Sort.Order.asc("id"));
		Page<CreditContractSummaryJpaProjection> result = contractRepository.search(
				criteria.status(),
				criteria.documentNumber() == null ? null : criteria.documentNumber().value(),
				criteria.contractNumber(),
				PageRequest.of(page.page(), page.size(), sort));
		return toPageResult(result.map(this::toSummary));
	}

	@Override
	public boolean existsById(ContractId contractId) {
		return contractRepository.existsById(contractId.value());
	}

	@Override
	public PageResult<ContractStatusHistoryItem> findStatusHistory(
			ContractId contractId, PageQuery page) {
		PageRequest pageable = PageRequest.of(page.page(), page.size(),
				Sort.by(Sort.Order.desc("changedAt"), Sort.Order.asc("id")));
		return toPageResult(statusHistoryRepository
				.findByContractId(contractId.value(), pageable)
				.map(this::toStatusHistory));
	}

	@Override
	public PageResult<CreditReanalysisItem> findCreditReanalyses(
			ContractId contractId, PageQuery page) {
		PageRequest pageable = PageRequest.of(page.page(), page.size(),
				Sort.by(Sort.Order.desc("requestedAt"), Sort.Order.asc("id")));
		return toPageResult(creditReanalysisRepository
				.findByContractId(contractId.value(), pageable)
				.map(this::toCreditReanalysis));
	}

	private CreditContractSummary toSummary(CreditContractSummaryJpaProjection entity) {
		return new CreditContractSummary(
				ContractId.from(entity.id()),
				entity.contractNumber(),
				entity.clientName(),
				entity.status(),
				entity.creditLimit(),
				entity.createdAt(),
				entity.updatedAt(),
				entity.version());
	}

	private ContractStatusHistoryItem toStatusHistory(ContractStatusHistoryJpaEntity entity) {
		return new ContractStatusHistoryItem(
				entity.getId(), entity.getPreviousStatus(), entity.getNewStatus(),
				entity.getReason(), entity.getChangedAt());
	}

	private CreditReanalysisItem toCreditReanalysis(CreditReanalysisJpaEntity entity) {
		return new CreditReanalysisItem(
				entity.getId(), entity.getStatus(), entity.getPreviousLimit(),
				entity.getNewLimit(), entity.getReason(), entity.getRequestedAt(),
				entity.getCompletedAt());
	}

	private <T> PageResult<T> toPageResult(Page<T> page) {
		return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}
}
