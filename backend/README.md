# Careme — Backend

## Project description

Spring Boot REST service for the Careme personal clinical assistant and body-tracking
application. It exposes the chat flow and the measurements that the frontend renders.

It exists as its own service so the UI does not own the data. Measurements are
stored in PostgreSQL, one measurement per metric and day, and the persistence layer
is isolated behind an interface so the storage engine can change without touching
the HTTP contract.

## General functionality

- `GET /api/v1/measurements` returns every measurement ordered from the oldest to
  the most recent date.
- `POST /api/v1/measurements` registers the legacy pair of a day —weight and
  abdominal circumference—. On the metric model behind it, re-registering that
  metric and day replaces its value instead of adding a row.
- Request validation rejects a future date, a missing or non-positive metric, a
  value above the allowed limit and any value with more than one decimal.
- One response envelope for every outcome, success or failure, plus a global
  exception handler that maps invalid payloads to `400` and unexpected failures
  to `500`.
- Measurements live in a PostgreSQL table. Flyway owns the schema; Hibernate only
  validates against it.
- CORS is configured explicitly and scoped to known origins.
- `POST /api/v1/chat/messages` receives Spanish natural-language messages and returns
  a non-streaming structured outcome. The default `CAREME_LLM_MODE=openai` uses
  DeepSeek's OpenAI-compatible API with the backend-only `CAREME_LLM_API_KEY`
  credential, so the backend refuses to start without it; `CAREME_LLM_MODE=fake`
  selects the simulated assistant for local development and tests, with no network call.
- The chat can answer questions about the clinical history from the derived
  `clinical_event_index`, returning `answered` with supporting events or
  `no_records` when no registered event supports the question. A `no_records`
  response names the scope of the absence (`empty_history`, `no_events_of_type`,
  `no_events_in_period` or `no_term_match`) and the actions offered to the user.
  Queries never modify the Markdown history or its index.
- A message that neither registers an event nor depends on the clinical history is
  answered as `general_conversation`. That reply is composed from the message and
  the recent turns of the active conversation only: this path does not read the
  clinical history, does not cite any event and never presents anything as a
  recorded fact. A request for a diagnosis, a recommendation or an interpretation
  of the user's own case is declined in the reply, which states that the assistant
  does not diagnose and does not recommend treatment.
- **A turn never writes to the clinical history.** What a message mentions is
  collected as a *note* of the open consultation and reported as `noted` —the
  clinical fact, as a clinical-fact note, and the measurement, as a measurement
  note—; the consultation is persisted from the moment it opens, so an
  interruption keeps what the person narrated. A consultation holds at most one
  open consultation per conversation: opening a new conversation closes the
  previous one. The admissible notes of each channel are registered only when the
  consultation is closed with
  `POST /api/v1/chat/conversations/{conversationId}/consultation/close`: the facts
  as clinical events with their provenance, the measurements in the metric
  tracking.
- Consultations are Markdown files under `data/encounters/`, configurable with
  `careme.encounters.directory`, with a derived, rebuildable PostgreSQL index.
- Clinical events are Markdown files under `data/events/`, configurable with
  `careme.events.directory`; PostgreSQL stores only the derived index. The index is
  rebuilt at startup and with `POST /api/v1/clinical-events/reindex`.

## Architecture overview

```text
Controller   MeasurementController      maps HTTP ↔ DTOs, no business logic
    │
Service      MeasurementService         sorts by date, maps view → DTO
    │
Repository   MeasurementRepository         data access contract (legacy view)
             MetricBackedMeasurement…      composes the daily view over the model
             MetricMeasurementJpaDao       Spring Data JPA interface
    │
Domain       MetricMeasurement / Metric   domain records of the metric model
             MetricMeasurementEntity       JPA mapping of the measurements table
             Measurement (record)          legacy daily view, identity + invariants
```

- **Request flow**: controller → service → `MetricBackedMeasurementRepository` →
  `MetricMeasurementJpaDao` → PostgreSQL. The repository composes the legacy daily
  view over the metric model: `GET` pairs the `weight` and `waist` measurements of
  each day, and `POST` upserts those two metrics of that day. The service sorts
  ascending by date and maps each view row to a `MeasurementResponse`, which the
  controller wraps in `ApiResponse`.
