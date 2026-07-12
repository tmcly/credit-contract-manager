package br.com.creditcontract.adapter.out.messaging.rabbitmq;

/** Stable RabbitMQ names shared by topology declaration and publication. */
public final class RabbitMqTopology {

	public static final String CONTRACT_EVENTS_EXCHANGE = "credit-contract.events";
	public static final String CREDIT_ANALYSIS_REQUESTS_QUEUE = "credit-analysis.requests";
	public static final String CREDIT_ANALYSIS_RESULTS_QUEUE = "credit-analysis.results";
	public static final String LEGACY_CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE =
			"credit-contract.activation.requests";
	public static final String CREDIT_CONTRACT_ACTIVATION_REQUESTS_QUEUE =
			"credit-contract.activation.requests.v2";
	public static final String CREDIT_CONTRACT_ACTIVATION_RESULTS_QUEUE = "credit-contract.activation.results";
	public static final String DEAD_LETTER_EXCHANGE = "credit-contract.dead-letter";
	public static final String CREDIT_ANALYSIS_DLQ = "credit-analysis.requests.dlq";
	public static final String CREDIT_CONTRACT_ACTIVATION_DLQ =
			"credit-contract.activation.requests.v2.dlq";
	public static final String CREDIT_ANALYSIS_DEAD_LETTER_ROUTING_KEY = "credit-analysis.requests.failed";
	public static final String CREDIT_CONTRACT_ACTIVATION_DEAD_LETTER_ROUTING_KEY =
			"credit-contract.activation.requests.v2.failed";
	public static final String CREDIT_CONTRACT_CREATED_ROUTING_KEY = "credit-contract.created.v1";
	public static final String CREDIT_ANALYSIS_APPROVED_ROUTING_KEY = "credit-analysis.approved.v1";
	public static final String CREDIT_ANALYSIS_REJECTED_ROUTING_KEY = "credit-analysis.rejected.v1";
	public static final String CREDIT_CONTRACT_ACCEPTED_ROUTING_KEY = "credit-contract.accepted.v1";
	public static final String CREDIT_CONTRACT_ACTIVATED_ROUTING_KEY = "credit-contract.activated.v1";

	private RabbitMqTopology() {
	}
}
