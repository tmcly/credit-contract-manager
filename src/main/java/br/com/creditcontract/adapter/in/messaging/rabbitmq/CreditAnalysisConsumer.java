package br.com.creditcontract.adapter.in.messaging.rabbitmq;

import br.com.creditcontract.adapter.out.messaging.rabbitmq.RabbitMqTopology;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisCommand;
import br.com.creditcontract.application.usecase.ProcessCreditAnalysisUseCase;
import br.com.creditcontract.domain.valueobject.ContractId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/** Inbound RabbitMQ adapter for at-least-once credit-analysis requests. */
@Component
public class CreditAnalysisConsumer {

	private final ProcessCreditAnalysisUseCase useCase;
	private final ObjectMapper objectMapper;

	public CreditAnalysisConsumer(
			ProcessCreditAnalysisUseCase useCase,
			ObjectMapper objectMapper) {
		this.useCase = Objects.requireNonNull(useCase);
		this.objectMapper = Objects.requireNonNull(objectMapper);
	}

	@RabbitListener(queues = RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE)
	public void consume(Message message) throws IOException {
		JsonNode payload = objectMapper.readTree(message.getBody());
		UUID contractId = UUID.fromString(payload.required("contractId").asText());
		UUID eventId = UUID.fromString(message.getMessageProperties().getMessageId());
		UUID correlationId = UUID.fromString(message.getMessageProperties().getCorrelationId());

		useCase.execute(new ProcessCreditAnalysisCommand(
				ContractId.from(contractId), eventId, correlationId));
	}
}
