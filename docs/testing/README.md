# Testing documentation — Careme

These documents are the working agreement for the automated test suite of Careme.
They exist to guide whoever writes the tests — the AI agent working through an
OpenSpec change — and to explain, to any reader, how this project decides what to
test, at which level, and what has to be true before a change is considered
finished.

They do not catalogue what the repository contains today. The suite is described
by running it and by the project READMEs; the values of the quality gates live in
the build files. Nothing here is duplicated on purpose, and anything that would
expire — a count, a list of classes, a version, a threshold — is left out by
design.

## Where each question is answered

| Question | Document |
| --- | --- |
| What does this project promise, and what are we willing to leave untested? | [`test-strategy.md`](./test-strategy.md) |
| Which level carries the evidence, how is a test written here, and what keeps it deterministic? | [`test-process.md`](./test-process.md) |
| What runs when, and how far does the regression reach? | [`regression-scope.md`](./regression-scope.md) |
| What is measured, where is the gate, and what does it take to satisfy it? | [`coverage-and-gates.md`](./coverage-and-gates.md) |
| Why test in levels at all — the underlying concepts | [`../concepts/07-calidad.md`](../concepts/07-calidad.md) (Spanish) |

## If you are the agent picking up a change

1. Read [`test-strategy.md`](./test-strategy.md) once, before the first task.
2. For every task that changes behaviour, use the selection table in
   [`test-process.md`](./test-process.md) to pick the level, write the test inside
   the same task, and record its outcome in the task itself.
3. Before closing the change, run what [`regression-scope.md`](./regression-scope.md)
   requires for the closing state.
4. Treat [`coverage-and-gates.md`](./coverage-and-gates.md) as the definition of
   "the suite is green": a passing test run alone is not it.

## Where the commands and the gate values live

| Need | Source of truth |
| --- | --- |
| Backend commands and what each suite contains | [`../../backend/README.md`](../../backend/README.md) |
| Frontend commands and what each suite contains | [`../../frontend/README.md`](../../frontend/README.md) |
| Whole stack and end-to-end preparation | [`../../README.md`](../../README.md) |
| The backend gate definition | [`../../backend/pom.xml`](../../backend/pom.xml) |
| The frontend scripts | [`../../frontend/package.json`](../../frontend/package.json) |
| The working rules an agent must follow per side | [`../../.github/instructions/`](../../.github/instructions) |

## Keeping these documents true

- They change in the same OpenSpec change that changes the behaviour they
  describe. A strategy that lags behind the suite is worse than no strategy.
- When the suite and these documents disagree, the suite and the build files win;
  the disagreement is a defect to fix here, in that same change.
