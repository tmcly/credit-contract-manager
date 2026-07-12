package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditContractResponse;
import br.com.creditcontract.adapter.in.rest.dto.UnblockCreditContractRequest;
import br.com.creditcontract.application.usecase.UnblockCreditContractUseCase;
import br.com.creditcontract.domain.entity.CreditContract;
import br.com.creditcontract.domain.valueobject.ContractId;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/** REST command endpoint used by external applications to unblock contracts. */
@RestController
@RequestMapping("/api/contracts")
public class UnblockCreditContractController {

	private final UnblockCreditContractUseCase useCase;

	public UnblockCreditContractController(UnblockCreditContractUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/unblocking")
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditContractResponse> unblock(
			@PathVariable UUID id,
			@Valid @RequestBody UnblockCreditContractRequest request,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE) UUID correlationId) {
		CreditContract contract = useCase.execute(
				ContractId.from(id), request.reason(), correlationId);
		return ResponseEntity.ok(CreditContractResponse.from(
				contract.getId().asString(),
				contract.getContractNumber(),
				contract.getClient().name(),
				contract.getStatus().name(),
				contract.getCreditLimit(),
				contract.getCreatedAt(),
				contract.getVersion()));
	}
}
