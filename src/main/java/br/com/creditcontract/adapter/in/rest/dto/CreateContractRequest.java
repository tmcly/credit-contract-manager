package br.com.creditcontract.adapter.in.rest.dto;

/**
 * Request body for {@code POST /api/contracts}.
 *
 * <p>The only required field is the client's document number (CPF/CNPJ).
 * Everything else — client snapshot, credit limit, contract number — is
 * resolved by the use case through domain ports.
 */
public record CreateContractRequest(String documentNumber) {

	public CreateContractRequest {
		if (documentNumber == null || documentNumber.isBlank()) {
			throw new IllegalArgumentException("documentNumber is required");
		}
	}
}
