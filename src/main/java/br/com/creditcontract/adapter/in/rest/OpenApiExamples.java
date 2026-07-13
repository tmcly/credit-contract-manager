package br.com.creditcontract.adapter.in.rest;

/** Compile-time JSON examples shared by OpenAPI operation annotations. */
final class OpenApiExamples {

	static final String CONTRACT_ID = "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a";

	static final String CREATE_CONTRACT_REQUEST = """
			{
			  "documentNumber": "529.982.247-25"
			}
			""";

	static final String DRAFT_CONTRACT_RESPONSE = """
			{
			  "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "contractNumber": "CT-2026-000001",
			  "clientName": "Maria Silva",
			  "status": "DRAFT",
			  "creditLimit": null,
			  "createdAt": "2026-07-13T10:15:30",
			  "version": 0
			}
			""";

	static final String ACTIVE_CONTRACT_RESPONSE = """
			{
			  "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "contractNumber": "CT-2026-000001",
			  "clientName": "Maria Silva",
			  "status": "ACTIVE",
			  "creditLimit": "7500.00",
			  "createdAt": "2026-07-13T10:15:30",
			  "version": 4
			}
			""";

	static final String ACCEPTED_CONTRACT_RESPONSE = """
			{
			  "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "contractNumber": "CT-2026-000001",
			  "clientName": "Maria Silva",
			  "status": "ACCEPTED",
			  "creditLimit": "7500.00",
			  "createdAt": "2026-07-13T10:15:30",
			  "version": 3
			}
			""";

	static final String BLOCKED_CONTRACT_RESPONSE = """
			{
			  "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "contractNumber": "CT-2026-000001",
			  "clientName": "Maria Silva",
			  "status": "BLOCKED",
			  "creditLimit": "7500.00",
			  "createdAt": "2026-07-13T10:15:30",
			  "version": 5
			}
			""";

	static final String CANCELLED_CONTRACT_RESPONSE = """
			{
			  "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "contractNumber": "CT-2026-000001",
			  "clientName": "Maria Silva",
			  "status": "CANCELLED",
			  "creditLimit": "7500.00",
			  "createdAt": "2026-07-13T10:15:30",
			  "version": 6
			}
			""";

	static final String BLOCK_CONTRACT_REQUEST = """
			{
			  "reason": "Payment overdue for more than 30 days"
			}
			""";

	static final String UNBLOCK_CONTRACT_REQUEST = """
			{
			  "reason": "Overdue balance was settled"
			}
			""";

	static final String CANCEL_CONTRACT_REQUEST = """
			{
			  "requestedBy": "LEGAL",
			  "reason": "Cancellation requested by court order"
			}
			""";

	static final String REANALYSIS_REQUEST_RESPONSE = """
			{
			  "requestId": "a4424800-0d30-49a9-bdb1-17206e00ea72",
			  "contractId": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "status": "REQUESTED",
			  "requestedAt": "2026-07-13T10:20:00",
			  "nextEligibleAt": "2026-08-12T10:20:00"
			}
			""";

	static final String CONTRACT_PAGE_RESPONSE = """
			{
			  "content": [
			    {
			      "id": "8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			      "contractNumber": "CT-2026-000001",
			      "clientName": "Maria Silva",
			      "status": "ACTIVE",
			      "creditLimit": "7500.00",
			      "createdAt": "2026-07-13T10:15:30",
			      "updatedAt": "2026-07-13T10:18:12",
			      "version": 4
			    }
			  ],
			  "page": 0,
			  "size": 20,
			  "totalElements": 1,
			  "totalPages": 1,
			  "first": true,
			  "last": true
			}
			""";

	static final String STATUS_HISTORY_PAGE_RESPONSE = """
			{
			  "content": [
			    {
			      "id": "60af2a43-bd11-442f-8051-5d993f6a9752",
			      "previousStatus": "ACTIVE",
			      "newStatus": "BLOCKED",
			      "reason": "Payment overdue for more than 30 days",
			      "changedAt": "2026-07-13T11:00:00"
			    }
			  ],
			  "page": 0,
			  "size": 20,
			  "totalElements": 1,
			  "totalPages": 1,
			  "first": true,
			  "last": true
			}
			""";

