# Careme

## Project description

Careme is a full-stack body-tracking application. It records two daily metrics —
weight and abdominal circumference — and presents them as trend charts and a
daily table.

The project exists to replace a manual spreadsheet: a single place to log body
measurements day by day and see the trend without doing the math by hand. It is
an early MVP, so it is deliberately read-only: it displays measurements, it does
not create or edit them yet.

## General functionality

- Daily records of weight (kg) and abdominal circumference (cm).
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
  payload and derives every chart series, axis domain and date label there.
  Recharts is the only client-side island.
- The **backend** is a Spring Boot REST service with a single read endpoint. It
  follows a layered structure (controller → service → repository) and is the
  source of truth for the API contract.
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
| Runtime         | Podman with Compose for the containerized stack; pnpm 10 for the frontend   |

## Use of APIs or external services

- The application consumes **no external or third-party APIs**. There are no
  cloud services, SDKs or analytics integrations.
- The frontend consumes the backend's own HTTP API (`GET /api/v1/measurements`).
  The contract is documented in [`backend/README.md`](./backend/README.md).
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
├── docs/standards/           # coding standards used by both services
├── .github/prompts/          # planning prompts kept with the project
├── .vscode/settings.json     # editor JDK configuration (Java 21)
├── docker-compose.yml        # postgres + backend + frontend stack
└── README.md
```

## Installation guide

**Prerequisites**

- Java 21 (JDK). The backend build fails on a different major version.
- Node.js 22+ and pnpm 10 (`corepack enable`).
- Podman (with its machine running) for the containerized stack. `podman compose`
  delegates to an external Compose provider, so `docker-compose` must be on the
  PATH.
- A container runtime reachable through the Docker API for the backend's
  integration tests. Podman exposes one; see
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

**Whole stack with containers** (verified)

```bash
podman compose up --build -d
podman compose logs -f backend
podman compose down
```

**Local development**, three terminals

```bash
# terminal 1 — database only
podman compose up -d postgres

# terminal 2
cd backend && ./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run

# terminal 3
cd frontend && pnpm dev
```

## Basic usage

1. Start the backend and the frontend (either mode above).
2. Open http://localhost:3000.
3. The dashboard shows the date range, the record count, one trend chart per
   metric and the daily table. Hover or focus a chart to read individual values:
   with the chart focused, the left and right arrow keys move across data points.
4. The table starts empty, so the dashboard first shows its empty state. To add a
   record, insert a row and reload the page:

   ```bash
   podman compose exec postgres psql -U careme -d careme \
     -c "INSERT INTO measurements (date, weight_kg, waist_cm) VALUES (CURRENT_DATE, 80.1, 94.8)"
   ```

   The backend reads the database on every request, so there is nothing to
   restart.
5. If the backend is not running, the frontend shows a "measurements could not be
   loaded" screen with a retry button instead of crashing.

## Additional notes

- **Measurements are read-only from the application.** There are no create,
  update or delete endpoints yet, so rows are inserted directly into the
  database. There is no pagination, no filtering and no runtime i18n.
- **One measurement per day.** The table has a unique constraint on `date`.
- **CORS is not required today** because the frontend fetches on the server. It is
  configured explicitly so a future client-side call cannot open the API to every
  origin.
- **Standards.** Both services follow the documents in `docs/standards/`
  (`next-standards.md`, `java-springboot-standards.md`), adopted progressively
  rather than all at once.
- No license file is present, so the project is currently unlicensed.
