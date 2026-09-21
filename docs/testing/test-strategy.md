# Test strategy — Careme

This is the strategy for the automated test suite of Careme: what it protects, how
it is shaped, and which decisions are settled in advance so that neither a person
nor an agent has to re-derive them in every change.

It is a strategy, not a plan. It does not say what is tested in a specific change
(the change's `tasks.md` says that), nor how to run the suite (the project
`README.md` files say that), nor how to design a test case (the concepts live in
[`../concepts/07-calidad.md`](../concepts/07-calidad.md)). It says *why* the suite
is shaped the way it is and *where the line is drawn*.

## 1. Context and constraints

Careme is an early MVP: a full-stack personal clinical assistant with no
authentication, whose assistant records patient-stated clinical facts and never
diagnoses or recommends treatment. Five constraints shape everything below.

- **The assistant records, it does not judge.** No test asserts a diagnosis or a
  recommendation, and no test may be written to require one.
- **There is no manual testing.** Everything the suite does not assert is, by
  definition, unverified. Nothing is "checked by hand" as a complement.
- **There is no independent author.** The tests are written by the same AI agent
  that implements the change, inside the same task, within the same spec-driven
  cycle. There is no second party producing the oracle.
- **There is no CI pipeline.** The verification run is executed locally as part of
  the change cycle, and its outcome is recorded in the change itself. The evidence
  is the change's task list, not a report service.
- **The suite must run offline.** The application's default assistant is the real
  provider; the test configuration selects the simulated one so that the whole
  fast and integration tiers run with no network access.

## 2. What the suite protects

The suite exists to protect a small number of properties. They are stated as
properties and not as tests, because tests change and properties should not.

| Property | Why it matters |
| --- | --- |
| The API contract at the boundary — envelope, status codes, error codes | The frontend is a consumer with no other contract document |
| Reading never writes | A question about the clinical history or the tracking must leave both untouched; this is the system's central promise |
| Absence is declared, never invented | A miss and a failure are different outcomes, and presenting one as the other misleads the person |
| The Markdown documents are the source of truth; the search index is derived and rebuildable | Losing that asymmetry turns a rebuildable cache into unverifiable data |
| A consultation collects notes and registers them only when it closes | Nothing half-registered is a fact |
| Values, units and dates shown are the stored ones | No silent rounding, conversion or reinterpretation |
| The user-visible states are distinguishable — answered, absence, duplicate, failure, clarification | The person must be able to tell "no consta" from "no pude consultarlo" |
| The automated run is deterministic and offline | A suite that depends on the network or on the clock stops being evidence |

## 3. Levels and selection rule

Each level trades speed for realism. The rule is: **the evidence is carried by the
lowest level at which the property is observable**, and a higher level never
replaces the evidence of a lower one — it only adds realism. An end-to-end test
can show that a flow works; it cannot tell you whether the form, the query or the
schema is what broke.

| Surface the change touches | Level that must carry the evidence |
| --- | --- |
| A pure rule, a validator, a date or number helper, a derivation | Unit (backend) or unit (frontend) |
| A service that composes others: mapping, ordering, outcome selection | Unit with doubles; add integration as soon as it reads or writes persisted state |
| An endpoint's shape, request validation or error mapping | Web layer on the backend (`@WebMvcTest` + MockMvc) |
| Persistence: entity mapping, SQL, full-text search, schema, a migration | Integration against the real engine |
| A React island: form, interaction, state, accessible output | Component |
| A payload or form schema at the boundary | Unit |
| The conversation end to end: message → operations → outcome → stored facts | End-to-end, **plus** the level that the specific behaviour sits at |
| The prompt or the real provider adapter | The simulated composer's contract, **plus** the corpus measurement |
| Documentation only | No test; but the change that alters behaviour updates the documents in the same change |

The mechanics of each level — what it actually runs, what it asserts, what it must
not be used for — are in [`test-process.md`](./test-process.md).

## 4. Execution tiers

Levels group into tiers by what they need to run. The tiers, and when each one
runs, are the subject of [`regression-scope.md`](./regression-scope.md).

| Tier | Needs | Cost shape |
| --- | --- | --- |
| Fast | Nothing beyond each side's runtime | Seconds; paid on every task |
| Integration | A container runtime and a real PostgreSQL engine | Dominated by container start and schema migration |
| End-to-end | The whole stack, plus the real assistant | Dominated by stack preparation |
| Corpus | The same as end-to-end | A measurement over a prepared dataset, not a pass/fail gate |

## 5. Quality gates

- **The backend is gated; the frontend is not.** The backend build fails when the
  module's coverage falls below the branch and line minimums declared in
  [`../../backend/pom.xml`](../../backend/pom.xml). The values live there and are
  deliberately not repeated here.
- **The gate is evaluated after the tests, over the whole module.** Passing tests
  is therefore necessary but not sufficient: a change can be functionally correct
  and still fail the build because it added paths nothing executed.
- **A gate cannot be satisfied by a subset.** Because the rule is aggregate, the
  only measurement that means anything is the one taken over the complete suite.
  This is the constraint that fixes the scope of the closing run.
- **The frontend has no threshold, but typing and linting must pass** before the
  build is considered good.
- **A coverage threshold is a floor, not a target.** An uncovered branch that
  represents behaviour is a missing case, not a number to be pushed up with cases
  that only execute lines.
- **Changing a threshold is a decision with its own change and its own
  measurement.** A shortfall that predates the change is measured against the
  state before the change and, when it is not caused by it, recorded as its own
  problem rather than lowered silently.

