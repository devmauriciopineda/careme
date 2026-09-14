## MODIFIED Requirements

### Requirement: Represent clarification, success, duplicate, and failure states

The interface MUST render each backend outcome distinctly enough for the user to understand whether an event was registered, whether a question was answered, whether no records were found, whether clarification is required, whether nothing was registered, or whether retry is available. It MUST preserve the failed message text for retry, MUST NOT display technical error details, and MUST word a failure from a named failure code rather than from a transport error.

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

## ADDED Requirements

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
