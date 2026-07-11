package br.com.creditcontract.application.port.out;

/** Publishes an integration event and waits for the broker's confirmation. */
public interface EventPublisher {

	EventPublicationResult publish(EventPublication event);
}
