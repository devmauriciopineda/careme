## MODIFIED Requirements

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

The interface MUST render each backend outcome distinctly enough for the user to understand whether an event was registered, whether clarification is required, whether nothing was registered, or whether retry is available. It MUST preserve the failed message text for retry, MUST NOT display technical error details, and MUST word a failure from a named failure code rather than from a transport error.

#### Scenario: Continue after clarification
- **WHEN** the backend requests clarification
- **THEN** the interface shows the question and lets the user send the answer in the same conversation

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
