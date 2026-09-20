# assistant-chat-interface Specification

## Purpose

Provide a focused and accessible chat surface for the assistant, including message submission, conversation continuity, clarification, retry, and failure states without adding clinical search or event inspection views.

## Requirements

### Requirement: Send and display assistant turns

The interface MUST display the conversation messages in order, allow the user to enter a non-empty message, submit it to the backend, and show the returned Spanish assistant message. While a request is pending, the interface MUST prevent accidental duplicate submission and expose an accessible busy state. The interface MUST reach the backend from the server: the browser MUST NOT resolve, hold, or send the API address.

#### Scenario: Submit a message
- **WHEN** the user enters a valid message and submits it
- **THEN** the interface adds the user message, sends it with the current conversation and message identifiers, and renders the assistant response in order

#### Scenario: Show request progress
- **WHEN** a chat request is pending
- **THEN** the interface exposes a loading/busy state to assistive technologies
- **AND** the send action cannot create a second request for the same turn

#### Scenario: Send a turn without exposing the API address
- **WHEN** the browser sends a chat turn
- **THEN** the request leaves from the server side of the application
- **AND** no request from the browser targets the backend API address

### Requirement: Represent clarification, success, duplicate, and failure states

The interface MUST render each backend outcome distinctly enough for the user to understand whether an event was registered, whether a question was answered, whether no records were found, whether clarification is required, whether nothing was registered, or whether retry is available. It MUST preserve the failed message text for retry, MUST NOT display technical error details, and MUST word a failure from a named failure code rather than from a transport error. For a no-records outcome, the interface MUST show the absence reason reported by the backend and MUST offer the actionable continuation the backend reported, keeping that outcome distinguishable from an answer and from an error. For a general-conversation outcome, the interface MUST present the reply as conversation and keep it distinguishable from an answer grounded in the clinical history, from an absence, and from an error, without showing supporting events or an absence reason. La respuesta que devuelve el backend es una, y la interfaz MUST NOT partirla en distintas clases de respuesta. When the backend reports more than one operation for a turn, the interface MUST show every completed operation with its own kind of result, in the order the backend reported them, and MUST show an operation that did not complete distinctly from the completed ones, so the user can tell what was registered from what could not be completed. The interface MUST NOT present a turn with an operation that did not complete as fully successful.

#### Scenario: Continue after clarification
- **WHEN** the backend requests clarification
- **THEN** the interface shows the question and lets the user send the answer in the same conversation

#### Scenario: Show an answered turn
- **WHEN** the backend answers a question about the clinical history
- **THEN** the interface shows the Spanish answer
- **AND** it distinguishes this outcome from a registration

#### Scenario: Show that no records were found
- **WHEN** the backend reports that no records support the question
- **THEN** the interface shows that outcome distinctly from an answer and from an error
- **AND** it shows the absence reason the backend reported
- **AND** it offers the actionable continuation the backend reported

#### Scenario: Show a partially supported answer
- **WHEN** the backend answers part of a question and reports another part without records
- **THEN** the interface shows the supported part with the events that support it
- **AND** it shows the part of the question that has no records
- **AND** it does not present the unsupported part as an answer

#### Scenario: Show a general conversation turn
- **WHEN** the backend answers as general conversation
- **THEN** the interface shows the conversational reply and marks the turn as conversation
- **AND** it distinguishes the turn from an answer grounded in the clinical history, from an absence, and from an error
- **AND** it shows no supporting events and no absence reason for that turn

#### Scenario: Show a declined request
- **WHEN** the backend declines a request for a diagnosis, a recommendation, or an interpretation
- **THEN** the interface shows the refusal as conversation
- **AND** it does not present the refusal as an answer grounded in the clinical history
- **AND** it offers no clinical advice of its own

#### Scenario: Show a turn that mixes conversation and clinical history
- **WHEN** the message mixes a part that does not depend on the clinical history with a part that does
- **THEN** the interface shows the single answer the backend returned
- **AND** it does not present a separate conversational part
- **AND** it shows supporting events only for the part that depends on the clinical history

#### Scenario: Show a turn that registered a fact and answered a question
- **WHEN** the backend reports a registration and an answer in the same turn
- **THEN** the interface shows the confirmation of the registered fact and the answer with its supporting events in the same turn
- **AND** it presents both as parts of one response, not as two separate turns
- **AND** it distinguishes the registration from the answer

#### Scenario: Show a turn with an operation that did not complete
- **WHEN** the backend reports a completed operation and another one that did not complete
- **THEN** the interface shows the completed operation with its own result
- **AND** it shows the operation that did not complete distinctly, as a retryable failure
- **AND** it does not present the turn as fully successful
- **AND** it preserves the message text for retry

#### Scenario: Retry a failed request
- **WHEN** the backend reports a retryable failure
- **THEN** the interface keeps the original message available and provides a retry action
- **AND** the retry uses a new message identifier unless the backend contract explicitly identifies the previous request as safely repeatable

#### Scenario: Use the interface with keyboard and assistive technology
- **WHEN** the user navigates the chat without a pointer or with a screen reader
- **THEN** the input, submit action, messages, busy state, and errors have accessible names and announcements

#### Scenario: Word a failure without transport detail
- **WHEN** a turn fails because the backend is unreachable or answers with something the application cannot read
- **THEN** the interface shows a Spanish message that states the failure without naming the transport cause
- **AND** it offers the retry action with the original message preserved

### Requirement: Show the events that support an answer

When an answer is grounded in clinical events, the interface MUST show the events that support it, using the references the backend returns and the temporal precision it reported, so the user can verify the answer without exposing prompts, credentials, or internal persistence details.

#### Scenario: Display the supporting events
- **WHEN** the backend returns an answer with supporting clinical events
- **THEN** the interface shows those events alongside the answer
- **AND** it shows each date with the precision the backend reported

#### Scenario: Keep the answer verifiable without technical detail
- **WHEN** the interface shows the supporting events
- **THEN** it shows only the events and references the user can verify
- **AND** it does not expose prompts, credentials, stack traces, or internal persistence details

### Requirement: End the consultation from the chat surface

The interface MUST let the user end the consultation in progress from the chat surface and MUST reflect that the consultation has been closed. Once a consultation is closed the interface MUST NOT offer to continue it and MUST let the user start a new one. The interface MUST show the close outcome reported by the backend — the facts that were registered, or that no facts were registered — distinctly enough for the user to tell a close that registered facts from one that did not, and from a close that failed. The interface MUST NOT expose the consultation identifier, its summary, or its motive.

#### Scenario: End the consultation

- **WHEN** the user ends the consultation in progress
- **THEN** the interface sends the close request for the current conversation
- **AND** renders the Spanish close outcome reported by the backend
- **AND** exposes a busy state while the request is pending, without creating a second request

#### Scenario: Show the closed consultation

- **WHEN** the backend reports that the consultation was closed
- **THEN** the interface shows that the consultation is closed
- **AND** does not offer to continue it
- **AND** lets the user start a new consultation

#### Scenario: Close a consultation that collected no clinical facts

- **WHEN** the close outcome reports that no facts were registered
- **THEN** the interface shows that outcome distinctly from an error

#### Scenario: Keep the consultation internals out of the surface

- **WHEN** the interface renders any consultation state
- **THEN** it does not show the consultation identifier, the summary, or the motive
- **AND** it does not display technical error details
