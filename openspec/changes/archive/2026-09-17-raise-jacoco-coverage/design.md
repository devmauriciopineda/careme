## Context

The backend already has a JaCoCo `verify` gate requiring 90% branch and line
coverage. The current report aggregates all production classes, including older
services and error paths, so this change must improve test coverage without
changing production behavior or relaxing the gate.

## Goals / Non-Goals

**Goals:**

- Use the JaCoCo report to prioritize uncovered branches and lines.
- Add deterministic tests at the narrowest suitable level: unit tests for pure
  logic, MockMvc tests for HTTP error paths, and Testcontainers tests for real
  persistence boundaries.
- Finish with the complete backend suite and `mvn verify` passing at 90% or
  higher for both configured counters.

**Non-Goals:**

- No production behavior, API, schema, dependency, or coverage-threshold
  changes.
- No tests that merely execute code without asserting observable behavior.
- No frontend coverage work.

## Decisions

- **Prioritize by JaCoCo report:** start with classes contributing the largest
  missed branch count, then address remaining line coverage. This gives the
  highest coverage gain per test and avoids speculative tests.
- **Test at existing boundaries:** use JUnit/Mockito for services and parsers,
  MockMvc for controllers and exception mapping, and Testcontainers only where
  PostgreSQL or Flyway behavior is part of the uncovered path. This preserves
  the repository's current testing conventions.
- **Keep JaCoCo configuration unchanged:** lowering the threshold or excluding
  production classes would conceal debt and weaken the existing quality gate.
- **Use a red-green verification loop:** capture the baseline report, add a
  focused test slice, run that slice, then run the full suite and `verify`.

## Risks / Trade-offs

- [Risk] Additional tests may be brittle if they assert implementation details
  rather than contracts. -> Mitigation: assert returned values, HTTP envelopes,
  persistence invariants, and documented error codes only.
- [Risk] Testcontainers makes the final verification dependent on a reachable
  container runtime. -> Mitigation: keep unit and MockMvc coverage broad, and
  report infrastructure failure separately from test failure.
- [Risk] Raising coverage can expose unrelated defects in old branches. ->
  Mitigation: fix only regressions revealed by the added assertions; keep
  unrelated behavior changes in separate changes.