- **Failure flow**: an invalid payload surfaces through `ApiExceptionHandler` as
  HTTP 400 — `VALIDATION_ERROR` for a field that breaks a rule, `INVALID_REQUEST`
  for a body that cannot be read — and an unexpected failure as HTTP 500. There
  is no 404 path, because no resource can go missing.
- **Invariants** live in the metric model's domain records (`MetricMeasurement`,
  `Metric`), so an invalid measurement cannot be constructed: every value is
  positive and inside its component's admissible range, and a composite metric
  carries all of its components. The table mirrors them with `CHECK` constraints
  and the uniqueness of `(metric, date)`. The rules that only apply to the legacy
  registration — date not in the future, limits of 500 kg and 400 cm, a single
  decimal — are enforced by Bean Validation on `MeasurementRequest` before the
  service runs.
- **The schema is not generated by Hibernate**: `spring.jpa.hibernate.ddl-auto`
  is `validate` and `db/migration/` is the single source of truth. A drift between
  the mapping and the migration fails at startup instead of silently altering the
  database.
- **Three types, three jobs**: `MetricMeasurementEntity` (table mapping),
  `MetricMeasurement` (domain, no annotations) and `MeasurementResponse` (HTTP
  contract), so a schema change does not move the JSON contract.
- **Every request hits the database.** There is no cache, so a row inserted
  outside the application is visible on the next request.

## Technology stack

- Java 21 (LTS) and Spring Boot 3.5 (Spring Web MVC, Bean Validation, Spring Data
  JPA)
- PostgreSQL 17, Hibernate ORM 6 and Flyway for schema migrations
- Jackson for JSON binding, SLF4J + Logback for logging
- Maven with the wrapper committed (Maven 3.9.9, `only-script` distribution) and
  `maven-enforcer-plugin` requiring JDK 21
- JUnit 5, Mockito, AssertJ, MockMvc, Spring Boot Test and Testcontainers
  (`postgres:17-alpine`) for the integration tests
- JaCoCo with a gate of 85% branches and 85% lines, bound to `verify`
- Docker multi-stage image (`maven:3.9-eclipse-temurin-21` →
  `eclipse-temurin:21-jre-alpine`, non-root user)

## Use of APIs or external services

- It can consume DeepSeek's OpenAI-compatible LLM API when explicitly enabled.
- Local real-provider configuration belongs in the root `.env` file, which is ignored
  by git. Copy the root `.env.example`, set `CAREME_LLM_MODE=openai`, and add the
  manually provisioned `CAREME_LLM_API_KEY`.
- Defaults are `CAREME_LLM_BASE_URL=https://api.deepseek.com` and
  `CAREME_LLM_MODEL=deepseek-flash`. The client calls `/chat/completions` and uses
  configurable connect/read timeouts.
- Chat messages are limited to 4,000 characters. In-memory conversation state is
  limited to 1,000 conversations and expires after 2 hours by default; recent
  turns used for follow-up questions remain bounded and are not persisted. The
  maximum number of history-query results is configured by
  `careme.chat.query.max-results`. The MVP has
  no application-level rate limiter; provider account limits still apply.
- In the fake mode, `careme.llm.fake.coverage` (`full`, `partial` or `none`) and
  `careme.llm.fake.unsupported-part` let a local run exercise a partial or an
  unanswered composition without a provider.
- The application does not select or guarantee a provider processing region or
  retention policy. Treat those as deployment approval requirements before sending
  real clinical data. Conversation turns are not persisted by Careme, but provider
  handling follows the selected provider account and policy.
- The assistant records patient-stated facts only. It must not diagnose, infer
  conditions, recommend treatment, or turn suspicions into clinical events.
- Logs retain conversation/message identifiers for correlation, but do not log the
  clinical message, prompt, provider response, API key, or stack trace for a normal
  provider failure.
- It **exposes** one HTTP API, documented below.
- It **stores data in PostgreSQL** and owns the schema through Flyway migrations.
- There is **no authentication or authorization**.
- No message broker, cache server or cloud service is used.

### `POST /api/v1/chat/messages`

