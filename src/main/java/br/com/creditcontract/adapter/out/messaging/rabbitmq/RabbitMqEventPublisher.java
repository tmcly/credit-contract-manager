package br.com.creditcontract.adapter.out.messaging.rabbitmq;

import br.com.creditcontract.application.port.out.EventPublication;
import br.com.creditcontract.application.port.out.EventPublicationResult;
import br.com.creditcontract.application.port.out.EventPublisher;
import br.com.creditcontract.domain.event.CreditAnalysisApproved;
import br.com.creditcontract.domain.event.CreditAnalysisRejected;
import br.com.creditcontract.domain.event.CreditContractCreated;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final Duration confirmTimeout;

	public RabbitMqEventPublisher(
			RabbitTemplate rabbitTemplate,
			@Value("${credit-contract.outbox.confirm-timeout}") Duration confirmTimeout) {
		this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate);
		this.confirmTimeout = Objects.requireNonNull(confirmTimeout);
	}

	@Override
	public EventPublicationResult publish(EventPublication event) {
		CorrelationData correlationData = new CorrelationData(event.eventId().toString());
		try {
			rabbitTemplate.send(
					RabbitMqTopology.CONTRACT_EVENTS_EXCHANGE,
					routingKey(event),
					message(event),
					correlationData);
			CorrelationData.Confirm confirm = correlationData.getFuture()
					.get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
			if (!confirm.isAck()) {
				return EventPublicationResult.failure(confirm.getReason());
			}
			if (correlationData.getReturned() != null) {
				return EventPublicationResult.failure(
						"message was returned: " + correlationData.getReturned().getReplyText());
			}
			return EventPublicationResult.success();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return EventPublicationResult.failure("publication interrupted");
		} catch (Exception exception) {
			return EventPublicationResult.failure(exception.getMessage());
		}
	}

	private String routingKey(EventPublication event) {
		if (CreditContractCreated.EVENT_TYPE.equals(event.eventType()) && event.schemaVersion() == 1) {
			return RabbitMqTopology.CREDIT_CONTRACT_CREATED_ROUTING_KEY;
		}
		if (CreditAnalysisApproved.EVENT_TYPE.equals(event.eventType()) && event.schemaVersion() == 1) {
			return RabbitMqTopology.CREDIT_ANALYSIS_APPROVED_ROUTING_KEY;
		}
		if (CreditAnalysisRejected.EVENT_TYPE.equals(event.eventType()) && event.schemaVersion() == 1) {
			return RabbitMqTopology.CREDIT_ANALYSIS_REJECTED_ROUTING_KEY;
		}
		throw new IllegalArgumentException(
				"unsupported event routing: " + event.eventType() + " v" + event.schemaVersion());
	}

	private Message message(EventPublication event) {
		var builder = MessageBuilder
				.withBody(event.payload().getBytes(StandardCharsets.UTF_8))
				.setContentType(MessageProperties.CONTENT_TYPE_JSON)
				.setContentEncoding(StandardCharsets.UTF_8.name())
				.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
				.setMessageId(event.eventId().toString())
				.setType(event.eventType())
				.setCorrelationId(event.correlationId().toString())
				.setHeader("eventId", event.eventId().toString())
				.setHeader("aggregateId", event.aggregateId().toString())
				.setHeader("aggregateType", event.aggregateType())
				.setHeader("eventType", event.eventType())
				.setHeader("schemaVersion", event.schemaVersion())
				.setHeader("occurredAt", event.occurredAt().toString());
		if (event.causationId() != null) {
			builder.setHeader("causationId", event.causationId().toString());
		}
		return builder.build();
	}
}
