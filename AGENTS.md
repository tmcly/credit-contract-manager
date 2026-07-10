# Credit Contract Manager - Agent Guide

This file contains durable instructions for any coding agent working in this
repository. Read it completely before planning or changing the project.

## Required reading order

1. `README.md`
2. `docs/architecture/overview.md`
3. Every ADR in `docs/architecture/decisions/`
4. `docs/roadmap.md`
5. The source files and tests affected by the requested change

Do not treat the roadmap as already implemented. Confirm the live repository
state before making current-state claims.

## Product intent

Credit Contract Manager is a learning-oriented, portfolio-quality backend for
the lifecycle of Brazilian personal credit contracts. It is not generic CRUD.
The intended lifecycle includes creation, credit analysis, approval, blocking,
cancellation, and reanalysis, with explicit business rules and traceability.

The project is also used to deepen Java and Spring knowledge. Prefer readable,
explicit implementations and explain non-obvious Java or architectural choices
when handing work off.

## Architectural invariants

- Keep the domain free of Spring, JPA, HTTP, messaging, and database concerns.
- Dependencies point inward: adapters -> application -> domain.
- Application output ports describe required capabilities; outbound adapters
  implement them.
- REST DTOs are transport models, not domain entities.
- JPA entities are persistence models, not domain entities.
- Map explicitly between domain and persistence models.
- Keep business state transitions and invariants inside the aggregate.
- Client data is an immutable snapshot embedded in the credit contract; it is
  not a locally owned client aggregate.
- `DocumentNumber` keeps its generic name but currently accepts CPF only.
- Monetary values represent Brazilian reais and use `BigDecimal`; do not add a
  currency field unless multi-currency becomes an explicit requirement.
- Status-specific reasons do not belong on `CreditContract`. Store one optional
  reason per transition in the status history.
- Flyway is the only schema creation/evolution mechanism. Hibernate validates
  the schema with `ddl-auto=validate`.

If a proposed change conflicts with an accepted ADR, call it out and update or
supersede the ADR deliberately instead of silently drifting from it.

## Git and pull-request workflow

- Start every change from an up-to-date `master`.
- Use one logical change per branch and pull request.
- Branch names use semantic prefixes such as `feat/`, `fix/`, `refactor/`,
  `docs/`, `test/`, or `chore/`.
- Use Conventional/Semantic Commit messages, for example:
  `feat: generate contract numbers with PostgreSQL`.
- Open pull requests ready for review, not as drafts.
- Write pull-request descriptions for humans: explain motivation, behavior,
  architectural impact, compatibility concerns, and verification evidence.
- Use concise tables, Mermaid diagrams, and emojis when they improve scanning;
  do not add decoration that obscures the content.
- Never commit unrelated local files, editor sessions, credentials, database
  volumes, or user-created scratch files.
- Preserve existing user changes in a dirty worktree.

## Implementation conventions

- Use Java 21 and the Spring Boot version managed by `pom.xml`.
- Prefer explicit Java over Lombok while the project remains learning-oriented.
- Constructor-inject required dependencies.
- Avoid exposing raw JPA repositories to application or domain code.
- Keep technology-specific code under `adapter/out` or `adapter/in`.
- Add a Flyway migration for every schema change; never edit an already applied
  migration after it has been shared.
- PostgreSQL constraints should reinforce important invariants, but they do not
  replace domain validation.
- Avoid non-deterministic test data. Local fakes may generate varied data only
  when a stable seed makes the same input produce the same output.
- Never generate sequential identifiers with `MAX(...) + 1`.
- Treat event delivery as at-least-once and make consumers idempotent.
- Do not publish database changes and broker messages as an unsafe dual write;
  use the transactional outbox described in the ADRs and roadmap.

## Verification

Run checks proportionate to the change.

Preferred local command when Java is available:

```bash
./mvnw clean test
```

Docker fallback, including Testcontainers support:

```bash
docker run --rm \
  -v "$PWD":/workspace \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  mvn -B clean test
```

For infrastructure or end-to-end changes, also validate:

```bash
docker compose config --quiet
docker compose up -d --build
```

Exercise the affected endpoint or message flow, inspect persisted state when
relevant, and tear down temporary resources afterward.

## Definition of done

A change is complete only when:

- behavior and architecture match the requested scope;
- relevant unit and integration tests pass;
- migrations and local infrastructure are verified when affected;
- documentation and ADRs reflect material decisions;
- the worktree contains no accidentally staged user files;
- the branch is committed, pushed, and represented by a ready pull request;
- the handoff states what changed, how it was verified, and any merge order or
  follow-up work that remains.
