# assistant-conversation Specification

## Purpose

Define the provider-independent conversation contract that lets the application receive chat messages, coordinate clinical registration, and return deterministic outcomes without persisting conversation history.

## Requirements

### Requirement: Process chat messages through a bounded conversation

The system MUST expose a non-streaming JSON chat operation that accepts a user message, an idempotent message identifier, and an optional conversation identifier. When no conversation identifier is supplied, the system MUST create one and return it. The system MUST keep pending clarification state and duplicate detection only for the active in-memory conversation.

#### Scenario: Start a conversation and register an event
- **WHEN** the client sends a valid message with a new message identifier and no conversation identifier
- **THEN** the system creates a conversation identifier, processes the message, and returns the identifier with the registration result
- **AND** the clinical event is persisted as Markdown and made available through the derived index

#### Scenario: Continue a conversation after clarification
- **WHEN** the system has requested clarification and the client sends the next message with the same conversation identifier
- **THEN** the system correlates the answer with the pending clarification and processes the combined clinical intent
- **AND** the system does not require persisted conversation history

#### Scenario: Conversation state is lost after restart
- **WHEN** the backend restarts while a conversation has a pending clarification
- **THEN** the pending clarification is discarded
- **AND** the next message is processed as a new turn without corrupting persisted clinical events

### Requirement: Return explicit chat outcomes

The system MUST return exactly one observable outcome for each accepted message: `registered`, `clarification_required`, `general_conversation`, `duplicate`, or `failed`. Each response MUST include a user-facing Spanish message and MUST NOT expose provider prompts, credentials, stack traces, or internal persistence details.

#### Scenario: Return a registration outcome
- **WHEN** the clinical registration completes successfully
- **THEN** the response status is `registered` and includes a concise Spanish confirmation

#### Scenario: Return a clarification outcome
- **WHEN** the message could describe a clinical event but lacks sufficient information
- **THEN** the response status is `clarification_required` and includes a Spanish clarification question
- **AND** no clinical event is persisted

#### Scenario: Return a general conversation outcome
- **WHEN** the message contains no registrable clinical event
- **THEN** the response status is `general_conversation` and includes the assistant reply
- **AND** no clinical event is persisted

#### Scenario: Return a failure outcome
- **WHEN** the provider, contract validation, or event publication fails
- **THEN** the response status is `failed` with a retryable Spanish message
- **AND** no partial clinical event is visible

### Requirement: Make message retries idempotent

The system MUST process a given `(conversationId, messageId)` at most once for side effects. Repeating the same request MUST return the original outcome without creating another clinical event or invoking a second persistence operation.

#### Scenario: Retry a completed message
- **WHEN** the client repeats the same message with the same conversation and message identifiers
- **THEN** the system returns the original outcome
- **AND** the number and contents of persisted events remain unchanged

#### Scenario: Reject an invalid chat request
- **WHEN** the message is blank, exceeds the configured size limit, or has an invalid identifier
- **THEN** the system returns a client error with a Spanish validation message
- **AND** it does not call the LLM or modify clinical persistence
