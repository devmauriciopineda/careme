# Careme

## Project description

Careme is a full-stack personal clinical assistant and body-tracking
application. Its chat entry point collects the clinical facts a person tells it
in Spanish —diagnoses, medications and notes— and the measurements they mention,
as notes of the open consultation, and registers them when the person ends the
consultation: the facts into a personal clinical history, preserving the temporal
precision they expressed, and the measurements into the metric tracking, each with
its metric's reference unit and its exact date. In both cases it declares the
consultation they came from. Body tracking records one measurement per metric and
day —weight and abdominal circumference among the catalogue's metrics— and
presents them as trend charts and a daily table.

The project exists to replace a manual spreadsheet: a single place to log body
measurements day by day and see the trend without doing the math by hand. It is
an early MVP with no authentication; the assistant registers patient-stated
facts only and never diagnoses or recommends treatment.

> **Product direction.** The assistant is now the entry point of the application
> and body tracking moved to a secondary view at `/measurements`. The chat flow,
> the clinical-event registration and the LLM adapter are implemented;
> [`docs/roadmap/`](./docs/roadmap) holds the remaining roadmap and the scope of
> the MVP of the assistant.

## General functionality

- A Spanish-language chat entry point that collects what a message mentions as
  notes of the open consultation: the clinical facts (diagnosis, medication or
  note), preserving the original wording and the temporal precision (exact,
  approximate or unknown), and the measurements (metric, value and date), which
  are never clinical facts. The facts are registered as clinical events —with
  their provenance— and the measurements into the metric tracking when the
  consultation is closed.
- The conversation is a consultation with identity and life cycle: it opens with
  the conversation, is the only one in progress, and closes when the person ends
  it — or when another conversation starts or the process stops — registering the
  facts it collected.
- The same chat answers questions about the measurements: it reads the metric
  tracking and replies with the values, their reference unit and their exact date,
  one measurement per metric and day —blood pressure with its two values—, and it
  declares an absence, a period without measurements or a metric outside the
  tracking instead of inventing values. Asking never changes the tracking.
- The same chat answers general conversation that does not depend on the clinical
  history, without reading it, and declines requests for a diagnosis, a
  recommendation or an interpretation of the case.
- Clinical events are stored as Markdown documents (`data/events/`) as the source
  of truth, with a rebuildable PostgreSQL full-text index. They can be inspected
  through read-only list and detail endpoints; editing and deletion are not
  available.
- Body tracking on a metric model: one measurement per metric and day, each in its
  metric's reference unit, over an extensible catalogue that ships weight (kg),
  abdominal circumference (cm), blood pressure (mmHg —one measurement with
  systolic and diastolic—) and cholesterol (mg/dL).
- A registration form that records the measurement of a day; re-registering a day
  replaces its values instead of creating a second record.
- A CSV import that loads a whole history at once, with a preview that separates
  the days that are new from the ones that would be replaced.
- Input validation in Spanish: required fields, positive values, a single decimal,
  the allowed limits and a date that is not in the future.
- A selector over the admitted catalogue that offers only the metrics with
  registered measurements, with weight and abdominal circumference selected by
  default.
- One trend chart per selected metric, with an axis domain padded around the data
  range so small day-to-day variations stay readable; blood pressure is one metric
  with a systolic and a diastolic series.
- A single daily detail table with a column per selected metric, ordered from the
  oldest record to the most recent.
- An isolated failure per metric: a metric that cannot be read says so in Spanish
  and offers its own retry, without hiding the metrics that did load.
- Localized date and number formatting (Spanish) that does not shift with the
  visitor's time zone.
- A route-level error screen when the data service is unreachable.

## Architecture overview

Two independent services plus a shared documentation folder:

```text
browser ──▶ frontend (Next.js, port 3000)
                │  server-side fetch at request time
                ▼
            backend (Spring Boot, port 8080)
                │  Spring Data JPA                 │  filesystem
                ▼                                  ▼
      PostgreSQL (measurements +          data/events/ (Markdown source of
      measurement_values +                 truth for clinical events) and
      admitted_metrics +                   data/encounters/ (Markdown source
      clinical_event_index +               of truth for consultations)
      encounter_index,
      Flyway-managed)
```

