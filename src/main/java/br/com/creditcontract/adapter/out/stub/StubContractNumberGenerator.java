package br.com.creditcontract.adapter.out.stub;

import br.com.creditcontract.application.port.out.ContractNumberGenerator;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stub implementation that generates sequential contract numbers
 * with the format {@code CT-YYYY-NNNNNN}.
 *
 * <p>In production this would be replaced by a database sequence or a
 * distributed ID generator — the domain never needs to change.
 */
@Component
public class StubContractNumberGenerator implements ContractNumberGenerator {

	private final AtomicLong sequence = new AtomicLong(0);

	@Override
	public String next() {
		long nextValue = sequence.incrementAndGet();
		return "CT-%d-%06d".formatted(Year.now().getValue(), nextValue);
	}
}
