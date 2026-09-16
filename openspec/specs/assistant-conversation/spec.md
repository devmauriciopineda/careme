# assistant-conversation Specification

## Purpose

Define the provider-independent conversation contract that lets the application receive chat messages, coordinate clinical registration, and return deterministic outcomes without persisting conversation history.

## Requirements

### Requirement: Process chat messages through a bounded conversation

The system MUST expose a non-streaming JSON chat operation that accepts a user message, an idempotent message identifier, and an optional conversation identifier. When no conversation identifier is supplied, the system MUST create one and return it. The system MUST keep pending clarification state, duplicate detection, and a bounded buffer of the most recent turns only for the active in-memory conversation, and MUST NOT persist conversation history.

#### Scenario: Start a conversation and register an event
- **WHEN** the client sends a valid message with a new message identifier and no conversation identifier
- **THEN** the system creates a conversation identifier, processes the message, and returns the identifier with the registration result
- **AND** the clinical event is persisted as Markdown and made available through the derived index

#### Scenario: Continue a conversation after clarification
- **WHEN** the system has requested clarification and the client sends the next message with the same conversation identifier
- **THEN** the system correlates the answer with the pending clarification and processes the combined clinical intent
- **AND** the system does not require persisted conversation history

#### Scenario: Answer a follow-up question from recent turns
- **WHEN** the client sends, in the same conversation, a follow-up question that depends on the recent turns
- **THEN** the system interprets the turn together with the bounded buffer of recent turns
- **AND** it answers from the clinical history, not from the buffer
- **AND** it does not require persisted conversation history

#### Scenario: Conversation state is lost after restart
- **WHEN** the backend restarts while a conversation has a pending clarification
- **THEN** the pending clarification is discarded
- **AND** the next message is processed as a new turn without corrupting persisted clinical events

#### Scenario: Discard the ephemeral buffer after an interruption
- **WHEN** the conversation is interrupted before the answer completes
- **THEN** the ephemeral conversation state is discarded
- **AND** the clinical history remains unchanged
- **AND** the next turn is processed as a new turn

### Requirement: Return explicit chat outcomes

The system MUST return exactly one observable outcome for each accepted message: `registered`, `answered`, `no_records`, `clarification_required`, `general_conversation`, `duplicate`, or `failed`. Each response MUST include a user-facing Spanish message and MUST NOT expose provider prompts, credentials, stack traces, or internal persistence details. For an `answered` outcome, the response MUST include the references to the clinical events that support the answer. For a `no_records` outcome, the response MUST include the reason the absence applies and the actionable continuation offered to the user, so a client can present both without reinterpreting the Spanish text.

#### Scenario: Return a registration outcome
- **WHEN** the clinical registration completes successfully
- **THEN** the response status is `registered` and includes a concise Spanish confirmation

#### Scenario: Return an answered outcome
- **WHEN** a question about the clinical history is answered from registered events
- **THEN** the response status is `answered` and includes the Spanish answer
- **AND** it includes the references to the clinical events that support the answer

#### Scenario: Return a no-records outcome
- **WHEN** a question about the clinical history has no supporting registered events
- **THEN** the response status is `no_records` and the Spanish message states that no records were found
- **AND** it identifies which absence reason applies: an empty history, no events of the questioned type, no events in the questioned period, or no events matching the terms used
- **AND** it includes the actionable continuation offered to the user
- **AND** no clinical event is presented as support

#### Scenario: Return a partial answer outcome
- **WHEN** part of a question about the clinical history is supported by registered events and part is not
- **THEN** the response answers the supported part and includes the references to the events it relies on
- **AND** it declares the part of the question that has no records
- **AND** no clinical event is presented as support for the unsupported part

#### Scenario: Keep absence out of the failed outcome
- **WHEN** the search cannot be completed, so no absence can be verified
- **THEN** the response status is `failed` with a retryable Spanish message
- **AND** it is not returned as `no_records`

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

### Requirement: Allow the declared origin to reach the operations a browser client uses

The API MUST accept a cross-origin preflight from every configured browser origin for the HTTP verbs those clients use, so a browser client is not rejected before its request is read. The API MUST keep every other verb closed for the same mapping.

#### Scenario: Accept a preflight for a browser write
- **WHEN** a browser client at a configured origin sends a preflight for a write operation it uses
- **THEN** the API answers it successfully
- **AND** the answer names that origin and that verb

#### Scenario: Keep unused verbs closed
- **WHEN** a browser client asks for a verb outside the exposed set
- **THEN** the API does not allow it
