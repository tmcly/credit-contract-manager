package br.com.creditcontract.domain.valueobject;

import java.util.Objects;

/**
 * Client embedded within the contract.
 *
 * As in the original system, the unit of work was the contract, not a
 * standalone client aggregate — so Client is modeled here as a value object
 * (immutable), nested in {@link br.com.creditcontract.domain.entity.CreditContract}.
 * If a client ever needs its own lifecycle (multiple contracts per client),
 * this can become an entity later.
 */
public record Client(DocumentNumber documentNumber, String name, Address address) {

	public Client {
		Objects.requireNonNull(documentNumber, "client document number cannot be null");
		Objects.requireNonNull(name, "client name cannot be null");
		Objects.requireNonNull(address, "client address cannot be null");
	}
}