Accepts `{ "message": "...", "messageId": "...", "conversationId": "..." }`.
`conversationId` is optional on the first turn: when it is absent the backend
creates one and opens the consultation for that conversation. The response contains
a generated conversation id, the message id, a status (`noted`, `registered`,
`nothing_to_register`, `answered`, `no_records`, `clarification_required`,
`general_conversation`, `duplicate` or `failed`) and a Spanish user-facing message.
A turn reports `noted` when it collected one or more clinical-fact or measurement
notes: the facts are **not** part of the clinical history yet and the measurements
are **not** in the tracking yet, and `message` says the note will be registered
when the consultation is closed. `registered` only appears in the
close outcome, never in a message turn. An `answered` response includes the clinical
events it is grounded in, in `events`, and the measurements it is grounded in, in
`measurements` —one or the other, never mixed: a measurement is its own record,
with its reference unit and its exact date. `no_records` includes neither, and
instead reports:

| Field              | Present when  | Value                                                                                  |
| ------------------ | ------------- | -------------------------------------------------------------------------------------- |
| `absenceReason`    | `no_records`  | `empty_history`, `no_events_of_type`, `no_events_in_period`, `no_term_match`, `no_measurements`, `no_measurements_in_period` or `metric_not_tracked`, or `null` |
| `suggestedActions` | `no_records`  | `reformulate` and/or `register`; empty otherwise                                        |
| `operations`       | always        | the operations the turn went through, in order, each with its own `status`, `events`, `absenceReason`, `suggestedActions` and `measurements` |
| `measurements`     | `answered` from the tracking | the measurements that support the answer, each with its `metric`, `label`, `unit`, `date`, `values` and the whole measurement written out in `text` |

`operations` is additive and optional, so a client that does not know it keeps
working. `message` is the single answer the turn produced, and `operations` lets a
client tell what was completed from what was not. A `general_conversation`
response carries the conversational reply in `message`, includes no events and no
absence reason, and is declined from the clinical history: the turn never reads or
cites it. When the turn completed more than one operation, the status is
`answered` if it produced an answer, and `failed` if any operation did not
complete — even when another operation of the same turn did. A search that cannot be completed is reported as
`failed`, never as `no_records`, so an absence is only ever declared after it was
verified. When the retrieved events support only part of a question, the answer is
`answered`: it answers the supported part and its Spanish message declares the part
that has no records.
Conversation state is in memory and is not persisted; clinical facts survive as notes
of the persisted consultation, and registered events survive because their Markdown
documents are the source of truth.

### `POST /api/v1/chat/conversations/{conversationId}/consultation/close`

Ends the consultation of a conversation and returns the same outcome envelope as a
message turn. The close registers the admissible notes of both channels: the facts
as clinical events with their provenance, and the measurements in the tracking. It
keeps the consultation's derived summary and never exposes the consultation's
identifier, summary or motive through the API. The operation is idempotent for the
same consultation: repeating it returns the outcome it already produced and
registers nothing new. Closing a consultation that collected neither facts nor
measurements reports `nothing_to_register` and leaves the clinical history and the
tracking exactly as they were. A close that cannot complete reports `failed` and
leaves the consultation open, so it can be retried.

The close registers **both channels** of what the consultation collected:

- The clinical facts, as events with their provenance, exactly as before.
- The measurements, in the tracking, each in its metric's reference unit and with the
  consultation as its provenance. The batch is one write: either every admissible
  measurement of the consultation is stored or the tracking stays exactly as it was, and
  a metric and day that already had one gets its value replaced.
- The confirmation names what was registered in each channel, so the person can tell what
  was registered from what was not. Because the tracking is written first, a close that
  cannot store the batch registers nothing at all and stays open; a later failure while
  registering the facts does not revert the measurements already stored, and is reported
  as such.

## API

### `GET /api/v1/measurements`

Returns every measurement, oldest first.

**Success — `200 OK`**

```json
{
  "success": true,
  "data": [
    {
      "id": "0a3b5ce7-fb23-4810-b588-e2f13de310a6",
      "date": "2026-07-18",
      "weightKg": 84.5,
      "waistCm": 98.4
    }
  ],
  "messageCode": "SUCCESS",
  "message": "Operation completed successfully"
}
```

An empty dataset is a successful response with `"data": []`.

**Errors** follow the same envelope with `success: false`:

```json
{
  "success": false,
  "error": {
    "message": "Unexpected server error",
    "code": "INTERNAL_ERROR",
    "details": []
  }
}
```

`500 INTERNAL_ERROR` for unexpected failures, including the database being
unreachable.

### `POST /api/v1/measurements`

