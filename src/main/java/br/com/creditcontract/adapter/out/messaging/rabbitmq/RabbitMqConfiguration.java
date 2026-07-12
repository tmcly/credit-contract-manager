package br.com.creditcontract.adapter.out.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RabbitMqConfiguration {

	@Bean
	DirectExchange contractEventsExchange() {
		return new DirectExchange(RabbitMqTopology.CONTRACT_EVENTS_EXCHANGE, true, false);
	}

	@Bean
	DirectExchange deadLetterExchange() {
		return new DirectExchange(RabbitMqTopology.DEAD_LETTER_EXCHANGE, true, false);
	}

	@Bean
	Queue creditAnalysisRequestsQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE)
				.deadLetterExchange(RabbitMqTopology.DEAD_LETTER_EXCHANGE)
				.deadLetterRoutingKey(RabbitMqTopology.CREDIT_ANALYSIS_DEAD_LETTER_ROUTING_KEY)
				.build();
	}

	@Bean
	Queue creditAnalysisDeadLetterQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_ANALYSIS_DLQ).build();
	}

	@Bean
	Binding creditAnalysisDeadLetterBinding(
			DirectExchange deadLetterExchange,
			Queue creditAnalysisDeadLetterQueue) {
		return BindingBuilder.bind(creditAnalysisDeadLetterQueue)
				.to(deadLetterExchange)
				.with(RabbitMqTopology.CREDIT_ANALYSIS_DEAD_LETTER_ROUTING_KEY);
	}

	@Bean
	Queue creditAnalysisResultsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_ANALYSIS_RESULTS_QUEUE, true);
	}

	@Bean
	Queue creditContractActivationRequestsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE, true);
	}

	@Bean
	Binding creditContractCreatedBinding(
			DirectExchange contractEventsExchange,
			Queue creditAnalysisRequestsQueue) {
		return BindingBuilder.bind(creditAnalysisRequestsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_CREATED_ROUTING_KEY);
	}

	@Bean
	Binding creditAnalysisApprovedBinding(
			DirectExchange contractEventsExchange,
			Queue creditAnalysisResultsQueue) {
		return BindingBuilder.bind(creditAnalysisResultsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_ANALYSIS_APPROVED_ROUTING_KEY);
	}

	@Bean
	Binding creditAnalysisRejectedBinding(
			DirectExchange contractEventsExchange,
			Queue creditAnalysisResultsQueue) {
		return BindingBuilder.bind(creditAnalysisResultsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_ANALYSIS_REJECTED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractAcceptedBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractActivationRequestsQueue) {
		return BindingBuilder.bind(creditContractActivationRequestsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_ACCEPTED_ROUTING_KEY);
	}
}
