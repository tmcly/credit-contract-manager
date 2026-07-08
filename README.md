# Contrato de Crédito 🏦

Sistema de gestão de contratos de crédito (criação, cancelamento, bloqueio,
reanálise). Projeto em construção — iniciado do zero como estudo de
**Java 21 + Spring Boot 3** com foco em boas práticas.

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 (LTS) |
| Framework | Spring Boot 3.5.x |
| Build | Maven |
| Banco | ⚠️ AINDA NÃO DEFINIDO (SQL vs NoSQL) — decisão adiada |

## Arquitetura (Clean Architecture + DDD + SOLID)

Dependência aponta para DENTRO. O domínio não conhece nenhum framework.

```
                         ┌─────────────────────────────┐
        HTTP (REST)      │     INTERFACES / ADAPTERS   │
   ────────────────────▶ │  br.com.contratocredito.    │
                         │        interfaces           │
                         │  • HealthCheckController     │
                         │  (traduz JSON -> application)│
                         └──────────────┬──────────────┘
                                        │ depends on
                                        ▼
                         ┌─────────────────────────────┐
      CASOS DE USO       │       APPLICATION           │
                         │  br.com.contratocredito.    │
                         │       application           │
                         │  • *UseCase (S - SOLID)      │
                         │  • orquestra o domínio       │
                         │  • depende só de ABSTRAÇÕES │
                         └──────────────┬──────────────┘
                                        │ depends on
                                        ▼
                         ┌─────────────────────────────┐
      REGRAS DE NEGÓCIO  │         DOMAIN (DDD)        │
                         │  br.com.contratocredito.    │
                         │         domain              │
                         │  • entidades / agregados    │
                         │  • value objects            │
                         │  • ports (interfaces)       │
                         │  • NÃO importa Spring/JPA   │
                         └─────────────────────────────┘
                                        ▲
                                        │ implements (ports)
                                        │
                         ┌─────────────────────────────┐
   IMPLEMENTAÇÕES REAIS  │      INFRASTRUCTURE         │
                         │  br.com.contratocredito.    │
                         │      infrastructure         │
                         │  • persistência (adapter)   │
                         │  • configs Spring           │
                         │  • banco (quando definido)  │
                         └─────────────────────────────┘
```

## Estrutura de pacotes

```
src/main/java/br/com/contratocredito/
├── ContratoCreditoApplication.java   # main (Spring Boot)
├── domain/                           # regras de negócio (DDD)
├── application/                      # use cases (casos de uso)
├── infrastructure/                   # adapters (persistência, config)
└── interfaces/                       # entrada (REST controllers)
```

## Estado atual

- ✅ Scaffold funcional (Spring Boot + Web + Actuator)
- ✅ Endpoint `GET /health` -> `{ "status": "UP" }`
- ✅ Endpoint nativo `GET /actuator/health`
- ⏳ Domínio: vazio (a ser desenhado em conjunto)
- ⏳ Banco de dados: não definido

## Como rodar

```bash
# compilar
./mvnw clean package

# rodar
./mvnw spring-boot:run
# ou
java -jar target/contrato-credito-0.0.1-SNAPSHOT.jar
```

## Healthcheck

```bash
curl http://localhost:8080/health
# {"status":"UP"}

curl http://localhost:8080/actuator/health
# {"status":"UP", ...}
```
