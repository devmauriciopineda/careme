---
description: "Use when writing, modifying or reviewing Java/Spring Boot backend code, resources or Maven build files under backend/. Covers layering, validation, persistence and testing expectations."
applyTo: backend/src/main/java/**/*.java, backend/src/test/java/**/*.java, backend/src/main/resources/**, backend/pom.xml
---

# Backend (Java / Spring Boot) — Careme

Follow the full standard: `docs/standards/java-springboot-standards.md`. This file only
highlights what is specific to this repository.

## Layering (keep it one-directional)

`controller → service → repository`, with `entity` and `dto` as data carriers and `exception`
for cross-cutting error handling. Controllers stay thin and delegate; services own the logic;
persistence stays behind repository interfaces.

- Do not let a controller reach a repository directly.
- Do not leak JPA entities into the API; map to `dto` records.
- Return errors through the existing `@RestControllerAdvice` (`exception/ApiExceptionHandler`).

## Contract

`backend/README.md` documents the endpoints and payloads — it is the source of truth for the API
contract. Change the contract there first, then the code and the frontend consumer.

## Conventions

- Java 21, Spring Boot 3.5; constructor injection, no field injection.
- Bean Validation on request DTOs; validation messages in Spanish (they reach the user).
- Schema changes go through a new Flyway migration — never edit an applied migration.
- New behavior needs JUnit 5 tests; `.\mvnw.cmd verify` runs the suite plus the JaCoCo gate.

## LLM integration

The assistant is selected by `CAREME_LLM_MODE` (default `openai`, the real provider; `fake` selects
the simulated assistant, with no network). Keep the `fake` implementations working — tests and local
development rely on them — and never expose the
API key (`CAREME_LLM_API_KEY`) to the frontend or logs.