Registers the measurement of a day. The day is the identity of the record: if it
already had a measurement, its values are replaced; otherwise a new one is
created. There is never more than one measurement per day.

**Request**

```json
{
  "date": "2026-09-10",
  "weightKg": 80.1,
  "waistCm": 94.8
}
```

`date` is an ISO `yyyy-MM-dd` calendar day that cannot be in the future.
`weightKg` must be positive, at most `500` and have at most one decimal;
`waistCm` must be positive, at most `400` and have at most one decimal.

**Success — `201 Created`**

```json
{
  "success": true,
  "data": {
    "id": "0a3b5ce7-fb23-4810-b588-e2f13de310a6",
    "date": "2026-09-10",
    "weightKg": 80.1,
    "waistCm": 94.8
  },
  "messageCode": "SUCCESS",
  "message": "Operation completed successfully"
}
```

**Errors**

- `400 VALIDATION_ERROR` when a field breaks a rule; `details` lists the offending
  fields.

### Measurement tracking and its metric model

`/api/v1/measurements` is a **derived view**, not a table. The tracking itself is the metric
model: a measurement is one metric on one exact day, with its values in the metric's reference
unit and the consultation it came from as its provenance. `GET` composes the weight and the
abdominal circumference of each day; `POST` stores those two metrics for that day. The contract
above is unchanged for the client.

- **Metric catalogue.** A metric is described by its reference unit, its components, its
  admissible range, its exact unit equivalences and, when it has one, the unit a value without
  an explicit unit resolves to. The catalogue ships weight (kg), abdominal circumference (cm),
  blood pressure (mmHg, composite: one measurement with systolic and diastolic) and cholesterol
  (mg/dL), and knows creatinine, fasting glucose and triglycerides without admitting them yet.
- **Admitted metrics** live in `admitted_metrics`. A metric the person confirms starts to be
  recorded by being admitted there; a metric the catalogue does not know cannot be admitted,
  because its unit and its range would have to be invented.
- **Unit conversion** happens when the measurement is collected: a value stated in another unit
  of the same metric is converted to the reference unit with the catalogue's single equivalence,
  and only then stored.
- **One measurement per metric and day** (`uq_measurements_metric_date`). Registering that metric
  and day again replaces the value and says so; it never creates a second measurement.
- **A measurement is not a clinical fact.** `measurement` is retired from the clinical-event types
  and a message that mentions a measurement is attended by the measurement channel (`record_measurement`
  in the chat contract). Registrations happen at the close of the consultation, and a document
  written before the change with `type: measurement` is still readable and never rewritten.
- **The tracking is read from the conversation too.** `consult_measurements` answers a question about
  the measurements with their values, their reference unit and their exact date, bounded by the metric
  and the period the question names. It is read-only: asking never changes a measurement. It never
  assumes a metric the question does not name —it asks which one—, it declares a metric outside the
  tracking instead of answering with another metric's values, and it declares an absence only after a
  retrieval completed. The values reach the person exactly as the tracking stores them: the application
  formats them against the catalogue's reference unit, and a composed answer that does not reproduce
  them verbatim is replaced by the application's own wording.

> **Known gap.** The legacy contract requires `weightKg` and `waistCm` together, so a day that
> carries only one of the two metrics —a weight mentioned in the conversation, for example— stays
> whole in the tracking but is not shown by this endpoint. Closing that gap is the sibling change
> `metric-tracking-view`, which brings the per-metric read contract and the per-metric form.

- `400 INVALID_REQUEST` when the body is not valid JSON or a value has the wrong
  type.
- `500 INTERNAL_ERROR` for unexpected failures.

Notes on the payload: `id` is the row's UUID rendered as a string, `date` is an ISO
`yyyy-MM-dd` calendar date and both metrics are decimals with one decimal place.

### `POST /api/v1/chat/messages`

Receives a Spanish natural-language message and returns a non-streaming
structured outcome. See *Use of APIs or external services* above for the request
shape, the statuses and the LLM configuration.

### `POST /api/v1/chat/conversations/{conversationId}/consultation/close`

Ends the consultation in progress and returns the close outcome: the registered
facts with their provenance, or that there were no facts to register. Every note
the consultation collected had already passed the same deterministic checks when
it was taken, so the close registers all of them. It is idempotent and registers
nothing when the consultation is already closed.

### `POST /api/v1/clinical-events/reindex`

