package br.com.creditcontract.adapter.out.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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
	Queue creditAnalysisRequestsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_ANALYSIS_REQUESTS_QUEUE, true);
	}

	@Bean
	Queue creditAnalysisResultsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_ANALYSIS_RESULTS_QUEUE, true);
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
}
