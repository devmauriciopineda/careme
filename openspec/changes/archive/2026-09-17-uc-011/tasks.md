## 1. Read-side domain and date presentation

- [x] 1.1 Define the inspection read model and response mapping for event code, type, occurrence date, record date, precision, and approximate-date display; verify unit tests preserve both user-visible dates without exposing Markdown or persistence details.
- [x] 1.2 Implement default ordering by record date descending and alternate ordering by occurrence date descending, with deterministic temporal and code tie-breakers; verify tests cover equal dates, mixed dated/undated events, both criteria, and repeated list requests.
- [x] 1.3 Implement relative-date estimation from the normalized occurrence date and its stored record date (`createdAt`), marking the display as approximate and omitting the original relative expression; verify tests cover “hace dos semanas” and date-boundary cases.

## 2. Backend inspection service and contract

- [x] 2.1 Add a read-only inspection service over `ClinicalEventMarkdownStore` that lists all events and reads one event by code, returning controlled empty and unavailable outcomes; verify service tests prove no registration or index-write interaction.
- [x] 2.2 Add type and inclusive period filters plus the order criterion to the inspection service while applying the selected ordering after filtering; verify tests cover each filter independently, combined filters, both ordering criteria, default ordering, and filters with no matches.
- [x] 2.3 Add the clinical-events list/detail HTTP contract with validated optional filters, order criterion, occurrence date, record date, and Spanish-safe error responses; verify MockMvc tests cover default and alternate ordering, successful lists, empty lists, detail success, unavailable detail, invalid filters, and unexpected read failure.
- [x] 2.4 Ensure inspection failures return no partial event data and do not mutate Markdown or the derived index; verify an integration test snapshots event files and index rows before and after a forced read failure.
- [x] 2.5 Document the new read contract and its empty, filtering, approximate-date, detail, and failure behavior in `backend/README.md`; verify the documented examples match MockMvc response fixtures.

## 3. Frontend inspection feature

- [x] 3.1 Add the frontend service, types, and Zod schemas for list/detail responses, filters, approximate dates, empty states, unavailable events, and retryable failures; verify unit tests reject malformed or incomplete payloads.
- [x] 3.2 Add a dedicated clinical-event inspection route and feature components separate from chat and measurements; verify the route renders the loading, list, empty, and failure states with Spanish user-facing text.
- [x] 3.3 Render event rows with type, code, occurrence date, record date, and approximate-date labeling, using record-date order by default and placing undated occurrence dates last when that criterion is selected; verify component tests assert both visible dates, both ordering modes, and that relative expressions are absent.
- [x] 3.4 Add type, period, and order-criterion controls and preserve active filters and ordering when opening and closing event detail; verify component tests cover applying, removing, combining, switching criteria, and retaining state.
- [x] 3.5 Add read-only event detail presentation with a return action and unavailable-event state; verify component tests show recorded content and temporal precision without edit or delete controls.
- [x] 3.6 Add an accessible retry action for failed list/detail reads and distinguish empty history from failure; verify component tests cover accessible names, busy/error announcements, and retry behavior.

## 4. Integrated verification and documentation

- [x] 4.1 Add backend integration coverage for the full inspection flow using persisted Markdown events, including both dates, default record-date ordering, alternate occurrence-date ordering, filters, detail fidelity, relative-date display, empty history, and read-only invariants; verify it passes with the project Maven test command.
- [x] 4.2 Add frontend integration or end-to-end coverage for entering the inspection view, filtering, opening detail, returning with filters, empty history, and retrying a failure; verify it passes with the project pnpm test command.
- [x] 4.3 Run backend `mvnw verify` and frontend `pnpm test`, `pnpm lint`, and `pnpm typecheck`; verify the coverage gate and existing chat, registration, and measurement suites remain green.
- [x] 4.4 Update `docs/roadmap/use-cases.md` to mark UC-011 as implemented only after the feature is complete and link the use case and acceptance criteria; verify the progress counter and status table are consistent.
- [x] 4.5 Run `openspec validate "uc-011" --strict`; verify all change artifacts and capability requirements validate successfully.

> Coverage note: the repository-wide JaCoCo deficit predates UC-011 and is deferred to a separate change; it does not block this change's task status.