Details, and the improvement proposed to this policy, are in
[`coverage-and-gates.md`](./coverage-and-gates.md).

## 6. Determinism and intermittent tests

An intermittent test — one that passes and fails on the same code — destroys the
only quality signal this project has, because there is no manual testing to fall
back on.

- **No network** from the fast and integration tiers. The real assistant belongs
  to the end-to-end tier and to the corpus, and nowhere else.
- **Time is fixed** whenever a behaviour depends on it.
- **A test never depends on another test**, on execution order, or on state left
  behind by a previous run.
- **Zero tolerance.** An intermittent test is fixed inside the same change. It
  cannot be quarantined, postponed or retried into green, because the spec-driven
  workflow has no state in which a task stays pending — a task is either complete
  or it is not. Prevention is therefore paid up front: the rules in
  [`test-process.md`](./test-process.md) are mandatory, not advisory.

## 7. Regression policy

Regression here means protection against unintended change, not re-verification of
everything. Its scope is chosen by tier and by the surface the change touches —
never by what is convenient to run.

The definition of the fixed core, the impact mapping and what runs when are in
[`regression-scope.md`](./regression-scope.md).

## 8. Ownership and exceptions

Because the suite is written by an agent, the points where judgement is required
are made explicit and reserved.

- **Who writes.** The agent implementing the change writes its tests, in the same
  task, at the level the task declares. A task is complete only when those tests
  exist, pass, and their outcome is recorded in the task.
- **Who reviews.** The person driving the cycle reads the recorded evidence — the
  task's declared level and outcome — because there is no test report to trust and
  no manual pass to fall back on.
- **Who approves an exception.** Only a person may approve an exception to a
  quality gate, a reduction of the regression scope, or the acceptance of a new
  risk. The agent does not approve its own exception, and an exception is never a
  deferral: it is either applied now, with its consequence recorded, or the change
  does not close.
- **What an exception costs.** An approved exception is written into section 9 of
  this document, or into the document that owns the affected area, with the
  property it leaves unprotected. An undocumented exception is a defect.

## 9. Accepted risks (deliberate omissions)

These are decisions, not oversights. Each one is accepted with its consequence.

| Not done | Consequence accepted |
| --- | --- |
| Manual and exploratory testing | Anything nobody thought to assert is unverified; the corpus measurement is the only exploratory instrument that exists |
| Accessibility testing with assistive technology | The suite checks accessible names and roles, not the experience of using a screen reader |
| Visual regression | A layout or style break that no assertion covers reaches the person |
| Performance, load and stress testing | Latency and capacity under concurrent use are unknown |
| Security testing beyond input validation | No authentication exists by design, but injection, exposure and dependency risk are not systematically probed |
| Independent authorship of tests | The same agent writes the implementation and its oracle; agreement between them is not evidence of correctness, which is why spec traceability matters |
| A continuous integration pipeline | Verification depends on someone running it and recording the result; a change that was never run is indistinguishable from a green one |
| Reading the real assistant in the fast and integration tiers | The prompt's behaviour against the real model is only observed in the end-to-end tier and the corpus |

One deliberate asymmetry is worth naming: a corpus run may stay red. When it finds
a real defect, the finding is recorded and the measurement is left failing rather
than relaxed so that it passes — the instrument is only useful while it is allowed
to disagree.

## 10. Metrics and reporting

- **What is looked at:** whether the change's suites pass; the result of the
  backend gate; whether any intermittent test was observed (the answer must be
  no); and the findings the corpus recorded.
- **What is deliberately not used as a target:** coverage as a goal, test count as
  progress, and any dashboard. All three reward execution over assertions.
- **Where the evidence lives:** in the change's task list, as the level applied and
  the outcome recorded. There is no separate report artifact.

## 11. Proposed improvements

None of these is implemented today. They are recorded with their prerequisite so
that adopting one is a decision and not a drift.

| Improvement | Why | What it needs first |
| --- | --- | --- |
| Gate on changed code instead of the whole module | It is the prerequisite for shrinking the closing run: an aggregate gate cannot be satisfied by a subset | Tooling that computes coverage of the modified lines; see [`coverage-and-gates.md`](./coverage-and-gates.md) |
| A recorded runtime baseline per tier | Turns the cost discussion into arithmetic instead of impressions | One measured run recorded in this document |
| Pin the fixed core to named suites | Today the core is defined by property; naming the suites that carry it makes the impact mapping mechanical | The property-to-suite mapping reviewed once |
| Contract tests between frontend and backend | Today the boundary is pinned from the backend side only | A shared contract artifact both sides can read |
| Run the corpus on a schedule instead of only when prompted | It is the only instrument that can find what nobody thought to assert | A reason to run it that is not a change |

## 12. Review triggers

Revisit this document when any of these happens:

- a new level, tool or runner is introduced;
- the mode of the assistant changes for any tier;
- a quality gate or its scope changes;
- an incident reveals a property that was not protected;
- a new regulatory or contractual requirement applies.

## 13. References

- [`./test-process.md`](./test-process.md) — how a test is written here.
- [`./regression-scope.md`](./regression-scope.md) — what runs when.
- [`./coverage-and-gates.md`](./coverage-and-gates.md) — coverage and the gate.
- [`../concepts/07-calidad.md`](../concepts/07-calidad.md) — the concepts behind the levels.
- [`../../backend/README.md`](../../backend/README.md), [`../../frontend/README.md`](../../frontend/README.md) — commands and suite contents.
- [`../../backend/pom.xml`](../../backend/pom.xml) — the gate definition.
