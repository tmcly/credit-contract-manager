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
| Database | ⚠️ NOT YET DEFINED (SQL vs NoSQL) — decision deferred |

## Architecture (Clean Architecture + DDD + SOLID)

Dependencies point inwards. The domain knows nothing about any framework.

```text
HTTP ──▶ adapter.in.rest ──▶ application.usecase ──▶ domain
                                │                      │
                                │ invokes              │ declares
                                ▼                      ▼
                           domain.port ◀──── implements ──── adapter.out.stub
```

Inbound adapters translate external requests into use-case calls. Outbound
adapters implement domain ports for databases, APIs, queues, or local stubs.

## Package structure

```
src/main/java/br/com/creditcontract/
├── CreditContractApplication.java     # main (Spring Boot)
├── domain/
│   ├── entity/                        # aggregates / entities (CreditContract)
│   ├── valueobject/                   # immutable value types
│   ├── enums/                         # domain enums (ContractStatus)
│   └── port/                          # outbound contracts
├── application/
│   └── usecase/                       # application orchestration
└── adapter/
    ├── in/rest/                       # REST controllers and DTOs
    └── out/stub/                      # local implementations of domain ports
```

## Current state

- ✅ Scaffold functional (Spring Boot + Web + Actuator)
- ✅ Domain modeled: `CreditContract` aggregate + value objects + `ContractStatus`
- ✅ Domain ports: `ContractNumberGenerator`, `ClientDataProvider`, `CreditLimitProvider`
- ✅ First use case: `CreateContractUseCase` (S of SOLID)
- ✅ REST endpoint: `POST /api/contracts` — creates a contract via stubs
- ✅ Unit tests passing (23 tests: 12 stub + 5 use case + 2 controller + 4 domain)
- ✅ Docker: `Dockerfile` (multi-stage) + `docker-compose.yml`
- ⏳ Database: not defined
- ⏳ Block / cancel / reanalyze use cases: not yet implemented

## API

```bash
# Create a contract
curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -d '{"documentNumber": "12345678900"}'

# Response: 201 Created
# {
#   "id": "uuid...",
#   "contractNumber": "CT-2026-000001",
#   "clientName": "Stub Client",
#   "status": "DRAFT",
#   "currency": "BRL",
#   "creditLimit": "1000.00",
#   "createdAt": "...",
#   "version": 0
# }
```

The local credit-engine stub assigns deterministic limits from BRL 1,000.00
to BRL 15,000.00 according to the final digit of the document number.

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

## Healthcheck

```bash
curl http://localhost:8080/health
# {"status":"UP"}
```
