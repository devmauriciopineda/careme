# Careme

## Project description

Careme is a full-stack personal clinical assistant and body-tracking
application. Its chat entry point records the clinical facts a person tells it
in Spanish —diagnoses, medications, measurements and notes— into a personal
clinical history, preserving the temporal precision they expressed. It also
records two daily body metrics —weight and abdominal circumference— and presents
them as trend charts and a daily table.

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

- A Spanish-language chat entry point that turns natural-language messages into
  clinical events (diagnosis, medication, measurement or note), preserving the
  original wording and the temporal precision (exact, approximate or unknown).
- The same chat answers general conversation that does not depend on the clinical
  history, without reading it, and declines requests for a diagnosis, a
  recommendation or an interpretation of the case.
- Clinical events are stored as Markdown documents (`data/events/`) as the source
  of truth, with a rebuildable PostgreSQL full-text index; there is no query,
  edit or delete endpoint yet.
- Daily records of weight (kg) and abdominal circumference (cm).
- A registration form that records the measurement of a day; re-registering a day
  replaces its values instead of creating a second record.
- A CSV import that loads a whole history at once, with a preview that separates
  the days that are new from the ones that would be replaced.
- Input validation in Spanish: required fields, positive values, a single decimal,
  the allowed limits and a date that is not in the future.
- One trend chart per metric, with an axis domain padded around the data range so
  small day-to-day variations stay readable.
- A daily table sorted from the most recent record to the oldest.
- A date range and record count summary for the loaded period.
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
      PostgreSQL (measurements +          data/events/ (Markdown source
      clinical_event_index,                of truth for clinical events)
      Flyway-managed)
```

- The **frontend** is a Next.js App Router application. The chat workspace is the
  entry point at `/`; the body-tracking dashboard lives at `/measurements`. The
  dashboard is an async Server Component: it fetches the measurements on the
  server, validates the payload and derives every chart series, axis domain and
  date label there. The registration form is a client island that submits through
  a Server Action, which revalidates the path so the charts and the table refresh
  on their own. Recharts, the registration/import forms and the chat workspace
  are the client-side islands.
- The **backend** is a Spring Boot REST service with measurement read, write and
  import endpoints, a chat endpoint and a clinical-event reindex endpoint. It
  follows a layered structure (controller → service → repository) and is the
  source of truth for the API contract.
- **Storage** is PostgreSQL, behind a repository interface, plus the Markdown
  files in `data/events/` as the source of truth for clinical events. Flyway owns
  the schema, so the tables exist from the first start without manual DDL; the
  PostgreSQL index of clinical events is derived and rebuildable.

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

- The backend can consume **DeepSeek's OpenAI-compatible LLM API** when the chat
  assistant is explicitly enabled (`CAREME_LLM_MODE=openai`); the default
  `CAREME_LLM_MODE=fake` runs with no network call. The key is read from the
  environment (`CAREME_LLM_API_KEY`) and never reaches the browser; see
  [`backend/README.md`](./backend/README.md).
- The frontend consumes the backend's own HTTP API
  (`GET` and `POST /api/v1/measurements`, the CSV import endpoints and
  `POST /api/v1/chat/messages`). The contract is documented in
  [`backend/README.md`](./backend/README.md).
- Measurements live in a **PostgreSQL** table owned by the backend, created and
  versioned by Flyway migrations. Clinical events live as Markdown documents in
  `data/events/` (source of truth) with a derived, rebuildable PostgreSQL index.
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
  clinical fact ("me diagnosticaron hipertensión el mes pasado") to record it,
  or ask about your clinical history to receive an answer with supporting
  records. It may ask for clarification or answer as general conversation. If
  the LLM is not configured it runs the `fake` interpreter, safe for local
  development.
3. Open http://localhost:3000/measurements for the body-tracking dashboard. It
   shows the date range, the record count, one trend chart per metric and the
   daily table. Hover or focus a chart to read individual values: with the chart
   focused, the left and right arrow keys move across data points.
4. The table starts empty, so the dashboard first shows its empty state. Register
   the measurement of a day with the registration form, or load a CSV file with
   the import action. Re-registering a day replaces its values, because there is
   at most one measurement per day.
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
- **One measurement per day.** The table has a unique constraint on `date`.
- **CORS is not required today** because the frontend fetches on the server. It is
  configured explicitly so a future client-side call cannot open the API to every
  origin.
- **Standards.** Both services follow the documents in `docs/standards/`
  (`next-standards.md`, `java-springboot-standards.md`), adopted progressively
  rather than all at once.
- No license file is present, so the project is currently unlicensed.
