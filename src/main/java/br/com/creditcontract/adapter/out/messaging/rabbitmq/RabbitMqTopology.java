package br.com.creditcontract.adapter.out.messaging.rabbitmq;

/** Stable RabbitMQ names shared by topology declaration and publication. */
public final class RabbitMqTopology {

	public static final String CONTRACT_EVENTS_EXCHANGE = "credit-contract.events";
	public static final String CREDIT_ANALYSIS_REQUESTS_QUEUE = "credit-analysis.requests";
	public static final String CREDIT_CONTRACT_CREATED_ROUTING_KEY = "credit-contract.created.v1";

	private RabbitMqTopology() {
	}
}
