---
description: "Use when writing or modifying frontend unit, component or end-to-end tests. Covers Vitest, React Testing Library and Playwright conventions."
applyTo: frontend/src/**/*.test.ts, frontend/src/**/*.test.tsx, frontend/src/test/**, frontend/e2e/playwright/**
---

# Frontend tests — Careme

Stack: Vitest + React Testing Library (unit/component), Playwright (end-to-end).

- Test behavior through the DOM (queries by role/label), not implementation details.
- Prefer Server-Component-friendly testing: exercise the derived data and the rendered output;
  mock the service layer, not the framework.
- User-facing assertions use the Spanish strings the UI actually renders.
- Run `pnpm test` for the suite (use `pnpm test:watch` while iterating) and `pnpm typecheck`
  before considering work done. End-to-end specs live under `frontend/e2e/playwright/`.
