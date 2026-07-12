package br.com.creditcontract.adapter.in.scheduling;

import br.com.creditcontract.application.usecase.CancelExpiredBlockedContractsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Periodically applies the configured blocked-contract expiration policy. */
@Component
public class BlockedContractExpirationScheduler {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(BlockedContractExpirationScheduler.class);
	private final CancelExpiredBlockedContractsUseCase useCase;
	private final Duration expiration;
	private final int batchSize;

	public BlockedContractExpirationScheduler(
			CancelExpiredBlockedContractsUseCase useCase,
			@Value("${credit-contract.cancellation.blocked-expiration}") Duration expiration,
			@Value("${credit-contract.cancellation.batch-size}") int batchSize) {
		this.useCase = Objects.requireNonNull(useCase);
		this.expiration = Objects.requireNonNull(expiration);
		if (expiration.isZero() || expiration.isNegative()) {
			throw new IllegalArgumentException("blocked expiration must be positive");
		}
		if (batchSize <= 0) {
			throw new IllegalArgumentException("cancellation batch size must be positive");
		}
		this.batchSize = batchSize;
	}

	@Scheduled(
			initialDelayString = "${credit-contract.cancellation.initial-delay}",
			fixedDelayString = "${credit-contract.cancellation.fixed-delay}")
	public void cancelExpiredContracts() {
		UUID correlationId = UUID.randomUUID();
		int cancelled = useCase.execute(
				LocalDateTime.now().minus(expiration), batchSize, correlationId);
		if (cancelled > 0) {
			LOGGER.atInfo()
					.addKeyValue("event", "expired_contract_cancellation_batch_completed")
					.addKeyValue("cancelledContracts", cancelled)
					.addKeyValue("correlationId", correlationId)
					.log("Expired blocked contracts cancelled");
		}
	}
}
