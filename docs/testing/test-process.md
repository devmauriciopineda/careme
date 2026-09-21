# Test process — Careme

This document describes **how a test is written and run in this project**: which
level applies, what each level actually executes, the assertion and isolation
rules, and the conventions that make the regression scope computable.

The theory — levels, oracles, equivalence partitioning, doubles, coverage — is in
[`../concepts/07-calidad.md`](../concepts/07-calidad.md). This document does not
repeat it; it applies it.

## 1. When a test is written

Tests are not a later phase. They are part of the task that changes the behaviour:

1. The task in the change's `tasks.md` declares the behaviour **and** the level
   that will carry its evidence.
2. The test is written with the implementation, in the same task.
3. The task records the level applied and the outcome observed.
4. A task is marked complete only when its declared tests exist and pass. Partial
   coverage of a task is not a complete task, and the workflow has no state for a
   deferred one.

## 2. What each level actually runs

Choosing the wrong runner produces evidence for the wrong question. This is what
each level executes here.

| Level | Runner | What it actually executes |
| --- | --- | --- |
| Unit — backend | JUnit 5 with AssertJ and Mockito | Only the JVM. No Spring context, no container. Collaborators are replaced by doubles |
| Unit — frontend | Vitest | Only Node. No DOM, no backend |
| Web layer | Spring Boot Test with `@WebMvcTest` and MockMvc | The web slice only — routing, JSON conversion, validation, error mapping — with the service replaced. The request never opens a socket |
| Integration | Spring Boot Test with Testcontainers | A real PostgreSQL engine in a container, with Flyway applying the real migrations. One container is started per JVM and reused, so each case cleans its own state before starting |
| Component | Vitest with Testing Library, `jsdom` and `userEvent` | A simulated DOM in Node: the component is rendered and the interaction is reproduced as a sequence of real events |
| End-to-end | Playwright | A real browser against a running stack, with the real assistant. The oracle is external to the system: the Markdown documents that the run reads from disk, compared before and after the turn |
| Corpus | Playwright used as an API client | The same engine without a browser, driving the API and aggregating a scoreboard instead of asserting case by case |

Rules that follow from the table:

- **Do not assert what the level cannot show.** A web-layer test cannot prove a
  repository works; an integration test cannot prove the outer envelope.
- **Do not climb a level to avoid a double.** If a behaviour is observable in a
  unit, the container adds cost and no evidence.
- **Do not descend a level to save time.** A query's correctness is not visible in
  a mocked repository.

## 3. Assertion rules

- One behaviour per case, and the name describes the behaviour, not the method.
- Assert the **observable result** — a value, an exception, a status code, a
  document's content — and, where it matters, the **forbidden effect**: that a read
  did not write, that nothing was attached to an answer that should carry no facts.
- Never assert only that a collaborator was called. A call is not a behaviour.
- User-facing assertions use the Spanish strings the interface actually renders.
- Interface assertions go through accessible queries — role, label, text — not
  through internal structure, ids or implementation details.
- Do not assert on the framework: that React rendered, that Spring wired a bean, or
  that the runner ran.

## 4. Data, isolation and environment

- **Each case owns its state.** Integration cases clean the tables they use before
  they start; nothing depends on what a previous case left behind.
- **The clinical history the end-to-end tier asserts on is read from disk**, through
  the directory the stack is pointed at, and is expected to start empty.
- **The simulated assistant is selected explicitly** for automatic runs, so no tier
  below end-to-end can reach the network.
- **A missing environment precondition is a failure, not a skip.** A test that
  silently passes when the infrastructure is absent is worse than no test.

## 5. Determinism rules (mandatory)

These rules exist because an intermittent test is not tolerated, so prevention is
the only available strategy.

1. **No network** outside the end-to-end tier and the corpus.
2. **Fix the clock and the reference date** in anything that reasons about "today",
   "yesterday" or a relative expression. Never read the real clock in a test.
3. **Never depend on execution order**, on a shared mutable buffer, or on a value
   produced by another case.
