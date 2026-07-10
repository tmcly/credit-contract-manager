package br.com.creditcontract.application.usecase;

import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.application.port.out.ClientDataProvider;
import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import br.com.creditcontract.application.port.out.CreditLimitProvider;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.valueobject.Client;
import br.com.creditcontract.domain.valueobject.ContractId;
import br.com.creditcontract.domain.valueobject.MonetaryAmount;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Orchestrates the creation of a new credit contract.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Fetch the client snapshot from the external registry.</li>
 *   <li>Obtain the approved credit limit from the risk engine.</li>
 *   <li>Generate the next sequential contract number.</li>
 *   <li>Assemble and return the {@link CreditContract} aggregate.</li>
 * </ol>
 *
 * <p>This use case is the "S" in SOLID — it does exactly one thing.
 * Persistence is deferred to a future use case / repository port.
 */
@Service
public class CreateContractUseCase {

	private final ClientDataProvider clientDataProvider;
	private final CreditLimitProvider creditLimitProvider;
	private final ContractNumberGenerator contractNumberGenerator;
	private final CreditContractRepository creditContractRepository;

	public CreateContractUseCase(ClientDataProvider clientDataProvider,
	                             CreditLimitProvider creditLimitProvider,
	                             ContractNumberGenerator contractNumberGenerator,
	                             CreditContractRepository creditContractRepository) {
		this.clientDataProvider = Objects.requireNonNull(clientDataProvider);
		this.creditLimitProvider = Objects.requireNonNull(creditLimitProvider);
		this.contractNumberGenerator = Objects.requireNonNull(contractNumberGenerator);
		this.creditContractRepository = Objects.requireNonNull(creditContractRepository);
	}

	/**
	 * Creates a new credit contract for the given client.
	 *
	 * @param input the document number of the client
	 * @return the newly created contract aggregate
	 */
	public CreditContract execute(CreateContractInput input) {
		Objects.requireNonNull(input, "input cannot be null");

		Client client = clientDataProvider.findByDocument(input.documentNumber());
		MonetaryAmount creditLimit = creditLimitProvider.getLimitFor(input.documentNumber());
		String contractNumber = contractNumberGenerator.next();
		ContractId contractId = ContractId.generate();

		CreditContract contract = CreditContract.create(contractId, contractNumber, client, creditLimit);
		creditContractRepository.save(contract);
		return contract;
	}
}
