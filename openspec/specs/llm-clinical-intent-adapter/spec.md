# llm-clinical-intent-adapter Specification

## Purpose

Define the structured, provider-neutral boundary that converts natural-language messages into validated clinical intents while preventing the language model from writing directly to clinical persistence.

## Requirements

### Requirement: Produce provider-neutral clinical intents

The adapter MUST send the user message together with the current reference date and applicable timezone context to the configured provider. It MUST map the provider response to one of `EVENTS`, `QUERY`, `CLARIFICATION`, or `CONVERSATION`. Event candidates MUST contain only the supported type, user-expressed content, date when known, date precision, and original date text when present. A `QUERY` intent MUST contain the question the user asked and MUST NOT contain event candidates.

#### Scenario: Map one or more event candidates
- **WHEN** the provider returns valid structured JSON containing clinical events
- **THEN** the adapter returns an `EVENTS` intent with one candidate per distinct event
- **AND** the persistence layer receives the intent only after application validation

#### Scenario: Preserve temporal uncertainty
- **WHEN** the provider identifies an approximate, relative, or unknown date
- **THEN** the adapter preserves the date precision and original date expression
- **AND** it does not manufacture an exact date from missing information

#### Scenario: Map a query
- **WHEN** the provider determines that the message asks about the user's clinical history
- **THEN** the adapter returns a `QUERY` intent containing the question
- **AND** it returns no event candidates

#### Scenario: Map a clarification
- **WHEN** the provider determines that a possible event needs more information
- **THEN** the adapter returns a `CLARIFICATION` intent with a Spanish question
- **AND** it returns no event candidates

#### Scenario: Map general conversation
- **WHEN** the provider determines that the message is not a registrable clinical event
- **THEN** the adapter returns a `CONVERSATION` intent with no event candidates

### Requirement: Validate provider responses before side effects

The adapter MUST reject malformed JSON, unknown intent kinds, unsupported event types, missing required candidate fields, responses containing both an answer and event candidates, and composed answers that cite a clinical event outside the retrieved set. A rejected response MUST never reach clinical persistence and MUST never be presented as a grounded answer.

#### Scenario: Provider returns invalid JSON
- **WHEN** the provider response cannot be parsed or does not match the structured schema
- **THEN** the adapter reports a controlled integration failure
- **AND** the system returns `failed` without persisting an event

#### Scenario: Provider times out or is unavailable
- **WHEN** the provider does not respond within the configured timeout or returns an unavailable error
- **THEN** the system returns `failed` with a retryable Spanish message
- **AND** the provider credential and internal error details remain hidden

#### Scenario: Provider cites an event outside the retrieved set
- **WHEN** the provider composes an answer that references a clinical event the adapter did not retrieve
- **THEN** the adapter rejects the response
- **AND** the system does not present that answer as grounded

### Requirement: Compose grounded answers from retrieved events

The adapter MUST compose the Spanish answer from the retrieved clinical events it receives, MUST use only their content and temporal precision, MUST NOT introduce facts, dates, or interpretations absent from that set, and MUST report the references of the events the answer relies on.

#### Scenario: Compose from retrieved events
- **WHEN** the adapter receives a question and the set of clinical events retrieved for it
- **THEN** it returns a Spanish answer that uses only those events
- **AND** it reports the references of the events the answer relies on

#### Scenario: Preserve temporal precision in the answer
- **WHEN** a retrieved event carries an approximate or unknown date
- **THEN** the composed answer keeps that precision and does not state an exact date

#### Scenario: No retrieved events
- **WHEN** the set of retrieved events is empty
- **THEN** the adapter reports that there is nothing to answer with
- **AND** it does not fall back to general knowledge or assumptions

### Requirement: Keep provider access behind the backend

The provider credential MUST be loaded only by the backend. The frontend MUST never receive the credential or call the provider directly. Prompts, model identifiers, and provider responses MUST be versioned or correlated in internal telemetry without logging complete clinical message content.

#### Scenario: Browser sends a chat message
- **WHEN** the frontend submits a chat message
- **THEN** the request targets the backend chat endpoint
- **AND** no provider credential is present in the request or browser configuration
