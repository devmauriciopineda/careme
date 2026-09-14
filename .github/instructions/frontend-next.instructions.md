---
description: "Use when writing or modifying Next.js/React/TypeScript code, configuration or scripts under frontend/. Covers rendering model, data fetching, validation and styling conventions."
applyTo: frontend/src/**/*.ts, frontend/src/**/*.tsx, frontend/e2e/**/*.ts, frontend/e2e/**/*.tsx, frontend/package.json, frontend/next.config.ts, frontend/vitest.config.mts, frontend/tsconfig.json
---

# Frontend (Next.js / React / TypeScript) — Careme

Follow the full standard: `docs/standards/next-standards.md`. This file only highlights what is
specific to this repository.

## Rendering and data

- App Router with Server Components as the default. Fetch on the server at request time in the
  Server Component that needs the data; derive series, axis domains and labels there too.
- Server Actions for mutations; revalidate the affected path so charts and tables refresh.
- `'use client'` only for interactivity (Recharts, forms, the chat workspace) — keep islands small.

## Service layer and validation

- Call the backend through the service layer in `src/services/`; do not call `fetch` from components.
- Validate every external payload with Zod before it reaches a component; treat a failed parse as
  a handled error (route-level error screen), not a crash.

## Conventions

- TypeScript strict; no `any` in new code.
- Tailwind CSS v4 + shadcn/ui + lucide-react; compose existing components before adding new ones.
- Spanish user-facing strings; localized date/number formatting that does not shift with the
  visitor's time zone.
- pnpm only (never npm/yarn). Scripts: `pnpm dev`, `pnpm build`, `pnpm test`, `pnpm lint`, `pnpm typecheck`.
- The backend contract is documented in `backend/README.md`; the frontend types mirror it.
