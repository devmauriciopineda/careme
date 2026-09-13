# Careme

## Project description

Careme is a full-stack body-tracking application. It records two daily metrics —
weight and abdominal circumference — and presents them as trend charts and a
daily table.

The project exists to replace a manual spreadsheet: a single place to log body
measurements day by day and see the trend without doing the math by hand. It is
an early MVP with no authentication; today the product is limited to viewing and
registering body measurements.

> **Product direction.** The project is expanding into an AI assistant for a
> personal clinical history. The assistant becomes the entry point of the
> application and body tracking moves to a secondary view. None of this is
> implemented yet; [`docs/roadmap/`](./docs/roadmap) holds the roadmap and the
> scope of the MVP of the assistant.

## General functionality

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
                │  Spring Data JPA
                ▼
            PostgreSQL (measurements table, Flyway-managed)
```

- The **frontend** is a Next.js App Router application. The dashboard is an async
  Server Component: it fetches the measurements on the server, validates the
  payload and derives every chart series, axis domain and date label there. The
  registration form is a client island that submits through a Server Action,
  which revalidates the path so the charts and the table refresh on their own.
  Recharts is the only other client-side island.
- The **backend** is a Spring Boot REST service with read, write and import
  endpoints. It follows a layered structure (controller → service → repository)
  and is the source of truth for the API contract.
- **Storage** is PostgreSQL, behind a repository interface. Flyway owns the
  schema, so the table exists from the first start without manual DDL.

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
| Backend build   | Maven (wrapper committed), JaCoCo coverage gate                             |
| Backend testing | JUnit 5, Mockito, MockMvc, Spring Boot Test, AssertJ, Testcontainers        |
| Runtime         | Podman (preferred) or Docker with Compose; pnpm 10 for the frontend        |

## Use of APIs or external services

- The application consumes **no external or third-party APIs** today. There are no
  cloud services, SDKs or analytics integrations. The planned assistant will call
  an OpenAI-compatible LLM provider from the backend, with its key read from an
  environment variable; see [`docs/roadmap/`](./docs/roadmap).
- The frontend consumes the backend's own HTTP API
  (`GET` and `POST /api/v1/measurements`, plus the CSV import endpoints). The
  contract is documented in [`backend/README.md`](./backend/README.md).
- Measurements live in a **PostgreSQL** table owned by the backend, created and
  versioned by Flyway migrations.
- There is **no authentication or authorization** in this MVP.
- Images and fonts: no image CDN. The only third-party asset source is
  `next/font/google` (Geist and Geist Mono), which Next.js downloads at build
  time and self-hosts, so no font service is contacted at runtime.
  `frontend/public/` is reserved and currently empty.

## General repository structure

```text
careme/
├── backend/                  # Spring Boot REST service (own README)
├── frontend/                 # Next.js dashboard (own README)
├── docs/roadmap/             # roadmap and MVP scope of the AI assistant
├── docs/standards/           # coding standards used by both services
├── docs/use-cases/           # formal use cases (UC-001 …)
├── .github/prompts/          # planning prompts kept with the project
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
2. Open http://localhost:3000.
3. The dashboard shows the date range, the record count, one trend chart per
   metric and the daily table. Hover or focus a chart to read individual values:
   with the chart focused, the left and right arrow keys move across data points.
4. The table starts empty, so the dashboard first shows its empty state. Register
   the measurement of a day with the registration form, or load a CSV file with
   the import action. Re-registering a day replaces its values, because there is
   at most one measurement per day.
5. If the backend is not running, the frontend shows a "measurements could not be
   loaded" screen with a retry button instead of crashing.

## Additional notes

- **Measurements can be registered and imported, but not edited or deleted.**
  The registration form replaces the values of a day that already has one, and
  the CSV import does the same in bulk. There is no delete endpoint, no
  pagination, no filtering and no runtime i18n.
- **One measurement per day.** The table has a unique constraint on `date`.
- **CORS is not required today** because the frontend fetches on the server. It is
  configured explicitly so a future client-side call cannot open the API to every
  origin.
- **Standards.** Both services follow the documents in `docs/standards/`
  (`next-standards.md`, `java-springboot-standards.md`), adopted progressively
  rather than all at once.
- No license file is present, so the project is currently unlicensed.
