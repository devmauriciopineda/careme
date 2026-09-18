## Why

Careme permite registrar eventos clínicos y consultarlos mediante preguntas,
pero todavía no ofrece una forma directa de revisar qué quedó guardado. Sin una
vista de inspección, la persona no puede verificar de manera rápida el conjunto
de eventos persistidos, revisar un detalle concreto ni localizar registros por
tipo o periodo.

## What Changes

- Add a read-only clinical-event inspection experience for the single user's
  history.
- Show both the medical occurrence date and the record date for every registered
  event when available.
- Order events by record date from newest to oldest by default, with an
  alternative order by medical occurrence date from newest to oldest.
- Place events without an occurrence date after dated events when ordering by
  medical occurrence date.
- Allow the person to filter the list by event type and by period without
  modifying the history.
- Allow the person to open an event and inspect its recorded content and
  temporal precision.
- Distinguish an empty history from a failed retrieval and provide a retryable
  failure outcome without exposing internal details.
- Display relative dates such as "hace dos semanas" as an approximate estimated
  date calculated from the record's save date; do not display the original
  relative expression in the inspection view.
- Keep the entire capability read-only: it must never create, complete, correct,
  edit, delete, export, share, or print clinical events.

## Capabilities

### New Capabilities

- `clinical-event-inspection`: Directly list, filter, order, and inspect the
  details of the user's persisted clinical events without changing them.

### Modified Capabilities

- None.

## Impact

- Backend: add a read-only event-list/detail service and an HTTP contract for
  retrieving events, applying type and period filters, and reporting empty or
  failed outcomes. The existing Markdown event documents remain the source of
  truth and PostgreSQL remains a derived index.
- Frontend: add a dedicated clinical-event inspection view separate from the
  natural-language chat and body-measurement tracking views, with Spanish empty,
  failure, filtering, ordering, detail, and approximate-date states.
- Data model: reuse persisted clinical-event metadata, including occurrence date
  and record date, plus the save date needed to estimate relative dates; no write
  behavior or authentication is introduced.
- Tests and documentation: add focused backend and frontend coverage for
  ordering, filters, detail fidelity, empty history, retrieval failure,
  relative-date display, and the read-only boundary.