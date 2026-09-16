# Careme — Frontend

Next.js application for the Careme personal clinical assistant and body-tracking
dashboard. `/` is the assistant chat and `/measurements` shows daily weight and
abdominal circumference as trend charts and as a table, and registers the
measurement of a day, all through the Careme backend API.

There is no authentication yet. Measurements come from
`GET /api/v1/measurements`, and the registration form sends
`POST /api/v1/measurements`, through a service layer that validates the payload
before it reaches the backend. The chat posts to `POST /api/v1/chat/messages`
through `chatService`.

## Tech stack

- Next.js 16 (App Router, Server Components by default)
- React 19 + TypeScript 5 in strict mode
- Tailwind CSS v4 + shadcn/ui components
- Recharts through the shadcn/ui `chart` component
- Native server-side `fetch` with Zod validating the API response
- Vitest + React Testing Library
- pnpm

## Getting started

Prerequisites: Node.js 22+ and pnpm 10 (`corepack enable`). The backend and its
PostgreSQL database must already be running; see the
[root README](../README.md) for how to start them.

```bash
pnpm install                       # once, and after dependency changes
pnpm dev                           # development server at http://localhost:3000
```

The app is served at http://localhost:3000 and calls the backend at
`http://localhost:8080` by default. If the backend runs elsewhere, set
`API_BASE_URL` (see [Data source and environment](#data-source-and-environment));
otherwise the dashboard shows its error screen.

Containers run with **Podman** preferred and **Docker** as fallback. To run only
the frontend in a container, from the repository root:

```bash
podman compose up --build frontend    # Docker: docker compose up --build frontend
```

**If port 3000 is already taken**, Next.js does **not** fail: it prints
`Port 3000 is in use by process …, using available port 3001 instead` and serves
on **3001**. The extra line `Another next dev server is already running` means a
previous `pnpm dev` is still alive. Stop it — the backend's CORS allowlist is
`http://localhost:3000`, so a server on 3001 will not match the documented URLs.

Do not run this dev server while the `careme-frontend-1` container is up: both
want port 3000 and the one that started first keeps it, which makes it easy to
test an old build by mistake.

## Scripts

| Script              | Purpose                                  |
| ------------------- | ---------------------------------------- |
| `pnpm dev`          | Development server                       |
| `pnpm build`        | Production build                         |
| `pnpm start`        | Serve the production build               |
| `pnpm lint`         | ESLint                                   |
| `pnpm typecheck`    | TypeScript check without emitting        |
| `pnpm test`         | Vitest, single run                       |
| `pnpm test:watch`   | Vitest in watch mode                     |

## Project structure

```
frontend/
├── e2e/playwright/               # Reserved for end-to-end tests
├── src/
│   ├── app/                      # layout, chat entry (/), measurements (/measurements), error
│   ├── components/ui/            # shadcn/ui primitives
│   ├── features/chat/            # chat feature (workspace, actions, schema, types)
│   ├── features/measurements/    # body-tracking feature module
│   │   ├── components/           # dashboard, chart and table
│   │   └── lib/                  # pure logic, schema, copy
│   ├── services/                 # API clients (measurements, chat) and base URL
│   └── test/                     # test setup
├── components.json               # shadcn/ui configuration
├── next.config.ts                # standalone output for containers
├── tsconfig.json
└── vitest.config.mts
```

## Architecture notes

**Chat entry point.** `src/app/page.tsx` renders `ChatWorkspace`, a client island
that submits a turn through the `sendChatMessage` Server Action; the action calls
`chatService` and revalidates the path so the conversation refreshes. The chat
renders registration, clarification, general conversation, failure, answered
history queries with supporting events, and no-records outcomes distinctly, with
the absence reason the backend reported and the continuation it offered. The
body-tracking view lives at `src/app/measurements/page.tsx`.

**Server Components first.** That measurements page renders
`MeasurementsDashboard`, an async Server Component that loads the measurements and
derives every chart series, axis domain and date label on the server.
`MetricTrendChart` is a client island, because Recharts needs the browser; it
receives ready-to-render primitives, never `Date` objects or formatting logic.
This is why date labels do not shift with the visitor's time zone.

**One chart component, two usages.** `MetricTrendChart` is parameterized by
metric instead of being duplicated per metric.

**Metric registry.** `src/features/measurements/lib/metrics.ts` holds the
`METRICS` map (label, unit, decimals, color token, axis padding) and all pure
helpers. Adding a metric means adding a field to `Measurement`, a value to
`MetricKey` and an entry to `METRICS`.

**Service boundary.** `src/services/measurementService.ts` and
`src/services/chatService.ts` are the only places that know where data comes
from. The measurement service calls `GET /api/v1/measurements` with
`cache: "no-store"`, unwraps the response envelope, validates `data` with Zod and
returns chronologically sorted measurements; the chat service posts to
`/api/v1/chat/messages` and validates the turn with Zod. The base URL is read
once in `src/services/apiConfig.ts`.

**Failure handling.** When that call fails, the Server Component throws and
`src/app/error.tsx` renders a localized message with a retry button instead of the
framework's error screen.

**Registration is a Server Action.** `MeasurementForm` is a client island with
controlled inputs that validates the typed text in Spanish with
`measurementFormSchema` and hands the numeric payload to `registerMeasurement`
(`src/features/measurements/actions.ts`). The action revalidates the payload
server-side, calls `measurementService.createMeasurement` and then
`revalidatePath("/")`, so the charts and the daily detail refresh without a
manual reload. A failed save keeps every typed value so the user can retry.

**Copy.** Code is written in English; only user-facing strings are Spanish. The
measurement copy lives in `src/features/measurements/lib/strings.ts` and the chat
copy in the chat feature.

## Data source and environment

Both the dashboard and the chat read from the backend API. Copy `.env.example` to
`.env.local` and set the base URL when the backend does not run on
`http://localhost:8080`:

```bash
API_BASE_URL=http://localhost:8080
```

The variable deliberately has no `NEXT_PUBLIC_` prefix, because the fetch happens
on the server and the browser never needs the backend address.

The endpoint paths, status codes and response envelope are documented in
[`../backend/README.md`](../backend/README.md). Running the whole stack, including
the containers, is covered in the [root README](../README.md).

## Testing

```bash
pnpm test        # Vitest, single run
pnpm test:watch  # Vitest in watch mode
```

Tests run in jsdom and need neither the backend nor the database.

121 test cases across the measurements and chat features, including validation
and rendering of the answered and no-records chat outcomes with supporting
events, the absence reason and the offered continuation.
Unit tests cover the pure helpers in `measurements/lib/metrics.ts` (sorting,
series building, axis domain, localization, local-day helpers), the Zod schemas
(measurement API payloads, the registration form and the chat turn) and both
services. Integration tests render `MeasurementsTable` (row order, number
formatting, accessible caption), `MeasurementForm` (validation, a successful
save, a failed save that keeps the typed values, the disabled state while saving)
and `ChatWorkspace` with its actions.

## Accessibility

- Every registration control has an associated label, errors are announced with
  `role="alert"` and referenced from the field through `aria-describedby`, and
  the save outcome is announced through a live region — never by color alone.
- Charts are wrapped in a `<figure>` with an `sr-only` `<figcaption>` that
  summarizes the series, and Recharts' `accessibilityLayer` enables keyboard
  navigation across data points.
- The table carries a screen-reader caption and a scoped header row.
- Dates are exposed as `<time dateTime="…">`.
- The two series colors are defined in `globals.css` with distinct hues and
  enough contrast against the card background. The default shadcn/ui neutral
  palette is grayscale, which is not usable for two-series charts.

## Deferred from the frontend standard

The project follows `docs/standards/next-standards.md` progressively. Still
pending: Storybook, Playwright, TanStack Query, Zustand, runtime i18n and dark
mode. The structure already reserves the places they will live in
(`e2e/playwright/`, `src/components/ui/`) so adopting them is additive.
