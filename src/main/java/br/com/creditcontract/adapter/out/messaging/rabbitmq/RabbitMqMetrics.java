package br.com.creditcontract.adapter.out.messaging.rabbitmq;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Properties;

/** Exposes queue depth from RabbitMQ through Micrometer when the broker is reachable. */
@Component
public class RabbitMqMetrics {

	private final AmqpAdmin rabbitAdmin;

	public RabbitMqMetrics(AmqpAdmin rabbitAdmin, MeterRegistry meterRegistry) {
		this.rabbitAdmin = Objects.requireNonNull(rabbitAdmin);
		Gauge.builder("credit_contract.rabbitmq.queue.depth", this,
				metrics -> metrics.messageCount(RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE))
				.tag("queue", RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE)
				.register(meterRegistry);
		Gauge.builder("credit_contract.rabbitmq.queue.depth", this,
				metrics -> metrics.messageCount(RabbitMqTopology.CREDIT_ANALYSIS_DLQ))
				.tag("queue", RabbitMqTopology.CREDIT_ANALYSIS_DLQ)
				.register(meterRegistry);
	}

	private double messageCount(String queue) {
		try {
			Properties properties = rabbitAdmin.getQueueProperties(queue);
			if (properties == null) {
				return 0;
			}
			Object count = properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
			return count instanceof Number number ? number.doubleValue() : 0;
		} catch (RuntimeException unavailable) {
			return Double.NaN;
		}
	}
}
