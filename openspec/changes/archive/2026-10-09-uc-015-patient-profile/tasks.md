## 1. Contract and persistence foundation

- [x] 1.1 Confirm the existing consultation-close, agent-operation, Markdown document, and migration boundaries against `design.md`, and record the selected patient-profile directory and document format in the implementation code; verify the planned paths do not overlap `data/events/` or `data/encounters/`.
- [x] 1.2 Add the canonical patient-profile model and serializer for the seven profile fields, explicit absence, derived age, and current-value replacement; verify unit tests cover complete, partial, unknown, and corrected profiles.
- [x] 1.3 Add the profile repository/atomic publication boundary using the existing Markdown source-of-truth conventions; verify repository tests prove failed publication leaves the previous snapshot unchanged.
- [x] 1.4 No database migration or derived profile index is required; verify migration startup and rebuild tests pass and existing event/measurement migrations remain compatible.

## 2. Consultation and agent behavior

- [x] 2.1 Add a bounded profile operation to the agent contract and route it through the existing operation executor; verify operation-routing tests reject unsupported or unconfirmed profile updates.
- [x] 2.2 Extend consultation state to collect confirmed profile candidates independently from clinical events and measurements; verify mixed-turn tests keep profile candidates separate from clinical persistence.
- [x] 2.3 Implement validation and selection of the last confirmed value per field, preserving prior values for ambiguous or unresolved candidates; verify tests cover corrections, contradictions, unknown values, and omitted fields.
- [x] 2.4 Integrate profile publication into consultation close as one retryable update after validation; verify service tests cover successful close, no-op close, and failed publication without partial profile changes.
- [x] 2.5 Update the Spanish assistant prompts and response handling for optional, resumable interview behavior; verify prompt/agent tests cover first proposal, skip, resume, missing-field questions, clarification, and non-blocking continuation.

## 3. Backend API and documentation

- [x] 3.1 Confirm that no independent profile API is required: the existing chat message and consultation-close endpoints are the application surface, while profile reads remain an internal agent-context concern and profile writes remain close-time service behavior; verify the existing controller contract remains unchanged.
- [x] 3.2 Document the resulting backend contract and profile persistence behavior in `backend/README.md` and the applicable architecture/data-model references; verify documented paths, payloads, and error behavior match the implemented tests.

## 4. Frontend consultation experience

- [x] 4.1 Extend the chat state and server-action payload validation to represent profile interview, skip, resume, clarification, and retry states; verify Vitest/React Testing Library tests cover each state without making the interview mandatory.
- [x] 4.2 Render Spanish profile prompts and recoverable update errors using existing chat UI conventions; verify component tests show confirmed values, absent values, and clarification messages without exposing internal errors.
- [x] 4.3 Preserve existing clinical-event and measurement flows while profile data is present in the same conversation; verify regression tests cover mixed profile, event, and measurement turns.

## 5. End-to-end verification

- [x] 5.1 Add an end-to-end scenario for a first interaction that skips and later resumes the profile interview; verify the conversation remains usable and only confirmed fields are persisted.
- [x] 5.2 Add an end-to-end scenario for correction, ambiguity, unknown data, and consultation-close failure/retry; verify the final profile contains only the latest confirmed values and no partial failed update.
- [x] 5.3 Run the backend test and coverage gates from `backend/` and verify existing consultation, event, measurement, and failure suites remain green.
- [x] 5.4 Run frontend lint, typecheck, unit tests, and relevant Playwright tests from `frontend/`; verify no new diagnostics or regressions are introduced.
- [x] 5.5 Update the UC-015 acceptance-criteria traceability with the implemented test references and verify every A1-A10 and E1-E2 criterion has executable coverage.
