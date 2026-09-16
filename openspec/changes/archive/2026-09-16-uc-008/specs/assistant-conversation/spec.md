## MODIFIED Requirements

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
