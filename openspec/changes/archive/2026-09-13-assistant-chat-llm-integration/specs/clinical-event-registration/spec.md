## ADDED Requirements

### Requirement: Register clinical events through the conversation boundary

The existing clinical-event registration rules MUST remain authoritative when invoked by the conversation flow. A chat outcome MAY request registration only after the structured intent has passed deterministic validation. Markdown in `data/events/` MUST remain the source of truth, PostgreSQL MUST remain a derived searchable index, and publication MUST be atomic from the user's perspective.

#### Scenario: Register through the chat flow
- **WHEN** a valid conversation produces an `EVENTS` intent
- **THEN** the system invokes the existing clinical registration behavior
- **AND** the response confirms the registered event without exposing internal storage details

#### Scenario: Preserve atomic publication
- **WHEN** Markdown publication or derived-index update fails
- **THEN** the chat response is `failed`
- **AND** no partial event is visible in Markdown or through the derived index

#### Scenario: Rebuild the derived index
- **WHEN** the PostgreSQL index is empty or inconsistent with the Markdown event documents
- **THEN** the backend can rebuild the index from Markdown without treating PostgreSQL as the source of truth