Rebuilds the derived clinical-event index from the Markdown documents in
`data/events/` and returns the number of indexed events.

**Success — `200 OK`**

```json
{ "indexedEvents": 12 }
```

### `GET /api/v1/clinical-events`

Lists the persisted clinical events without changing Markdown or the derived
index. The default order is `recordDate` descending, meaning the most recently
saved event appears first. Use `sort=occurrenceDate` for occurrence-date order;
events without an occurrence date are last in that order.

Optional query parameters are `type` (`diagnosis`, `medication` or `note`),
inclusive `from` and `to` occurrence dates in `yyyy-MM-dd`, and
`sort` (`recordDate` or `occurrenceDate`).

**Success — `200 OK`**

```json
{
  "success": true,
  "data": [
    {
      "id": "0a3b5ce7-fb23-4810-b588-e2f13de310a6",
      "code": "evt_012",
      "type": "note",
      "content": "Dolor de cabeza",
      "occurrenceDate": "2026-09-01",
      "occurrenceDatePrecision": "approximate",
      "recordDate": "2026-09-17"
    }
  ],
  "messageCode": "SUCCESS",
  "message": "Operation completed successfully"
}
```

An empty history or a filter with no matches is successful with `"data": []`.
Relative expressions stored with an event are not returned; their normalized
occurrence date is marked `approximate`.

### `GET /api/v1/clinical-events/{code}`

Returns one event with the same payload and temporal fields as the list. A
missing event returns `404 EVENT_NOT_FOUND`; invalid filters on the list return
`400 INVALID_FILTER`. Unexpected read failures return `500 INTERNAL_ERROR` and
never include partial event data.

## General repository structure

```text
backend/
├── src/main/java/com/careme/backend/
│   ├── controller/ · service/ · repository/ · entity/ · dto/
│   ├── config/                         # CORS
│   ├── exception/                      # global handler
│   └── CaremeBackendApplication.java   # entry point
├── src/main/resources/
│   ├── application.yml                 # port, datasource, JPA, CORS, LLM, events + encounters dir
│   ├── application-{dev,pre,prd}.yml   # per-environment overrides
│   ├── prompts/clinical-agent-v1.txt   # in use: the agent with the declared operations
│   ├── prompts/clinical-answer-v1.txt  # grounded answer composition prompt
│   ├── prompts/clinical-answer-v2.txt  # adds the unsupported part of the question
│   ├── prompts/clinical-answer-v3.txt  # in use: adds the reported answer coverage
│   ├── prompts/measurement-answer-v1.txt # in use: measurement answer composition prompt
│   └── db/migration/                   # Flyway migrations (V1 legacy measurements, V2 event index, V3 encounter index, V4 event provenance, V5 metric model)
├── src/test/java/com/careme/backend/   # mirrors the package structure
├── .mvn/wrapper/                       # Maven wrapper configuration
├── Dockerfile
├── mvnw / mvnw.cmd
└── pom.xml
```

## Installation guide

**Requirements**

- JDK 21. The build is pinned: `maven-enforcer-plugin` fails with an explicit
  message when `JAVA_HOME` points at another major version.
- No Maven installation needed; the wrapper downloads Maven 3.9.9 on first use.
- A reachable PostgreSQL 17 instance. From the repository root,
  `podman compose up -d postgres` starts one on `localhost:5432` with the
  defaults below (`docker compose up -d postgres` when Docker is used instead of
  Podman).

```bash
cd backend
./mvnw dependency:go-offline      # Windows: .\mvnw.cmd dependency:go-offline
```

**Configuration** (`src/main/resources/application.yml`)

| Property                       | Default                                        |
| ------------------------------ | ---------------------------------------------- |
| `server.port`                  | `8080`                                         |
| `spring.datasource.url`        | `jdbc:postgresql://localhost:5432/careme` (`CAREME_DB_URL`) |
| `spring.datasource.username`   | `careme` (`CAREME_DB_USER`)                    |
| `spring.datasource.password`   | `careme` (`CAREME_DB_PASSWORD`)                |
| `spring.jpa.hibernate.ddl-auto` | `validate`                                    |
| `careme.cors.allowed-origins`  | `http://localhost:3000`                        |
| `careme.events.directory`      | `data/events` (`CAREME_EVENTS_DIRECTORY`)      |