4. **Never depend on parallelism**, worker count, or which worker picked a file.
5. **Clean up what you create**, including files written outside the database.
6. **Retries are not a determinism strategy.** If a retry is used anywhere, the
   count is recorded; a retry that hides a real defect is treated as a defect.

## 6. Naming and mapping

The regression scope is computable only if tests can be traced back to what they
protect. Three conventions make that possible, and they are not cosmetic:

- **The test is named after its subject.** A backend test class named after the
  class under test, a frontend test file next to the module it covers.
- **The end-to-end spec is named after the capability or use case** it walks, so a
  change can be mapped to the spec that has to run.
- **The corpus stays separate** from the specs, because it measures and does not
  assert pass-or-fail per case.

## 7. Recording evidence in the change

- Each task states the level it applied and the outcome it observed.
- The task that closes the change states the commands it ran and what each
  returned, including the gate result.
- A finding that is out of the change's scope is recorded with its measurement and
  left as it is — not softened so that the change can close.

## 8. What not to do

- Adding a case that only executes lines to raise a number.
- Duplicating at a higher level what a lower level already proves.
- Asserting on the clock, the network, the order or the worker.
- Leaving a test disabled, skipped or quarantined: there is no state for it in the
  workflow, and an unrecorded exception is a defect.
- Writing a test whose only expectation is that the code ran.

## 9. Natural-language answers

An answer written in natural language cannot be compared against an expected
sentence: two correct answers to the same question do not share their words, and the
sentence comes from a model that varies between runs. The response here is not to
assert the prose more cleverly, but to keep it out of reach of the assertions and to
measure what is left.

The model's reply is a **contract, not a text**. The prompt requires a fixed shape
carrying a *coverage verdict* — how much of the question the retrieved events support:
the whole of it, part of it, or none of it — the answer itself, and the codes of the
events the answer relies on (requirement *Compose grounded answers from retrieved
events*, `openspec/specs/llm-clinical-intent-adapter/spec.md`). No test asserts what
the answer says; the tests assert the verdict, the cited codes, and whether the
response is acceptable at all.

| Where the prose lives | How it is treated |
| --- | --- |
| The adapter that talks to the provider | Asserted against a local HTTP server that returns canned payloads in the provider's envelope, so the model never runs. The cases that matter are the rejections: a response that cannot be read, a missing or unknown verdict, an empty retrieved set, and a code outside the retrieved set (requirement *Validate provider responses before side effects*, same spec) |
| The composer | A real offline implementation whose verdict is configurable, so the whole question, part of it and none of it are all reachable with no network call. A behaviour that depends on the verdict is not tested unless it is reachable this way |
| The paths where an invention would be harmful | When the retrieved events do not answer the question, the composer's wording is **discarded** and the application declares the absence, with the reason resolved from counts rather than from the model. When the events support only part of the question, the application keeps the supported answer and appends its own declaration of the unsupported part — a declaration that can only withdraw coverage and never introduces a fact (`openspec/specs/clinical-history-query/spec.md`, requirement *Informar fallos sin inventar una respuesta*; `openspec/specs/assistant-chat-interface/spec.md`) |
| What is retrieved | Measured over a prepared corpus instead of asserted case by case: the state of the turn, whether every cited code corresponds to a document that exists, whether a question about something not recorded produces no affirmation, and whether a query writes nothing — see [`../concepts/07-calidad.md`](../concepts/07-calidad.md) §4.6 |
| A behaviour that depends on the prompt's wording | Not a test. A recorded risk with the measurement that supports it — usually a single sample — and a deterministic guard in the application as the planned response to a regression |

Rules:

- **Never compare an answer against an expected sentence**, and never grade answers
  with another model or by similarity: both replace a false negative with a false
  sense of verification.
- **Never reach the provider from an automated tier.** The simulated composer is what
  produces the verdict, and the verdict is what every level above it consumes.
- **A lower temperature is not a determinism strategy**, and it does not turn this
  into a comparable-output problem.
- **Every sentence a person reads when the answer cannot be verified is application
  code**, and it is asserted like any other string.
- **A behaviour observable only with the real model is measured**, and its
  measurement may stay red on purpose as a recorded finding rather than relaxed to
  pass.
