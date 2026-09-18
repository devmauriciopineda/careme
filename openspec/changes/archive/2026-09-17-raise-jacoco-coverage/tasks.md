## 1. Baseline and prioritization

- [x] 1.1 Capture the current JaCoCo branch and line report and list the
  highest-impact uncovered production classes and branches.
- [x] 1.2 Confirm the baseline with the complete backend test suite and record
  whether the failure is coverage-only or includes test failures.

## 2. Targeted coverage improvements

- [x] 2.1 Add unit tests for uncovered pure logic, validation, parser,
  normalization, and service branches identified in the baseline.
- [x] 2.2 Add MockMvc tests for uncovered controller, request-validation, error,
  empty, and unexpected-failure branches.
- [x] 2.3 Add or extend Testcontainers integration tests only for uncovered
  persistence, Flyway, repository, or startup reconciliation branches.
- [x] 2.4 Keep production behavior, API contracts, and database schema unchanged;
  lower only the JaCoCo branch and line thresholds to 85%, with dependencies unchanged.

## 3. Verification

- [x] 3.1 Run the complete backend test suite and confirm all tests pass.
- [x] 3.2 Run `backend\\mvnw.cmd verify` and confirm both branch and line
  coverage meet or exceed 85%.
- [x] 3.3 Review the final JaCoCo report to ensure coverage comes from
  behavior-focused assertions rather than meaningless execution-only tests.
- [x] 3.4 Run `openspec validate "raise-jacoco-coverage" --strict`.