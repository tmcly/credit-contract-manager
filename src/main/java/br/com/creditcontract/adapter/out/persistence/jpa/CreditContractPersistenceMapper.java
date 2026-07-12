package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.entity.ContractStatusHistory;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.entity.CreditReanalysis;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.DocumentNumber;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import br.com.creditcontract.domain.valueobject.ZipCode;
import org.springframework.stereotype.Component;

@Component
public class CreditContractPersistenceMapper {

	CreditContractJpaEntity toJpaEntity(CreditContract contract) {
		Client client = contract.getClient();
		Address address = client.address();

		CreditContractJpaEntity entity = new CreditContractJpaEntity(
				contract.getId().value(),
				contract.getContractNumber(),
				client.documentNumber().value(),
				client.name(),
				address.state(),
				address.city(),
				address.street(),
				address.number(),
				address.zipCode().digits(),
				contract.getStatus(),
				contract.getCreditLimit() == null ? null : contract.getCreditLimit().amount(),
				contract.getCreatedAt(),
				contract.getUpdatedAt());

		contract.getStatusHistory().stream()
				.map(this::toJpaEntity)
				.forEach(entity::addStatusHistory);
		contract.getCreditReanalyses().stream()
				.map(this::toJpaEntity)
				.forEach(entity::addCreditReanalysis);
		return entity;
	}

	void updateJpaEntity(CreditContract contract, CreditContractJpaEntity entity) {
		entity.updateFrom(contract);
		contract.getStatusHistory().stream()
				.filter(history -> !entity.hasStatusHistory(history.id()))
				.map(this::toJpaEntity)
				.forEach(entity::addStatusHistory);
		contract.getCreditReanalyses().forEach(reanalysis -> {
			CreditReanalysisJpaEntity existing = entity.findCreditReanalysis(reanalysis.getId());
			if (existing == null) {
				entity.addCreditReanalysis(toJpaEntity(reanalysis));
			} else {
				existing.updateFrom(reanalysis);
			}
		});
	}

	CreditContract toDomain(CreditContractJpaEntity entity) {
		MonetaryAmount creditLimit = entity.getCreditLimit() == null
				? null
				: MonetaryAmount.reais(entity.getCreditLimit());
		return CreditContract.rehydrate(
				ContractId.from(entity.getId()),
				entity.getContractNumber(),
				new Client(
						DocumentNumber.from(entity.getClientDocumentNumber()),
						entity.getClientName(),
						new Address(
								entity.getClientState(),
								entity.getClientCity(),
								entity.getClientStreet(),
								entity.getClientAddressNumber(),
								new ZipCode(entity.getClientZipCode()))),
				entity.getStatus(),
				creditLimit,
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getVersion(),
				entity.getStatusHistory().stream()
						.map(this::toDomain)
						.toList(),
				entity.getCreditReanalyses().stream()
						.map(this::toDomain)
						.toList());
	}

	private CreditReanalysisJpaEntity toJpaEntity(CreditReanalysis reanalysis) {
		return new CreditReanalysisJpaEntity(
				reanalysis.getId(),
				reanalysis.getStatus(),
				reanalysis.getPreviousLimit().amount(),
				reanalysis.getNewLimit() == null ? null : reanalysis.getNewLimit().amount(),
				reanalysis.getReason(),
				reanalysis.getRequestedAt(),
				reanalysis.getCompletedAt());
	}

	private CreditReanalysis toDomain(CreditReanalysisJpaEntity reanalysis) {
		return CreditReanalysis.rehydrate(
				reanalysis.getId(),
				MonetaryAmount.reais(reanalysis.getPreviousLimit()),
				reanalysis.getRequestedAt(),
				reanalysis.getStatus(),
				reanalysis.getNewLimit() == null
						? null : MonetaryAmount.reais(reanalysis.getNewLimit()),
				reanalysis.getReason(),
				reanalysis.getCompletedAt());
	}

	private ContractStatusHistoryJpaEntity toJpaEntity(ContractStatusHistory history) {
		return new ContractStatusHistoryJpaEntity(
				history.id(),
				history.previousStatus(),
				history.newStatus(),
				history.reason(),
				history.changedAt());
	}

	private ContractStatusHistory toDomain(ContractStatusHistoryJpaEntity history) {
		return new ContractStatusHistory(
				history.getId(),
				history.getPreviousStatus(),
				history.getNewStatus(),
				history.getReason(),
				history.getChangedAt());
	}
}
