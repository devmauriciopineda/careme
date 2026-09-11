# Careme — Frontend

Body tracking dashboard. It shows daily weight and abdominal circumference as
trend charts and as a table, reading them from the Careme backend API.

This is the MVP: no authentication and no write operations. Measurements come
from `GET /api/v1/measurements` on the backend, through a service layer that
validates the payload before it reaches the UI.

## Tech stack

- Next.js 16 (App Router, Server Components by default)
- React 19 + TypeScript 5 in strict mode
- Tailwind CSS v4 + shadcn/ui components
- Recharts through the shadcn/ui `chart` component
- Native server-side `fetch` with Zod validating the API response
- Vitest + React Testing Library
- pnpm

## Getting started

```bash
pnpm install
pnpm dev
```

The app is served at http://localhost:3000. It needs the backend running on
`http://localhost:8080` (or `API_BASE_URL` set to wherever it runs); otherwise it
shows the error screen. See the [root README](../README.md) for the backend.

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
│   ├── app/                      # layout, page, error boundary, global styles
│   ├── components/ui/            # shadcn/ui primitives
│   ├── features/measurements/    # feature module
│   │   ├── components/           # dashboard, chart and table
│   │   └── lib/                  # pure logic, schema, copy
│   ├── services/                 # API client and base URL configuration
│   └── test/                     # test setup
├── components.json               # shadcn/ui configuration
├── next.config.ts                # standalone output for containers
├── tsconfig.json
└── vitest.config.mts
```

## Architecture notes

**Server Components first.** `src/app/page.tsx` renders `MeasurementsDashboard`, an
async Server Component that loads the measurements and derives every chart
series, axis domain and date label on the server. `MetricTrendChart` is the only
client island, because Recharts needs the browser; it receives ready-to-render
primitives, never `Date` objects or formatting logic. This is why date labels do
not shift with the visitor's time zone.

**One chart component, two usages.** `MetricTrendChart` is parameterized by
metric instead of being duplicated per metric.

**Metric registry.** `src/features/measurements/lib/metrics.ts` holds the
`METRICS` map (label, unit, decimals, color token, axis padding) and all pure
helpers. Adding a metric means adding a field to `Measurement`, a value to
`MetricKey` and an entry to `METRICS`.

**Service boundary.** `src/services/measurementService.ts` is the only place that
knows where data comes from. It calls `GET /api/v1/measurements` with
`cache: "no-store"`, unwraps the response envelope, validates `data` with Zod and
returns chronologically sorted measurements. The base URL is read once in
`src/services/apiConfig.ts`.

**Failure handling.** When that call fails, the Server Component throws and
`src/app/error.tsx` renders a localized message with a retry button instead of the
framework's error screen.

**Copy.** Code is written in English; only user-facing strings are Spanish, and
they all live in `src/features/measurements/lib/strings.ts`.

## Data source and environment

The dashboard reads from the backend API. Copy `.env.example` to `.env.local` and
set the base URL when the backend does not run on `http://localhost:8080`:

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
pnpm test
```

30 tests across 4 files. Unit tests cover the pure helpers in `lib/metrics.ts`
(sorting, series building, axis domain, localization) and the Zod schema. An
integration test renders `MeasurementsTable` with React Testing Library and
asserts row order, number formatting and the accessible table caption. The
service tests mock `fetch` and cover the success path, an error status, a failed
envelope and a malformed measurement.

## Accessibility

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
