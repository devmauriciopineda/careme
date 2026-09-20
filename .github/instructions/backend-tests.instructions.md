---
description: "Use when writing or modifying backend tests under backend/src/test. Covers JUnit 5, Mockito, MockMvc, Testcontainers and the coverage gate."
applyTo: backend/src/test/**
---

# Backend tests — Careme

Stack: JUnit 5, Mockito, MockMvc, Spring Boot Test, AssertJ, Testcontainers.

- Unit-test services with Mockito; use `@WebMvcTest` + MockMvc for controllers and
  `@SpringBootTest` + Testcontainers for integration paths that need PostgreSQL.
- Assert with AssertJ; one behavior per test, names describe the behavior, not the method.
- Integration tests need a reachable container runtime (Podman exposes a Docker-compatible API).
- Run `.\mvnw.cmd test` for the suite and `.\mvnw.cmd verify` for the suite plus the JaCoCo
  coverage gate before considering work done.
- Keep the `fake` LLM modes selected explicitly for tests — `backend/src/test/resources/application.properties`
  sets `careme.llm.mode=fake` — so the suite runs without network access even though the application's
  default is the real assistant.