`careme.events.directory` holds the Markdown source of truth for clinical events.
It must be writable by the user that runs the process and must outlive the
container. The image creates `/app/data/events` owned by the non-root `careme`
user, and the Compose stack mounts a named volume on `/app/data`; mounting the
volume lower would leave the directory root-owned and the registration would fail
with `AccessDeniedException`.

Profiles `dev`, `pre` and `prd` override logging and CORS origins; in `pre` and
`prd` the origins come from `CAREME_CORS_ALLOWED_ORIGINS` and the datasource
variables have no defaults, so the application refuses to boot without them.

## Run guide

The backend needs PostgreSQL running first. Containers run with **Podman**
preferred; when Podman is not installed, replace `podman` with `docker` in every
command.

**1. Start the database** (from the repository root)

```bash
podman compose up -d postgres     # Docker: docker compose up -d postgres
```

**2. Start the backend** — pick one

```bash
# Development server with live reload, from backend/ (port 8080)
./mvnw spring-boot:run            # Windows: .\mvnw.cmd spring-boot:run

# Packaged jar
./mvnw package -DskipTests && java -jar target/backend-0.0.1-SNAPSHOT.jar

# Container, from the repository root (builds the image too)
podman compose up --build postgres backend   # Docker: docker compose up --build postgres backend
```

Flyway creates the metric-model tables (`measurements`, `measurement_values` and
`admitted_metrics`) on the first start. Verify the service with
`curl http://localhost:8080/api/v1/measurements`.

**If port 8080 is already taken**, the startup aborts with
`Web server failed to start. Port 8080 was already in use.` and
`APPLICATION FAILED TO START` (exit 1) — never a silent fallback to another
port. The usual owner is a previous `spring-boot:run`, the packaged jar or the
`careme-backend-1` container. Stop it before starting again:

```powershell
# Windows: show the process holding 8080
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess
```

## Testing

Integration tests start their own PostgreSQL container through Testcontainers, so
the suite needs a container runtime reachable through the Docker API — Podman is
the default and Docker also works. The compose database does **not** need to be
running.

```bash
./mvnw test        # unit, web-layer and integration tests
./mvnw verify      # tests + JaCoCo report + coverage gate
```

The suite is 58 test classes, organized by layer:

