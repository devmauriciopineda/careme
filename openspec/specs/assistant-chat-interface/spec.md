# assistant-chat-interface Specification

## Purpose

Provide a focused and accessible chat surface for the assistant, including message submission, conversation continuity, clarification, retry, and failure states without adding clinical search or event inspection views.

## Requirements

### Requirement: Send and display assistant turns

The interface MUST display the conversation messages in order, allow the user to enter a non-empty message, submit it to the backend, and show the returned Spanish assistant message. While a request is pending, the interface MUST prevent accidental duplicate submission and expose an accessible busy state.

#### Scenario: Submit a message
- **WHEN** the user enters a valid message and submits it
- **THEN** the interface adds the user message, sends it with the current conversation and message identifiers, and renders the assistant response in order

#### Scenario: Show request progress
- **WHEN** a chat request is pending
- **THEN** the interface exposes a loading/busy state to assistive technologies
- **AND** the send action cannot create a second request for the same turn

### Requirement: Represent clarification, success, duplicate, and failure states

The interface MUST render each backend outcome distinctly enough for the user to understand whether an event was registered, whether clarification is required, whether nothing was registered, or whether retry is available. It MUST preserve the failed message text for retry and MUST not display technical error details.

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
