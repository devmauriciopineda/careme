# Coverage and gates — Careme

This document explains what coverage measures here, where the gate is, what it
takes to satisfy it, and which improvement is proposed to the current policy.

The concepts — line and branch coverage, what each one misses — are in
[`../concepts/07-calidad.md`](../concepts/07-calidad.md). This document is about
this project's instrumentation and policy.

## 1. What coverage does and does not say

Coverage is a **structural** metric: it says which parts of the program a
collection of tests executed. It does not say whether what executed was checked
correctly, and it does not say whether the assertions were sufficient. A case that
runs a hundred lines and asserts nothing has the same effect on the number as a
hundred lines asserted one by one.

Two consequences follow, and both are policy here:

- **Coverage is a floor, not a goal.** It is used to find behaviour that no test
  reaches — not as a target to push up.
- **Coverage never replaces an oracle.** "Covered" and "verified" are different
  claims, and only the second one is evidence.

## 2. Aggregate coverage and changed-code coverage

Two ways of measuring the same report, with very different effects:

| | Aggregate | Changed code |
| --- | --- | --- |
| Unit of measurement | The whole module | The lines and branches the change introduces or modifies |
| Answers | "How much of the program has ever been executed?" | "Did this change bring behaviour with no test?" |
| Effect on selection | Forces a complete run to be measurable | Allows a subset, because it only needs the tests that reach the changed lines |
| Failure mode | Stagnation: a large legacy surface depresses the figure, and the response tends to be adding cases that execute lines | A change can be locally well covered while the module as a whole is not |

This project uses the aggregate form. The consequence for the workflow is in
[`regression-scope.md`](./regression-scope.md#6-why-the-closing-run-is-complete).

## 3. Where the gate is

- **Backend.** The build instruments the tests, writes a report, and then checks
  the module against branch and line minimums. The check runs in a phase **after**
  the tests, so a green test run is not a green build. The minimums themselves are
  declared in [`../../backend/pom.xml`](../../backend/pom.xml) and are deliberately
  not repeated here: a threshold written twice drifts.
- **Frontend.** There is no threshold. The working rule is that typing and linting
  pass before the build is considered good, and the commands live in
  [`../../frontend/package.json`](../../frontend/package.json).

Consequences you can plan around:

1. A change can be functionally correct and still fail the build, because it added
   a decision nothing exercised.
2. A quick test run is not blocked by coverage; the complete verification is. Use
   that difference deliberately: the quick run gives feedback, the complete run
   gives the verdict.
3. Because the rule is aggregate, **the only meaningful measurement is the one
   taken over the complete suite.**

## 4. Reading the report

The build writes the coverage report under the backend's `target/` output tree
after a complete run. Read it in this order:

1. the module totals, to see whether the gate passed;
2. the packages the change touched, to see whether the change is the cause;
3. the individual branches not taken, to identify the behaviour each one
   represents.

Step 3 is the only one that produces work.

## 5. Threshold policy

- **A threshold is a floor.** Lowering it to make a build green removes the only
  automated signal the module has, and it is never done inside an unrelated change.
- **A shortfall has an owner.** When the build fails on coverage, the first
  question is whether the state before the change already failed too. If it did,
  the shortfall is measured against the previous state and recorded as its own
  problem; if the change introduced it, the change fixes it.
- **Changing a threshold is a decision with its own change**, justified by a
  measurement of the before-and-after, and reviewed by a person — not by the agent
  that is being blocked by it.

## 6. How to get coverage that is worth something

- Measure, then look for uncovered **branches** rather than uncovered lines: a line
  executed once says nothing about the path not taken.
- For each uncovered branch, name the behaviour it represents, and write a case
  with meaningful input and a concrete oracle.
- Expect roughly these shapes: for a validation rule, the valid value, each
  relevant limit and the errors that change the outcome; for a service, success,
  absence, failure and the effect that must not happen; for an interface, the
  initial state, a valid interaction, an error and a pending operation.
- Re-measure and ask of each new case whether it protects a rule or only executes
  code. If it only executes code, it is not a test.

## 7. Proposed improvement: gate on changed code

**Not implemented.** This is the change that would unlock a smaller closing run.

- **What it means.** The gate would evaluate the lines and branches introduced or
  modified by the change instead of the whole module.
- **Why it is better here.** It attacks the failure mode that matters most in a
  project where an agent writes both implementation and tests: a change can satisfy
  an aggregate floor while bringing new behaviour with no assertion, and it can
  fail the same floor for a reason that has nothing to do with it.
- **What it needs.** The build instruments coverage but does not compute
  changed-code coverage by itself; it needs either a static-analysis service that
  does, or a script that crosses the coverage report with the version-control diff
  and fails when the changed lines fall below a minimum. That script becomes part
  of the build, and therefore part of what has to be maintained.
- **What it unlocks.** Replacing "complete run" with "fixed core plus affected
  tests" in [`regression-scope.md`](./regression-scope.md#5-what-runs-when), which
  is the only form of subsetting that does not leave the build red.
- **What it does not change.** The complete run stays for the delivery row, and the
  aggregate report can still be inspected by hand.

## 8. Decision

The changed-code gate is the accepted direction and the prerequisite for shrinking
the closing run, but it is **not scheduled**. Until it is implemented, the aggregate
gate stands and the closing run stays complete, as described in
[`regression-scope.md`](./regression-scope.md#6-why-the-closing-run-is-complete).