- The **frontend** is a Next.js App Router application. The chat workspace is the
  entry point at `/`; clinical-event inspection lives at `/clinical-events`; the
  body-tracking dashboard lives at `/measurements`. The dashboard is an async
  Server Component: it reads the admitted catalogue and one payload per metric on
  the server, and hands them resolved to a client island that owns the metric
  selection and the per-metric retry. The registration form is a client island
  that submits through a Server Action, which revalidates the path so the charts
  and the table refresh on their own. Recharts, the registration/import forms and
  the chat workspace are the client-side islands.
- The **backend** is a Spring Boot REST service with measurement read, write and
  import endpoints, a chat endpoint, a consultation-close endpoint, read-only
  clinical-event inspection endpoints and a clinical-event reindex endpoint. It
  follows a layered structure (controller → service → repository) and is the
  source of truth for the API contract.
- **Storage** is PostgreSQL, behind a repository interface, plus the Markdown
  files in `data/events/` as the source of truth for clinical events and
  `data/encounters/` as the source of truth for consultations. Flyway owns the
  schema, so the tables exist from the first start without manual DDL; the
  PostgreSQL indexes of clinical events and consultations are derived and
  rebuildable.

Each service is documented in its own README:

- [`backend/README.md`](./backend/README.md) — API contract, layers, configuration, tests.
- [`frontend/README.md`](./frontend/README.md) — UI architecture, scripts, testing, accessibility.

## Technology stack

| Area            | Technology                                                                 |
| --------------- | -------------------------------------------------------------------------- |
| Frontend        | Next.js 16 (App Router, Server Components), React 19, TypeScript 5 (strict) |
| Frontend UI     | Tailwind CSS v4, shadcn/ui, Recharts, lucide-react                          |
| Frontend data   | Native `fetch` behind a service layer, Zod validation, Vitest + RTL         |
| Backend         | Java 21, Spring Boot 3.5 (Web MVC, Bean Validation, Data JPA)               |
| Backend data    | PostgreSQL 17, Hibernate ORM 6, Flyway migrations                           |
| Backend LLM     | Spring `RestClient` → OpenAI-compatible API (DeepSeek, opt-in via `CAREME_LLM_MODE=openai`) |
| Backend build   | Maven (wrapper committed), JaCoCo coverage gate                             |
| Backend testing | JUnit 5, Mockito, MockMvc, Spring Boot Test, AssertJ, Testcontainers        |
| Runtime         | Podman (preferred) or Docker with Compose; pnpm 10 for the frontend        |

## Use of APIs or external services

- The backend consumes **DeepSeek's OpenAI-compatible LLM API** for the chat
  assistant, which is the default (`CAREME_LLM_MODE=openai`); setting
  `CAREME_LLM_MODE=fake` selects a simulated assistant that runs with no network
  call. The key is read from the environment (`CAREME_LLM_API_KEY`) and never
  reaches the browser; see [`backend/README.md`](./backend/README.md).
- The frontend consumes the backend's own HTTP API
  (`GET` and `POST /api/v1/measurements`,
  `GET /api/v1/measurements/catalog`,
  `GET /api/v1/measurements/tracking/{metricCode}`, the CSV import endpoints,
  `POST /api/v1/chat/messages`,
  `POST /api/v1/chat/conversations/{conversationId}/consultation/close` and the
  read-only clinical-event inspection endpoints). The contract is documented in
  [`backend/README.md`](./backend/README.md).
- Measurements live in a **PostgreSQL** table owned by the backend, created and
  versioned by Flyway migrations. Clinical events live as Markdown documents in
  `data/events/` (source of truth) with a derived, rebuildable PostgreSQL index;
  consultations live the same way in `data/encounters/`.
- There is **no authentication or authorization** in this MVP.
- Images and fonts: no image CDN. The only third-party asset source is
  `next/font/google` (Geist and Geist Mono), which Next.js downloads at build
  time and self-hosts, so no font service is contacted at runtime.
  `frontend/public/` is reserved and currently empty.

