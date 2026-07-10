package br.com.creditcontract.application.usecase;

/**
 * Input data for {@link CreateContractUseCase}.
 *
 * <p>The only mandatory field is the client's document number — everything
 * else is resolved by the domain ports (client snapshot, credit limit,
 * contract number).
 */
public record CreateContractInput(String documentNumber) {

	public CreateContractInput {
		if (documentNumber == null || documentNumber.isBlank()) {
			throw new IllegalArgumentException("documentNumber is required");
		}
	}
}
