# Credit Contract Manager 🏦

Credit contract management system (create, cancel, block, reanalyze).
Built from scratch as a study project focused on **Java 21 + Spring Boot 3**
with strong emphasis on software design best practices.

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5.x |
| Build | Maven |
| Database | PostgreSQL 17 + Spring Data JPA + Flyway |
| Integration tests | Testcontainers |

## Architecture (Clean Architecture + DDD + SOLID)

Dependencies point inwards. The domain knows nothing about any framework.

```text
HTTP ──▶ adapter.in.rest ──▶ application.usecase ──▶ domain
                                │
                                │ invokes
                                ▼
                     application.port.out ◀──── implements ──── adapter.out
```

Inbound adapters translate external requests into use-case calls. The
application owns the output ports required by those use cases, and outbound
adapters implement them for databases, APIs, queues, or local stubs.

## Package structure

```
src/main/java/br/com/creditcontract/
├── CreditContractApplication.java     # main (Spring Boot)
├── domain/
│   ├── entity/                        # aggregates / entities (CreditContract)
│   ├── valueobject/                   # immutable value types
│   ├── enums/                         # domain enums (ContractStatus)
│   └── exception/                     # domain validation errors
├── application/
│   ├── usecase/                       # application orchestration
│   ├── port/out/                      # required external capabilities
│   └── exception/                     # use-case and integration errors
└── adapter/
    ├── in/rest/                       # REST controllers and DTOs
    └── out/
        ├── stub/                      # local external-service substitutes
        └── persistence/jpa/           # PostgreSQL persistence adapter
```

## Current state

- ✅ Scaffold functional (Spring Boot + Web + Actuator)
- ✅ Domain modeled: `CreditContract` aggregate + value objects + `ContractStatus`
- ✅ Application output ports: `ContractNumberGenerator`, `ClientDataProvider`, `CreditLimitProvider`
- ✅ CPF/CNPJ value object: normalization and check-digit validation
- ✅ First use case: `CreateContractUseCase` (S of SOLID)
- ✅ REST endpoint: `POST /api/contracts` — creates a contract via stubs
- ✅ PostgreSQL persistence with client snapshot in `credit_contracts`
- ✅ Generic status changes in `contract_status_history`
- ✅ Flyway schema migration + Testcontainers integration test
- ✅ Docker: `Dockerfile` (multi-stage) + `docker-compose.yml`
- ⏳ Block / cancel / reanalyze use cases: not yet implemented

## API

```bash
# Create a contract
curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -d '{"documentNumber": "529.982.247-25"}'

# Response: 201 Created
# {
#   "id": "uuid...",
#   "contractNumber": "CT-2026-000001",
#   "clientName": "Stub Client",
#   "status": "DRAFT",
#   "creditLimit": "5000.00",
#   "createdAt": "...",
#   "version": 0
# }
```

The local credit-engine stub assigns deterministic limits from R$ 1,000.00
to R$ 15,000.00 according to the final digit of the document number.

## Run it

### With Maven
```bash
./mvnw clean package
./mvnw spring-boot:run
```

### With Docker
```bash
docker compose up --build
```

This starts both the application and PostgreSQL. Flyway applies the schema
automatically and JPA only validates it (`ddl-auto=validate`).

## Healthcheck

```bash
curl http://localhost:8080/health
# {"status":"UP"}
```