## General repository structure

```text
careme/
├── backend/                  # Spring Boot REST service (own README)
├── frontend/                 # Next.js chat + dashboard (own README)
├── docs/roadmap/             # roadmap and MVP scope of the AI assistant
├── docs/standards/           # coding standards used by both services
├── docs/use-cases/           # formal use cases (UC-001 …)
├── openspec/                 # OpenSpec specs and archived changes
├── .github/prompts/          # planning prompts kept with the project
├── .github/skills/           # agent skills used by the assistant
├── .vscode/settings.json     # editor JDK configuration (Java 21)
├── docker-compose.yml        # postgres + backend + frontend stack
└── README.md
```

## Installation guide

**Prerequisites**

- Java 21 (JDK). The backend build fails on a different major version.
- Node.js 22+ and pnpm 10 (`corepack enable`).
- A container runtime for PostgreSQL and the containerized stack (see below).

**Container runtime — Podman first, Docker as fallback**

Containers run with **Podman** whenever it is available. Use **Docker** only when
Podman is not installed: every `podman` command below has a `docker` equivalent.

| Purpose              | Preferred (Podman)                 | Fallback (Docker)                 |
| -------------------- | ---------------------------------- | --------------------------------- |
| Whole stack          | `podman compose up --build -d`     | `docker compose up --build -d`    |
| Database only        | `podman compose up -d postgres`    | `docker compose up -d postgres`   |
| Follow backend logs  | `podman compose logs -f backend`   | `docker compose logs -f backend`  |
| Stop the stack       | `podman compose down`              | `docker compose down`             |

- On macOS/Windows, start the Podman machine first (`podman machine start`).
- `podman compose` delegates to an external Compose provider, so `docker-compose`
  must be on the PATH. Verified here: `podman compose` runs
  `docker-compose.exe` (Docker Compose v5), so the Docker **engine** is not
  needed — only the `docker-compose` CLI plus a running Podman machine.
- The backend's integration tests need a container runtime reachable through the
  Docker API. Podman exposes one; see
  [`backend/README.md`](./backend/README.md).

**Install dependencies**

```bash
cd frontend && pnpm install
cd ../backend && ./mvnw dependency:go-offline   # Windows: .\mvnw.cmd dependency:go-offline
```

**Environment variables**

The frontend needs one, and only when the backend does not run on its default
address:

```bash
# frontend/.env.local
API_BASE_URL=http://localhost:8080
```

`API_BASE_URL` is read on the server and deliberately has no `NEXT_PUBLIC_`
prefix, so it is never exposed to the browser.

The backend needs none in development: it defaults to the Postgres started by
`docker-compose.yml`. The `pre` and `prd` profiles require `CAREME_DB_URL`,
`CAREME_DB_USER` and `CAREME_DB_PASSWORD`, and the application refuses to boot
without them.

## Run guide

### Option A — Whole stack with containers

No local JDK, Node or PostgreSQL needed; the images build everything.

```bash
podman compose up --build -d       # Docker: docker compose up --build -d
podman compose logs -f backend     # wait for "Started CaremeBackendApplication"
podman compose down                # stop and remove the stack
```

Then: frontend at http://localhost:3000, backend at http://localhost:8080,
PostgreSQL at `localhost:5432`.

### Option B — Local development, three terminals

```bash
# terminal 1 — database only (port 5432)
podman compose up -d postgres      # Docker: docker compose up -d postgres

# terminal 2 — backend (port 8080)
cd backend
./mvnw spring-boot:run             # Windows: .\mvnw.cmd spring-boot:run

# terminal 3 — frontend (port 3000)
cd frontend
pnpm install
pnpm dev
```

Start the database first: the backend refuses to start without it, and the
frontend needs the backend to render data.

### Run the tests

```bash
# backend — unit, web-layer and integration tests
cd backend
./mvnw test                        # Windows: .\mvnw.cmd test
./mvnw verify                      # tests + JaCoCo coverage gate

# frontend — Vitest
cd frontend
pnpm test
```

