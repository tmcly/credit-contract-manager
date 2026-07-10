package br.com.creditcontract.application.port.out;

/** Result returned only after the broker accepts or rejects a publication. */
public record EventPublicationResult(boolean confirmed, String failureReason) {

	public static EventPublicationResult success() {
		return new EventPublicationResult(true, null);
	}

	public static EventPublicationResult failure(String reason) {
		return new EventPublicationResult(false, reason == null ? "broker did not confirm publication" : reason);
	}
}
