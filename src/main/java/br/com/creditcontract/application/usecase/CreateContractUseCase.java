package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Orchestrates the creation of a new credit contract.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Fetch the client snapshot from the external registry.</li>
	 *   <li>Generate the next sequential contract number.</li>
 *   <li>Assemble and persist the {@link CreditContract} aggregate.</li>
 * </ol>
 *
 * <p>This use case is the "S" in SOLID — it orchestrates one business action.
 * The repository adapter atomically persists the aggregate and its pending
 * domain events without exposing outbox mechanics here.
 */
@Service
public class CreateContractUseCase {

	private final ClientDataProvider clientDataProvider;
	private final ContractNumberGenerator contractNumberGenerator;
	private final CreditContractRepository creditContractRepository;

	public CreateContractUseCase(ClientDataProvider clientDataProvider,
	                             ContractNumberGenerator contractNumberGenerator,
	                             CreditContractRepository creditContractRepository) {
		this.clientDataProvider = Objects.requireNonNull(clientDataProvider);
		this.contractNumberGenerator = Objects.requireNonNull(contractNumberGenerator);
		this.creditContractRepository = Objects.requireNonNull(creditContractRepository);
	}

	/**
	 * Creates a new credit contract for the given client.
	 *
	 * @param input the CPF of the client
	 * @return the newly created contract aggregate
	 */
	public CreditContract execute(CreateContractInput input) {
		Objects.requireNonNull(input, "input cannot be null");

		Client client = clientDataProvider.findByDocument(input.documentNumber());
		String contractNumber = contractNumberGenerator.next();
		ContractId contractId = ContractId.generate();

		CreditContract contract = CreditContract.create(
				contractId, contractNumber, client, input.correlationId());
		creditContractRepository.save(contract);
		return contract;
	}
}