The backend integration tests start their own PostgreSQL container through
Testcontainers, so the container runtime is required but `docker-compose.yml`
does not need to be running. The frontend tests run in jsdom and need neither the
backend nor the database.

The strategy behind these suites — levels, quality gates and regression scope — is
documented in [`docs/testing/`](./docs/testing/README.md).

### Run the end-to-end tests (Playwright)

The scenarios under `frontend/e2e/playwright/` drive the real interface against a
running stack, so they need four things in place before they run. Getting any of
them wrong produces failures that point away from the real cause.

**1. A container runtime, Podman first.** Podman is preferred; use Docker only
when Podman is not installed. On Windows `docker` is often absent while
`docker-compose` exists only as a shim, so `docker compose` fails where `podman`
works.

**2. PostgreSQL, reachable from the backend.** Start it with
`podman compose up -d postgres`, or directly:

```bash
podman run -d --name careme-pg -e POSTGRES_DB=careme -e POSTGRES_USER=careme \
  -e POSTGRES_PASSWORD=careme -p 5432:5432 postgres:17-alpine
```

**A port published by Podman is not reachable on IPv4.** `podman port` reports
`0.0.0.0:5432`, but on the host `127.0.0.1:5432` is refused while `[::1]:5432`
accepts: Podman forwards on the IPv6 loopback. The JDBC driver resolves
`localhost` to IPv4 first, so the backend dies during Flyway with
`Connection to localhost:5432 refused` (`SQL State: 08001`) while the container is
healthy. Point it at the IPv6 loopback instead:

```bash
CAREME_DB_URL="jdbc:postgresql://[::1]:5432/careme"
```

Two checks mislead here: `pg_isready` **inside** the container proves nothing
about the host mapping, and `Test-NetConnection -InformationLevel Quiet` can
report the port as reachable while it is closed. Verify with a real TCP connect
from the host.

**3. Backend and frontend up, with the clinical-event directory on the host.** Run
the backend locally rather than through compose: compose mounts the events into
the named volume `careme-events`, which a host `CAREME_EVENTS_DIRECTORY` cannot
point at. Use a scratch directory so the committed `backend/data/` stays intact —
the scenarios assert that directory starts **empty**:

```bash
# terminal 1 — backend
cd backend
CAREME_EVENTS_DIRECTORY="$PWD/target/e2e-events" \
CAREME_ENCOUNTERS_DIRECTORY="$PWD/target/e2e-encounters" \
CAREME_DB_URL="jdbc:postgresql://[::1]:5432/careme" \
./mvnw spring-boot:run                 # Windows: .\mvnw.cmd spring-boot:run

# terminal 2 — frontend
cd frontend && pnpm dev
```

**4. The real assistant, configured.** The scenarios exercise the provider, so
`CAREME_LLM_API_KEY` must be set. A walkthrough costs several provider round
trips, which is why a scenario that makes more than one may need its own budget
(`test.describe.configure({ timeout: … })`) instead of the per-test default.

Then run a scenario with the events directory exported, so it can compare the
history before and after a turn:

```powershell
# Windows
$env:CAREME_EVENTS_DIRECTORY = "C:\path\to\careme\backend\target\e2e-events"
cd frontend
pnpm exec playwright test --project=interface e2e/playwright/uc-014b.spec.ts
```

```bash
# Linux / macOS
CAREME_EVENTS_DIRECTORY="$PWD/backend/target/e2e-events" \
  pnpm exec playwright test --project=interface e2e/playwright/uc-014b.spec.ts
```

### If something is already running, or a port is taken

> **Do not run Option A and Option B at the same time.** Ports 3000 and 8080 are
> published on the host by the containers and used directly by the local
> servers. When both run, `podman compose up -d` still exits `0` and `podman ps`
> still shows healthy containers, but the host ports stay with the local
> processes, so you silently keep using the old instance. Check who owns the port
> before starting anything.

**Check the ports first**

```powershell
# Windows
Get-NetTCPConnection -LocalPort 3000,8080,5432 -State Listen -ErrorAction SilentlyContinue |
  Select-Object LocalPort, OwningProcess
```

```bash
# Linux / macOS
lsof -iTCP:3000 -iTCP:8080 -iTCP:5432 -sTCP:LISTEN
```