| Class                                      | Covers                                                     |
| ------------------------------------------ | ---------------------------------------------------------- |
| `CaremeBackendApplicationTests`            | Full stack: real database, envelope, ordering, CORS preflight |
| `CorsConfigTest`                           | The CORS verbs the interface uses stay listed                |
| `ApiExceptionHandlerTest`                  | Upload-limit error contract, enforced before the controller  |
| `MeasurementControllerTest`                | `@WebMvcTest`: envelope shape, success and error mappings   |
| `MeasurementImportControllerTest`          | `@WebMvcTest`: preview and import endpoints                 |
| `ClinicalEventIndexControllerTest`         | `@WebMvcTest`: inspection list/detail and the reindex endpoint |
| `MeasurementServiceTest`                   | Ordering, mapping, empty dataset, error pass-through        |
| `MeasurementImportServiceTest`             | All-or-nothing import, preview counts                       |
| `MeasurementCsvParserTest`                 | CSV parsing, headers, per-row validation                    |
| `MetricMeasurementTest` / `MetricTest`     | Metric domain invariants, components and admissible ranges   |
| `MetricCatalogServiceTest`                 | Metric catalogue, unit resolution and exact unit conversion  |
| `MeasurementRegistrationServiceTest`       | Registration at the close, per-metric-and-day replacement    |
| `MetricBackedMeasurementRepositoryTest`    | The legacy daily view composed over the metric model         |
| `MigrationV5BackfillsLegacyMeasurementsTest` | The V5 migration copies the legacy rows and retires its table |
| `MeasurementTest` / `MeasurementDraftTest` | Domain invariants                                           |
| `ChatControllerTest`                       | `@WebMvcTest`: chat contract, absence reason and offered actions, close endpoint |
| `ChatOrchestratorTest`                     | Operation → status mapping, idempotency by message id       |
| `ClinicalEventTest`                        | Clinical-event domain invariants                            |
| `ClinicalEventIntentTest`                  | Intent variants, query payload and rejected combinations    |
| `EncounterTest`                            | Consultation life cycle invariants: notes, close, no reopening |
| `ClinicalEventDateNormalizerTest`          | Exact, relative, approximate and unknown dates              |
| `ClinicalEventIntentValidatorTest`         | Rejection of non-registrable or invalid intents             |
| `ClinicalEventMarkdownStoreTest`           | Markdown round-trip, code reservation, atomic publish       |
| `EncounterMarkdownStoreTest`               | Encounter round-trip: notes, motive and derived summary     |
| `ClinicalEventRegistrationServiceTest`     | Registration at the close, dedup within conversation, rollback on failure |
| `EncounterServiceTest`                     | Open, collect notes without writing, close, retry and interruption |
| `AgentOperationTest`                       | Declared operation set and name resolution                  |
| `AgentOperationExecutorTest`               | Argument validation and per-operation execution              |
| `AgentToolContractTest`                    | Wire shape of the declared tools and tool calls              |
| `AgentTurnRunnerTest`                      | Turn loop, operation budget and failure mapping              |
| `AgentTurnOutcomeTest`                     | Operation list and turn status precedence                    |
| `FakeClinicalAgentTest`                    | Deterministic offline agent that also chooses operations     |
| `OpenAiAgentProviderTest`                  | Startup guard on the credential and prompt loading           |
| `ClinicalAnswerResultTest`                 | Answer-kind normalization and collection null-safety         |
| `ClinicalEventQueryRepositoryTest`         | Lexical and metadata retrieval, absence counts on a real index |
| `ClinicalHistoryQueryServiceTest`          | Absence reason ladder, partial answers, failure vs absence  |
| `ClinicalEventInspectionServiceTest`       | Inspection ordering, filters and read-failure mapping       |
| `ConversationStateStoreTest`               | Recent-turn buffer bounds, eviction and expiry              |
| `ClinicalHistoryQueryFlowIntegrationTest`  | Register → ask cycle, every absence reason leaves the history untouched |
| `ClinicalHistoryUnsupportedRetrievalIntegrationTest` | A retrieved event that does not answer the question declares the absence |
| `ClinicalEventInspectionIntegrationTest`   | Inspection reads leave Markdown and the derived index untouched |
| `ClinicalEventProvenanceIntegrationTest`   | The consultation of origin survives the index rebuild from Markdown |
| `ClinicalEventSurvivesLaterFailureIntegrationTest` | A noted fact survives a later failure of the same turn |
| `EncounterIndexRebuilderIntegrationTest`   | Encounter index rebuild, upsert and closed-consultation recovery |
| `OpenAiClinicalAnswerComposerTest`         | Grounded composition, citations, reported coverage          |
| `FakeClinicalAnswerComposerTest`           | Deterministic offline composition and reported coverage     |
| `ClinicalConversationIntegrationTest`      | Conversational outcomes leave the index and the documents untouched, with no repository read |

Integration tests extend `PostgresIntegrationTest`, which starts one
`postgres:17-alpine` container per JVM and reuses it. They run the real Flyway
migration, so the schema under test is the schema in production. A container
runtime must be reachable: on Windows, Podman exposes its Docker-compatible pipe
at the default location Testcontainers uses.

The coverage gate runs at `verify`, not `test`, so a quick `mvn test` is not
blocked by coverage while a full build is.

## Additional notes

- **Writes are explicit and few.** The write surface is `POST /api/v1/measurements`,
  the CSV import endpoints, `POST /api/v1/chat/messages`,
  `POST /api/v1/chat/conversations/{conversationId}/consultation/close` and
  `POST /api/v1/clinical-events/reindex`; there is no update or delete endpoint.
  Adding a measurement write means adding a method to `MeasurementRepository` and
  a `save` on `MetricMeasurementJpaDao`; the controller, the service and the JSON
  contract stay as they are.
- **`(metric, date)` is unique.** The tracking holds one measurement per metric and
  day. Recording several readings of the same metric on a day means dropping
  `uq_measurements_metric_date`, since the application cannot decide which of the
  day's rows to return; recording several metrics on the same day needs nothing.
- **The tracking starts empty.** The dashboard shows its empty state until
  measurements are stored, through the registration form, the CSV import or the
  chat close.
- **CORS is not strictly required today** because the frontend fetches on the
  server. It is explicit so a future client-side call works without opening the
  API to every origin.
- Follows `docs/standards/java-springboot-standards.md`, which prescribes
  PostgreSQL, Spring Data JPA and Flyway.