	static final String REANALYSIS_PAGE_RESPONSE = """
			{
			  "content": [
			    {
			      "id": "a4424800-0d30-49a9-bdb1-17206e00ea72",
			      "status": "APPROVED",
			      "previousLimit": "5000.00",
			      "newLimit": "7500.00",
			      "reason": null,
			      "requestedAt": "2026-07-13T10:20:00",
			      "completedAt": "2026-07-13T10:20:01"
			    }
			  ],
			  "page": 0,
			  "size": 20,
			  "totalElements": 1,
			  "totalPages": 1,
			  "first": true,
			  "last": true
			}
			""";

	static final String INVALID_REQUEST_PROBLEM = """
			{
			  "type": "/errors/request-validation",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "documentNumber is required",
			  "instance": "/api/contracts"
			}
			""";

	static final String INVALID_BLOCK_REQUEST_PROBLEM = """
			{
			  "type": "/errors/request-validation",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "blocking reason is required",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/blocking"
			}
			""";

	static final String INVALID_UNBLOCK_REQUEST_PROBLEM = """
			{
			  "type": "/errors/request-validation",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "unblocking reason is required",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/unblocking"
			}
			""";

	static final String INVALID_CANCELLATION_REQUEST_PROBLEM = """
			{
			  "type": "/errors/request-validation",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "requestedBy is required",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/cancellation"
			}
			""";

	static final String INVALID_CORRELATION_ID_PROBLEM = """
			{
			  "type": "/errors/invalid-correlation-id",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "X-Correlation-ID must be a valid UUID"
			}
			""";

	static final String INVALID_QUERY_PARAMETER_PROBLEM = """
			{
			  "type": "/errors/invalid-query-parameter",
			  "title": "Invalid query parameter",
			  "status": 400,
			  "detail": "size must be between 1 and 100",
			  "instance": "/api/contracts"
			}
			""";

	static final String CONTRACT_NOT_FOUND_PROBLEM = """
			{
			  "type": "/errors/credit-contract-not-found",
			  "title": "Credit contract not found",
			  "status": 404,
			  "detail": "Credit contract not found: 8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a"
			}
			""";

	static final String INVALID_TRANSITION_PROBLEM = """
			{
			  "type": "/errors/invalid-contract-transition",
			  "title": "Invalid contract transition",
			  "status": 409,
			  "detail": "credit contract cannot transition from UNDER_REVIEW to BLOCKED",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/blocking"
			}
			""";

	static final String ACCEPTANCE_CONFLICT_PROBLEM = """
			{
			  "type": "/errors/invalid-contract-transition",
			  "title": "Invalid contract transition",
			  "status": 409,
			  "detail": "credit contract cannot transition from UNDER_REVIEW to ACCEPTED",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/acceptance"
			}
			""";

	static final String UNBLOCK_CONFLICT_PROBLEM = """
			{
			  "type": "/errors/invalid-contract-transition",
			  "title": "Invalid contract transition",
			  "status": 409,
			  "detail": "credit contract cannot transition from ACTIVE to ACTIVE",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/unblocking"
			}
			""";

	static final String CANCELLATION_CONFLICT_PROBLEM = """
			{
			  "type": "/errors/invalid-contract-transition",
			  "title": "Invalid contract transition",
			  "status": 409,
			  "detail": "credit contract cannot transition from BLOCKED to CANCELLED",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/cancellation"
			}
			""";

	static final String REANALYSIS_NOT_ALLOWED_PROBLEM = """
			{
			  "type": "/errors/credit-reanalysis-not-allowed",
			  "title": "Credit reanalysis not allowed",
			  "status": 409,
			  "detail": "credit reanalysis requires an ACTIVE contract, but contract is BLOCKED",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/credit-reanalysis"
			}
			""";

	static final String REANALYSIS_COOLDOWN_PROBLEM = """
			{
			  "type": "/errors/credit-reanalysis-cooldown",
			  "title": "Credit reanalysis cooldown active",
			  "status": 429,
			  "detail": "credit reanalysis can be requested again at 2026-08-12T10:20",
			  "instance": "/api/contracts/8f2d7c44-3a64-4de5-940a-80c6a4cf1f7a/credit-reanalysis",
			  "nextEligibleAt": "2026-08-12T10:20:00"
			}
			""";

	private OpenApiExamples() {
	}
}
