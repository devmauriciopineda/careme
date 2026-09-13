## Context

The existing backend already validates `ClinicalEventIntent`, normalizes dates, writes clinical events as Markdown, updates a derived index, and tracks conversation-scoped duplicate fingerprints. It has no public chat endpoint, LLM provider adapter, turn orchestrator, or frontend chat surface. The root page currently belongs to the measurement workflow.

The design follows the proposal and the delta specs. UC-004 remains the domain contract; this change adds the missing transport and interpretation boundary.

## Goals / Non-Goals

**Goals:**

- Provide one non-streaming backend chat operation for the MVP.
- Keep the LLM provider replaceable and unable to write clinical storage directly.
- Convert provider output into the existing provider-neutral intent model before registration.
- Keep conversation turns and pending clarifications in memory only.
- Make retries idempotent and preserve atomic Markdown/index publication.
- Replace the root measurement landing surface with a minimal accessible chat while retaining measurements as a secondary route.

**Non-Goals:**

- Persisting conversation transcripts or pending clarifications.
- User authentication, multiple patients, roles, or third-party access.
- Clinical-event search, retrieval-augmented generation, editing, deletion, or event inspection.
- Streaming responses, attachments, audio, or image interpretation.
- Medical diagnosis, recommendations, or disease detection.

## Decisions

### Backend owns orchestration

The frontend calls `POST /api/v1/chat/messages`. A backend orchestrator validates the request, resolves the in-memory conversation state, calls an `ClinicalIntentInterpreter` provider port, validates the returned intent, invokes `ClinicalEventRegistrationService`, and translates the result into the public response. This keeps provider details out of controllers and keeps persistence out of the LLM adapter.

Alternative rejected: calling the provider from Next.js. It would expose credentials, duplicate policy, and allow provider output to bypass backend invariants.

### Provider-neutral adapter port

Define a backend port that accepts message text, conversation context needed for the current turn, reference date, and timezone, then returns `ClinicalEventIntent`. The initial implementation uses a configurable OpenAI-compatible HTTP client with strict structured JSON output and a versioned prompt resource. A deterministic fake adapter remains available for tests and local development through configuration.

Alternative rejected: binding the domain to provider SDK response classes. That would make provider changes leak into clinical validation and tests.

### Ephemeral state with bounded keys

Use a bounded in-memory store keyed by `conversationId`. Each state contains pending clarification context, conversation fingerprints, and message outcomes needed for idempotent retries. Apply a maximum age and size limit; evict expired conversations. Restart clears this state by design. Persisted Markdown events are unaffected.

Alternative rejected: persisting chat turns. It conflicts with the MVP decision and introduces retention, deletion, and privacy requirements unrelated to UC-004.

### Public response envelope

Use explicit JSON fields: `conversationId`, `messageId`, `status`, `message`, and optional registered event summaries. The status vocabulary is fixed by the spec. Internal intent JSON, prompt text, provider metadata, and stack traces never cross the API boundary.

### Markdown plus derived PostgreSQL index

Reuse the existing event publication services. Markdown remains canonical; PostgreSQL only supports derived search/index operations. Registration must not report success until both publication steps complete, and reconciliation/reindexing repairs a derived-index mismatch.

### Frontend client boundary

Add a typed chat client and chat components under the existing frontend conventions. The root route becomes chat; measurement navigation remains available. Keep API errors normalized into user-safe Spanish messages and keep the original text in local component state for retry.

## Risks / Trade-offs

- **[Provider output is unsafe or malformed]** -> enforce structured output, schema validation, domain validation, and a failure result before persistence.
- **[In-memory state disappears]** -> show a new-turn behavior after restart and document that only successfully registered events survive.
- **[Provider receives sensitive clinical text]** -> configure credentials only in backend, minimize prompts, redact logs, select approved provider/region, and document privacy decisions before production use.
- **[Duplicate HTTP retries]** -> key outcomes by conversation and message identifiers and test repeated requests against persistence mocks.
- **[Markdown/index divergence]** -> use atomic publication, startup reconciliation, explicit reindex operation, and failure tests.
- **[Replacing the root route regresses measurements]** -> retain the measurements route and run the existing frontend/backend measurement suites.

## Migration Plan

1. Add the provider port, fake adapter, DTOs, bounded conversation store, orchestrator, controller, and tests while leaving measurement endpoints unchanged.
2. Add provider configuration with safe local defaults that use the fake adapter; require an explicit backend credential and provider URL for real LLM mode.
3. Add the frontend chat route and retain a visible route to measurements.
4. Run contract, integration, and regression suites; verify no provider credential is bundled into frontend output.
5. Enable the real provider only after privacy, region, model, logging, and operational limits are approved.
6. Rollback by selecting the measurement route as root and disabling the chat provider; existing Markdown events and measurement behavior remain intact.