**What each failure looks like**

| Symptom                                                                                     | Cause                                                                            | Fix                                                                                                              |
| ------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `Web server failed to start. Port 8080 was already in use.` + `APPLICATION FAILED TO START` | Another backend owns 8080: a previous `spring-boot:run`, the packaged jar or `careme-backend-1` | Stop it (Ctrl+C in its terminal, or kill the PID from the port check).                              |
| `Port 3000 is in use by process …, using available port 3001 instead` + `Another next dev server is already running` | A previous `pnpm dev` is still alive. Next.js does **not** fail, it moves to **3001** | Stop the old server. Port 3001 will not match `careme.cors.allowed-origins` (`http://localhost:3000`). |
| `rootlessport listen tcp4 0.0.0.0:5432: bind: address already in use` (exit 1)                | Another PostgreSQL or a second compose project owns 5432                          | `podman compose down`, or stop the local PostgreSQL service.                                                     |
| `postgres` reports `Running` and exit is `0`                                                  | The database was already up; compose is idempotent                               | Nothing to fix. Run `podman compose down` first when you want a clean restart.                                    |
| `podman ps` shows `Up` with `0.0.0.0:8080->8080/tcp`, but the browser serves the old build    | The stack was started while the local servers were running, so the host port was never published | Use one mode only: `podman compose down` and stop the local servers, or keep the local servers. |

## Basic usage

1. Start the backend and the frontend (either mode above).
2. Open http://localhost:3000. The chat workspace is the entry point: tell it a
  clinical fact ("me diagnosticaron hipertensión el mes pasado") or a measurement
  ("hoy me tomaron la presión y fue 145/92") and it collects it as a note of the
  open consultation — the turn writes neither the history nor the tracking; use
  the **Terminar consulta** action to end the consultation and register the
  collected facts with their provenance and the measurements in the metric
  tracking. Or ask about your clinical history, or about your measurements
  ("¿cuánto peso?", "¿cuál es mi presión arterial?"), to receive an answer with
  supporting records or with the registered values, units and dates. It may ask
  for clarification or answer as general conversation. If the LLM is not configured it runs the `fake`
  interpreter, safe for local development.
3. Open http://localhost:3000/measurements for the body-tracking dashboard. It
   lists the metrics that have registered measurements, shows one trend chart per
   selected metric and a single daily detail table with a column per selected
   metric. Hover or focus a chart to read individual values: with the chart
   focused, the left and right arrow keys move across data points.
4. The table starts empty, so the dashboard first shows its empty state. Register
   the measurement of a day with the registration form, or load a CSV file with
   the import action. Re-registering a day replaces its values, because the form
   records one weight-and-circumference pair per day.
5. If the backend is not running, the dashboard shows a "measurements could not
   be loaded" screen with a retry button instead of crashing.

## Additional notes

- **Measurements can be registered and imported, but not edited or deleted.**
  The registration form replaces the values of a day that already has one, and
  the CSV import does the same in bulk. There is no delete endpoint, no
  pagination, no filtering and no runtime i18n.
- **Clinical events can be registered and queried, but not edited or deleted.**
  The chat answers history questions from the PostgreSQL index and the index can
  be rebuilt from the Markdown documents; edit and delete use cases remain on the
  roadmap.
- **A conversation is a consultation.** What a message mentions is collected as a
  note and is not part of the clinical history while the consultation is open;
  ending the consultation registers the admissible notes with their provenance.
  Consultations live as Markdown in `data/encounters/`.
- **One measurement per metric and day.** The tracking has a unique constraint on
  `(metric, date)`, so re-registering that metric and that day replaces its value
  instead of adding a second measurement. Several metrics can be recorded on the
  same day.
- **CORS is not required today** because the frontend fetches on the server. It is
  configured explicitly so a future client-side call cannot open the API to every
  origin.
- **Standards.** Both services follow the documents in `docs/standards/`
  (`next-standards.md`, `java-springboot-standards.md`), adopted progressively
  rather than all at once.
- No license file is present, so the project is currently unlicensed.
