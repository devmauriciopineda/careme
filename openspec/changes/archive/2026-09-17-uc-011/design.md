## Context

See `proposal.md` for the motivation and `specs/clinical-event-inspection/spec.md`
for the observable contract.

The backend already has a `ClinicalEvent` domain record containing the event
code, type, content, occurrence date, date precision, original date text and
`createdAt`, which is the record date.
`ClinicalEventMarkdownStore` can read the Markdown source of truth as one event
or as a collection, while `ClinicalEventQueryRepository` already supports
read-side access to the derived PostgreSQL index for chat questions. The
existing HTTP surface exposes index rebuilding, but not a user-facing event
list or detail query.

The frontend has separate feature areas for chat and measurements. The new
inspection surface should remain a distinct feature and use the existing
server-side service boundary, schema validation and UI primitives.

## Goals / Non-Goals

**Goals:**

- Add a read-only read model for listing and inspecting persisted clinical events.
- Make default record-date ordering, alternate occurrence-date ordering, type
  filtering, period filtering and undated-event placement deterministic.
- Present relative dates as approximate estimated dates derived from `createdAt`.
- Keep empty history, unavailable event and retrieval failure observable as
  different outcomes.
- Cover backend contract, frontend presentation and the no-write boundary with
  automated tests.

**Non-Goals:**

- Change event registration, Markdown persistence, index rebuilding or chat
  question answering semantics.
- Add editing, deletion, correction, completion, export, sharing or printing.
- Include body measurements in the clinical-event list.
- Add authentication or multiple-patient selection.

## Decisions

### Use the Markdown source of truth for inspection, with a read-only adapter

The inspection service will read events through a dedicated read-side adapter
that can use the existing Markdown store for complete event fidelity. The
derived index may remain useful for filtering, but the response must ultimately
represent the persisted event content and temporal metadata.

**Why:** Markdown is the declared source of truth and contains the complete
event body, while the index is rebuildable. Reusing registration writes or
exposing the index writer would violate the read-only boundary.

**Alternative rejected:** use only the chat query path. Chat retrieval is
question-oriented and returns only events relevant to a natural-language
question; it cannot provide a complete deterministic list or direct detail
lookup.

### Expose a dedicated read contract, separate from chat

Add a user-facing collection/detail contract under the clinical-events resource,
with optional type and date-period filters. Empty results are successful and
distinct from a failed retrieval. The response carries Spanish-safe data for
the frontend to format without exposing persistence details.

**Why:** inspection has a different goal and lifecycle from chat. A dedicated
contract makes the no-write behavior and deterministic filtering testable without
changing the chat response states.

**Alternative rejected:** encode inspection as a special natural-language chat
question. That would make ordering, filters, pagination-free listing and error
states dependent on intent interpretation and would blur UC-007's boundary.

### Expose both temporal dimensions and default to record-date ordering

The read-side mapping will expose the medical occurrence date separately from
the record date (`createdAt`). The default list order will be record date
descending, with an explicit alternate occurrence-date descending order. When
the selected occurrence-date criterion has equal or missing values, the service
will use the other available temporal value and then the event code as a stable
tiebreaker. Unknown occurrence dates sort after dated events when that criterion
is selected.

The mapping will derive a display date for relative events from the stored
`createdAt` reference and the normalized occurrence date, mark it approximate,
and omit the original relative expression from the inspection response.

**Why:** record date answers when information entered the history and is the
most useful default for reviewing newly captured information. Occurrence date
remains necessary for clinical chronology, so the user can choose it explicitly.
Keeping both dimensions visible prevents confusing when an event happened with
when it was recorded.

**Alternative rejected:** expose only one date or always sort by occurrence date.
That would hide the distinction between clinical time and record time and make
newly recorded events harder to review.

### Keep filters in the query state and preserve them across detail navigation

The frontend will own the selected type and period filters as view state. Opening
an event detail and returning to the list will reuse that state, while the
backend applies the same constraints to every list request.

**Why:** filters are presentation choices, not clinical data, and preserving them
supports the stated inspection workflow without persisting conversation state.

**Alternative rejected:** persist filters in the backend or in the clinical
history. That adds state unrelated to the user’s medical facts and creates no
business value for the MVP.

### Treat failures as explicit retryable read outcomes

The backend will map read failures to the existing API error envelope without
returning partial event data. The frontend will show a Spanish retry action and
will keep empty history as a successful empty state.

**Why:** a blank list cannot safely communicate whether no events exist or the
read failed. The distinction is required by the use case and protects against
presenting incomplete history as complete.

## Risks / Trade-offs

- **Relative-date estimation depends on the stored creation timestamp.** -> Use
  `createdAt` as the fixed reference, mark the result approximate, and test the
  calculation around date boundaries.
- **Markdown reads can fail or contain malformed event documents.** -> Return a
  controlled failure without partial results, log technical details server-side,
  and keep the user-facing message free of internals.
- **The derived index and Markdown source can temporarily disagree.** -> Treat
  Markdown as authoritative for detail fidelity and keep index use read-only;
  document reconciliation through the existing reindex operation rather than
  adding repair behavior to inspection.
- **Large histories may make a complete list expensive.** -> Keep the first MVP
  contract bounded to the existing event set and deterministic filters/order
  criteria; defer
  pagination until measured need appears.

## Migration Plan

1. Add the read contract and service behind the new inspection view while leaving
   registration and chat contracts unchanged.
2. Deploy the backend and frontend together; no database migration is expected
   because the event metadata already exists in Markdown and the derived index.
3. Verify list ordering, filters, relative-date display, empty state, failure
   retry and zero write interactions in backend and frontend tests.
4. Roll back by removing the inspection route and client feature; existing event
   registration, chat queries and stored data remain usable because this change
   adds no destructive operation.