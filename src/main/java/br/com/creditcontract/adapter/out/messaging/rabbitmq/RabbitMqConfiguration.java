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
	Queue creditReanalysisRequestsQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_REANALYSIS_REQUESTS_QUEUE)
				.deadLetterExchange(RabbitMqTopology.DEAD_LETTER_EXCHANGE)
				.deadLetterRoutingKey(RabbitMqTopology.CREDIT_REANALYSIS_DEAD_LETTER_ROUTING_KEY)
				.build();
	}

	@Bean
	Queue creditReanalysisDeadLetterQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_REANALYSIS_DLQ).build();
	}

	@Bean
	Binding creditReanalysisDeadLetterBinding(
			DirectExchange deadLetterExchange,
			Queue creditReanalysisDeadLetterQueue) {
		return BindingBuilder.bind(creditReanalysisDeadLetterQueue)
				.to(deadLetterExchange)
				.with(RabbitMqTopology.CREDIT_REANALYSIS_DEAD_LETTER_ROUTING_KEY);
	}

	@Bean
	Queue creditReanalysisResultsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_REANALYSIS_RESULTS_QUEUE, true);
	}

	@Bean
	Queue legacyCreditContractActivationRequestsQueue() {
		return new Queue(RabbitMqTopology.LEGACY_CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE, true);
	}

	@Bean
	Queue creditContractActivationRequestsQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE)
				.deadLetterExchange(RabbitMqTopology.DEAD_LETTER_EXCHANGE)
				.deadLetterRoutingKey(
						RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_DEAD_LETTER_ROUTING_KEY)
				.build();
	}

	@Bean
	Queue creditContractActivationDeadLetterQueue() {
		return QueueBuilder.durable(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_DLQ).build();
	}

	@Bean
	Binding creditContractActivationDeadLetterBinding(
			DirectExchange deadLetterExchange,
			Queue creditContractActivationDeadLetterQueue) {
		return BindingBuilder.bind(creditContractActivationDeadLetterQueue)
				.to(deadLetterExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_DEAD_LETTER_ROUTING_KEY);
	}

	@Bean
	Queue creditContractActivationResultsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATION_RESULTS_QUEUE, true);
	}

	@Bean
	Queue creditContractLifecycleEventsQueue() {
		return new Queue(RabbitMqTopology.CREDIT_CONTRACT_LIFECYCLE_EVENTS_QUEUE, true);
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
	Binding creditReanalysisRequestedBinding(
			DirectExchange contractEventsExchange,
			Queue creditReanalysisRequestsQueue) {
		return BindingBuilder.bind(creditReanalysisRequestsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_REANALYSIS_REQUESTED_ROUTING_KEY);
	}

	@Bean
	Binding creditReanalysisApprovedBinding(
			DirectExchange contractEventsExchange,
			Queue creditReanalysisResultsQueue) {
		return BindingBuilder.bind(creditReanalysisResultsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_REANALYSIS_APPROVED_ROUTING_KEY);
	}

	@Bean
	Binding creditReanalysisRejectedBinding(
			DirectExchange contractEventsExchange,
			Queue creditReanalysisResultsQueue) {
		return BindingBuilder.bind(creditReanalysisResultsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_REANALYSIS_REJECTED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractAcceptedBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractActivationRequestsQueue) {
		return BindingBuilder.bind(creditContractActivationRequestsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_ACCEPTED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractActivatedBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractActivationResultsQueue) {
		return BindingBuilder.bind(creditContractActivationResultsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_ACTIVATED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractBlockedBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractLifecycleEventsQueue) {
		return BindingBuilder.bind(creditContractLifecycleEventsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_BLOCKED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractUnblockedBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractLifecycleEventsQueue) {
		return BindingBuilder.bind(creditContractLifecycleEventsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_UNBLOCKED_ROUTING_KEY);
	}

	@Bean
	Binding creditContractCancelledBinding(
			DirectExchange contractEventsExchange,
			Queue creditContractLifecycleEventsQueue) {
		return BindingBuilder.bind(creditContractLifecycleEventsQueue)
				.to(contractEventsExchange)
				.with(RabbitMqTopology.CREDIT_CONTRACT_CANCELLED_ROUTING_KEY);
	}
}
