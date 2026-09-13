## 1. Backend conversation contract

- [x] 1.1 Add request/response DTOs for `POST /api/v1/chat/messages`, including `conversationId`, `messageId`, `message`, fixed outcome statuses, Spanish user message, and optional event summaries; verify JSON serialization with controller tests.
- [x] 1.2 Add request validation for blank/oversized messages and invalid identifiers; verify invalid requests return a Spanish 4xx response without invoking the interpreter.
- [x] 1.3 Add bounded in-memory conversation state for pending clarification, fingerprints, and message outcomes with expiry and size limits; verify duplicate requests are side-effect free and restart semantics are documented/tested.
- [x] 1.4 Add the conversation orchestrator that coordinates validation, state, interpretation, registration, and public outcome translation; verify every outcome maps to exactly one response status.
- [x] 1.5 Add the chat controller and exception handling without changing measurement endpoints; verify the HTTP contract with MockMvc tests.

## 2. LLM intent adapter

- [x] 2.1 Define the provider-neutral interpreter port and provider request context containing message, reference date, timezone, and prompt version; verify it maps to the existing `ClinicalEventIntent` model.
- [x] 2.2 Add strict provider-response parsing and validation for events, clarification, conversation, unsupported kinds, missing fields, and mixed responses; verify malformed payloads never reach registration.
- [x] 2.3 Add a deterministic fake interpreter for tests/local mode; verify the message matrix covers event, multi-event, approximate/relative/unknown date, suspicion, clarification, and general conversation.
- [x] 2.4 Add the configurable OpenAI-compatible HTTP interpreter with backend-only credentials, timeout, safe error mapping, and versioned prompt resource; verify timeout, unavailable provider, and invalid JSON produce controlled failures.
- [x] 2.5 Add configuration documentation and defaults for fake versus real provider mode; verify frontend build output contains no provider credential or provider URL secret.

## 3. Clinical event integration

- [x] 3.1 Connect valid `EVENTS` intents to the existing Markdown-backed registration service while preserving deterministic validation and conversation-scoped deduplication; verify UC-004 existing tests remain green.
- [x] 3.2 Verify atomic failure behavior across Markdown publication and derived PostgreSQL index update; add regression tests proving no partial event is visible.
- [x] 3.3 Add or verify startup/reindex reconciliation from `data/events/` to the derived PostgreSQL index; verify Markdown remains authoritative when the index is empty or inconsistent.
- [x] 3.4 Extend UC-004 acceptance coverage for provider failure, invalid structured response, clarification continuation, idempotent retry, and in-memory state loss after restart; verify no duplicate or partial events.

## 4. Frontend chat interface

- [x] 4.1 Add a typed chat API client that creates message identifiers, propagates `conversationId`, and normalizes API errors; verify request/response fixtures match the backend contract.
- [x] 4.2 Replace the root measurement surface with an accessible chat while retaining a clear route to measurements; verify keyboard navigation and existing measurement route behavior.
- [x] 4.3 Render ordered user/assistant messages, busy state, clarification, registration, duplicate, general conversation, and retryable failure states; verify component tests cover each backend outcome.
- [x] 4.4 Preserve failed input for retry and prevent accidental duplicate submission; verify retries use the intended idempotency behavior.

## 5. Security and verification

- [x] 5.1 Redact clinical message content and provider credentials from backend logs while retaining correlation identifiers; verify logging tests or inspection show no sensitive payloads.
- [x] 5.2 Document provider, model, region, retention, rate/size limits, and no-diagnosis boundary in the backend and project documentation; verify configuration names and runtime behavior agree.
- [x] 5.3 Run backend tests, frontend tests, lint/type checks, and OpenSpec strict validation; verify measurement behavior remains unchanged and the full chat message matrix passes.
