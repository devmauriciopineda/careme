# Careme — Frontend

Body tracking dashboard. It shows daily weight and abdominal circumference as
trend charts and as a table.

This is the MVP: **frontend only**, no authentication and no backend. The data
comes from a local mock dataset behind a service layer that is already shaped
like an API client, so swapping in real endpoints does not touch the UI.

## Tech stack

- Next.js 16 (App Router, Server Components by default)
- React 19 + TypeScript 5 in strict mode
- Tailwind CSS v4 + shadcn/ui components
- Recharts through the shadcn/ui `chart` component
- Zod for boundary validation
- Vitest + React Testing Library
- pnpm

## Getting started

```bash
pnpm install
pnpm dev
```

The app is served at http://localhost:3000.

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
├── app/                          # Next.js App Router
├── e2e/playwright/               # Reserved for end-to-end tests
├── src/
│   ├── app/                      # layout, page, global styles
│   ├── components/ui/            # shadcn/ui primitives
│   ├── features/measurements/    # feature module
│   │   ├── components/           # dashboard, chart and table
│   │   ├── data/                 # mock dataset
│   │   └── lib/                  # pure logic, schema, copy
│   ├── services/                 # data access boundary
│   └── test/                     # test setup
├── components.json               # shadcn/ui configuration
├── tsconfig.json
└── vitest.config.mts
```

## Architecture notes

**Server Components first.** `app/page.tsx` renders `MeasurementsDashboard`, an
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

**Service boundary.** `src/services/measurementService.ts` is the only place
that knows where data comes from. It validates the payload with Zod and returns
chronologically sorted measurements.

**Copy.** Code is written in English; only user-facing strings are Spanish, and
they all live in `src/features/measurements/lib/strings.ts`.

## Replacing the mock with the API

1. Set `NEXT_PUBLIC_API_BASE_URL` (see `.env.example`).
2. Replace the body of `measurementService.getMeasurements` with a `fetch` of
   `/measurements`. Keep the Zod validation and the sort.
3. Nothing else changes: the signature is already `Promise<Measurement[]>` and
   consumers do not care where the data came from.

When client-side caching and invalidation are needed, wrap that same method in
TanStack Query instead of rewriting the data flow.

## Testing

```bash
pnpm test
```

Unit tests cover the pure helpers in `lib/metrics.ts` (sorting, series
building, axis domain, localization) and the Zod schema. An integration test
renders `MeasurementsTable` with React Testing Library and asserts row order,
number formatting and the accessible table caption.

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
