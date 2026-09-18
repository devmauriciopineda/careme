## Why

The backend's repository-wide JaCoCo gate currently reports insufficient branch coverage
against the configured 85% minimum, so `mvn verify` fails even though the test
suite passes. This change isolates the existing coverage debt from functional
changes such as UC-011 and makes the quality gate pass through targeted tests.

## What Changes

- Add focused unit, controller, service, and integration tests for uncovered
  existing backend branches identified by the JaCoCo report.
- Preserve all existing runtime behavior, API contracts, and persistence behavior.
- Set the repository-wide branch and line coverage minimums to 85%.
- Verify that the full backend test suite and `mvn verify` pass at the configured
  85% branch and line coverage minimums.

## Capabilities

### New Capabilities

None. This is a test-coverage and quality-gate change with no user-visible or
API behavior change.

### Modified Capabilities

None.

## Impact

- Backend test sources under `backend/src/test/java/`.
- JaCoCo reports under `backend/target/` during verification only.
- No production API, database schema, frontend behavior, dependency, or
  runtime configuration changes are expected.