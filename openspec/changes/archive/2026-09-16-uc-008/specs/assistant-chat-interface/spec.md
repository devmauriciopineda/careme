## MODIFIED Requirements

### Requirement: Represent clarification, success, duplicate, and failure states

The interface MUST render each backend outcome distinctly enough for the user to understand whether an event was registered, whether a question was answered, whether no records were found, whether clarification is required, whether nothing was registered, or whether retry is available. It MUST preserve the failed message text for retry, MUST NOT display technical error details, and MUST word a failure from a named failure code rather than from a transport error. For a no-records outcome, the interface MUST show the absence reason reported by the backend and MUST offer the actionable continuation the backend reported, keeping that outcome distinguishable from an answer and from an error.

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
