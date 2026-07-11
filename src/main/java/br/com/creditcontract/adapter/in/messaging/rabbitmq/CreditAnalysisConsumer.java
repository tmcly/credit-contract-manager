package br.com.creditcontract.adapter.in.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.messaging.rabbitmq.RabbitMqTopology;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisCommand;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisUseCase;
import br.com.creditcontract.domain.valueobject.ContractId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/** Inbound RabbitMQ adapter for at-least-once credit-analysis requests. */
@Component
public class CreditAnalysisConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreditAnalysisConsumer.class);

	private final ProcessCreditAnalysisUseCase useCase;
	private final ObjectMapper objectMapper;
	private final Counter successCounter;
	private final Counter failureCounter;
	private final Timer processingTimer;

	public CreditAnalysisConsumer(
			ProcessCreditAnalysisUseCase useCase,
			ObjectMapper objectMapper,
			MeterRegistry meterRegistry) {
		this.useCase = Objects.requireNonNull(useCase);
		this.objectMapper = Objects.requireNonNull(objectMapper);
		this.successCounter = meterRegistry.counter("credit_contract.consumer.success", "consumer", "credit-analysis");
		this.failureCounter = meterRegistry.counter("credit_contract.consumer.failure", "consumer", "credit-analysis");
		this.processingTimer = meterRegistry.timer("credit_contract.consumer.processing", "consumer", "credit-analysis");
	}

	@RabbitListener(queues = RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE)
	public void consume(Message message) throws IOException {
		UUID eventId = UUID.fromString(message.getMessageProperties().getMessageId());
		UUID correlationId = UUID.fromString(message.getMessageProperties().getCorrelationId());
		MDC.put("eventId", eventId.toString());
		MDC.put("correlationId", correlationId.toString());
		try {
			processingTimer.recordCallable(() -> {
				JsonNode payload = objectMapper.readTree(message.getBody());
				UUID contractId = UUID.fromString(payload.required("contractId").asText());
				useCase.execute(new ProcessCreditAnalysisCommand(
						ContractId.from(contractId), eventId, correlationId));
				return null;
			});
			successCounter.increment();
			LOGGER.atInfo()
					.addKeyValue("event", "message_consumed")
					.addKeyValue("consumer", "credit-analysis")
					.log("RabbitMQ message consumed");
		} catch (IOException exception) {
			failureCounter.increment();
			logFailure(exception);
			throw exception;
		} catch (Exception exception) {
			failureCounter.increment();
			logFailure(exception);
			if (exception instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			throw new IOException("could not process credit-analysis message", exception);
		} finally {
			MDC.remove("eventId");
			MDC.remove("correlationId");
		}
	}

	private void logFailure(Exception exception) {
		LOGGER.atWarn()
				.addKeyValue("event", "message_consumption_failed")
				.addKeyValue("consumer", "credit-analysis")
				.addKeyValue("errorType", exception.getClass().getSimpleName())
				.log("RabbitMQ message consumption failed");
	}
}
