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

```
                         ┌─────────────────────────────┐
        HTTP (REST)      │     INTERFACES / ADAPTERS   │
   ────────────────────▶ │  br.com.creditcontract.     │
                         │        interfaces           │
                         │  • HealthCheckController     │
                         │  (translates JSON -> app)    │
                         └──────────────┬──────────────┘
                                        │ depends on
                                        ▼
                         ┌─────────────────────────────┐
      USE CASES         │       APPLICATION           │
                         │  br.com.creditcontract.     │
                         │       application           │
                         │  • *UseCase (S - SOLID)      │
                         │  • orchestrates the domain   │
                         │  • depends only on ABSTRACTIONS │
                         └──────────────┬──────────────┘
                                        │ depends on
                                        ▼
                         ┌─────────────────────────────┐
      BUSINESS RULES     │         DOMAIN (DDD)        │
                         │  br.com.creditcontract.     │
                         │         domain              │
                         │  • entity/   (CreditContract)│
                         │  • valueobject/ (ContractId, │
                         │      MonetaryAmount, ...)    │
                         │  • enumeration/ (ContractStatus) │
                         │  • NO Spring/JPA imports     │
                         └─────────────────────────────┘
                                        ▲
                                        │ implements (ports)
                                        │
                         ┌─────────────────────────────┐
   CONCRETE IMPL        │      INFRASTRUCTURE         │
                         │  br.com.creditcontract.    │
                         │      infrastructure        │
                         │  • persistence (adapter)    │
                         │  • Spring config            │
                         │  • database (when defined)  │
                         └─────────────────────────────┘
```

## Package structure

```
src/main/java/br/com/creditcontract/
├── CreditContractApplication.java     # main (Spring Boot)
├── domain/
│   ├── entity/                        # aggregates / entities (CreditContract)
│   ├── valueobject/                   # immutable value types
│   └── enumeration/                   # domain enums (ContractStatus)
├── application/                       # use cases (to be implemented)
├── infrastructure/                    # adapters (persistence, config)
└── interfaces/                        # entry points (REST controllers)
```

## Current state

- ✅ Scaffold functional (Spring Boot + Web + Actuator)
- ✅ Domain modeled: `CreditContract` aggregate + value objects + `ContractStatus`
- ✅ Unit tests passing (state transitions, invariants, versioning)
- ✅ Docker: `Dockerfile` (multi-stage) + `docker-compose.yml`
- ⏳ Use cases: not yet implemented
- ⏳ Database: not defined

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
