## MODIFIED Requirements

### Requirement: Validate provider responses before side effects

The adapter MUST reject a response it cannot read, an operation outside the set it declared to the provider, an unsupported event type, a candidate missing a required field, and a composed answer that cites a clinical event outside the retrieved set. A rejected response MUST never reach clinical persistence and MUST never be presented as a grounded answer.

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

### Requirement: Compose conversational replies outside the clinical history

The adapter MUST compose the conversational Spanish reply for a message that neither registers a clinical event nor depends on the clinical history, and MUST compose it only from the message and the active conversation's recent turns. That reply is part of the turn's single response: the system MUST NOT deliver it as a second message or as a separate part of the answer. It MUST NOT retrieve, read, or cite clinical events, MUST NOT present any retrieved event as support, and MUST NOT use the user's recorded data to answer. It MUST NOT introduce facts about the user's clinical history and MUST NOT apply a general explanation to the user's own case. When the user asks for a diagnosis, a treatment recommendation, or an interpretation of their own case, the composed reply MUST decline and MUST state that the assistant does not diagnose and does not recommend treatment. A failure to compose MUST be reported as a controlled integration failure and MUST NOT be worked around with an answer taken from the clinical history or from the model's own assumptions about the user.

#### Scenario: Compose a reply for a general message
- **WHEN** the adapter composes a reply for a message that does not depend on the clinical history
- **THEN** it uses only the message and the recent turns of the active conversation
- **AND** it reports no clinical event references
- **AND** it does not retrieve clinical events

#### Scenario: Decline inside the composed reply
- **WHEN** the user asks for a diagnosis, a treatment recommendation, or an interpretation of their own case
- **THEN** the composed reply declines the request
- **AND** it states that the assistant does not diagnose and does not recommend treatment
- **AND** it does not answer from the user's clinical history

#### Scenario: Report a failure to compose
- **WHEN** the provider is unavailable or the response cannot be read
- **THEN** the adapter reports a controlled integration failure
- **AND** the system returns `failed` with a retryable Spanish message
- **AND** the message is preserved so the user can try again
- **AND** the clinical history is not read
