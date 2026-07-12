package br.com.creditcontract.application.usecase;

import br.com.creditcontract.application.exception.CreditContractNotFoundException;
import br.com.creditcontract.application.port.out.CreditContractRepository;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.entity.CreditReanalysis;
import br.com.creditcontract.domain.valueobject.ContractId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Accepts a client reanalysis request and atomically emits its work event. */
@Service
public class RequestCreditReanalysisUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestCreditReanalysisUseCase.class);
	private final CreditContractRepository repository;
	private final Duration cooldown;
	private final Clock clock;

	public RequestCreditReanalysisUseCase(
			CreditContractRepository repository,
			@Value("${credit-contract.reanalysis.cooldown}") Duration cooldown,
			Clock clock) {
		this.repository = Objects.requireNonNull(repository);
		this.cooldown = Objects.requireNonNull(cooldown);
		this.clock = Objects.requireNonNull(clock);
		if (cooldown.isZero() || cooldown.isNegative()) {
			throw new IllegalArgumentException("credit reanalysis cooldown must be positive");
		}
	}

	@Transactional
	public CreditReanalysisRequestResult execute(ContractId contractId, UUID correlationId) {
		Objects.requireNonNull(contractId, "contract id is required");
		Objects.requireNonNull(correlationId, "correlation id is required");
		CreditContract contract = repository.findById(contractId)
				.orElseThrow(() -> new CreditContractNotFoundException(contractId));
		LocalDateTime requestedAt = LocalDateTime.now(clock);
		CreditReanalysis reanalysis = contract.requestCreditReanalysis(
				requestedAt, cooldown, correlationId);
		repository.save(contract);
		LOGGER.atInfo()
				.addKeyValue("event", "credit_reanalysis_requested")
				.addKeyValue("contractId", contractId.asString())
				.addKeyValue("reanalysisId", reanalysis.getId())
				.addKeyValue("correlationId", correlationId)
				.log("Credit reanalysis requested");
		return new CreditReanalysisRequestResult(
				reanalysis.getId(), contractId, requestedAt, requestedAt.plus(cooldown));
	}
}
