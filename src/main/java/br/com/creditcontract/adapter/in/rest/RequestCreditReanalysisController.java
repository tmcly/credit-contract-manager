package br.com.creditcontract.adapter.in.rest;

import br.com.creditcontract.adapter.in.rest.dto.CreditReanalysisRequestResponse;
import br.com.creditcontract.application.usecase.CreditReanalysisRequestResult;
import br.com.creditcontract.application.usecase.RequestCreditReanalysisUseCase;
import br.com.creditcontract.domain.valueobject.ContractId;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/** REST command endpoint for client-requested credit reanalysis. */
@RestController
@RequestMapping("/api/contracts")
public class RequestCreditReanalysisController {

	private final RequestCreditReanalysisUseCase useCase;

	public RequestCreditReanalysisController(RequestCreditReanalysisUseCase useCase) {
		this.useCase = Objects.requireNonNull(useCase);
	}

	@PostMapping("/{id}/credit-reanalysis")
	@Parameter(
			name = HttpRequestLoggingFilter.CORRELATION_ID_HEADER,
			description = "Optional request correlation ID. The API generates one when omitted.",
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"))
	public ResponseEntity<CreditReanalysisRequestResponse> request(
			@PathVariable UUID id,
			@RequestAttribute(HttpRequestLoggingFilter.CORRELATION_ID_ATTRIBUTE)
			UUID correlationId) {
		CreditReanalysisRequestResult result = useCase.execute(ContractId.from(id), correlationId);
		return ResponseEntity.accepted().body(CreditReanalysisRequestResponse.from(result));
	}
}
