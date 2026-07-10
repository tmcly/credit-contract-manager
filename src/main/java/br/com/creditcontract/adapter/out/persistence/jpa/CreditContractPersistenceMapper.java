package br.com.creditcontract.adapter.out.persistence.jpa;

import br.com.creditcontract.domain.entity.ContractStatusHistory;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.Address;
import br.com.creditcontract.domain.valueobject.Client;
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
				contract.getCreditLimit().amount(),
				contract.getCreatedAt(),
				contract.getUpdatedAt());

		contract.getStatusHistory().stream()
				.map(this::toJpaEntity)
				.forEach(entity::addStatusHistory);
		return entity;
	}

	private ContractStatusHistoryJpaEntity toJpaEntity(ContractStatusHistory history) {
		return new ContractStatusHistoryJpaEntity(
				history.id(),
				history.previousStatus(),
				history.newStatus(),
				history.reason(),
				history.changedAt());
	}
}
