## MODIFIED Requirements

### Requirement: Return explicit chat outcomes

The system MUST return exactly one observable outcome for each accepted message: `registered`, `answered`, `no_records`, `clarification_required`, `general_conversation`, `duplicate`, or `failed`. Each response MUST include a user-facing Spanish message and MUST NOT expose provider prompts, credentials, stack traces, or internal persistence details. For an `answered` outcome, the response MUST include the references to the clinical events that support the answer. For a `no_records` outcome, the response MUST include the reason the absence applies and the actionable continuation offered to the user, so a client can present both without reinterpreting the Spanish text. For a `general_conversation` outcome, the response MUST carry the conversational Spanish reply composed for that message, MUST NOT carry references to clinical events, and MUST NOT present any part of the reply as a fact recorded in the clinical history. For a `clarification_required` outcome the response MUST include a Spanish clarification question, and the system MUST also return it when it cannot determine whether the message depends on the clinical history. When a message contains both a general part and a part that depends on the clinical history, the response MUST carry the general part in a separate field from the outcome of the history part, so a client can present the two as different kinds of answer.

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

#### Scenario: Return a clarification outcome for an undetermined channel
- **WHEN** the message may or may not depend on the clinical history and the system cannot determine which
- **THEN** the response status is `clarification_required` and includes a Spanish clarification question
- **AND** the system assumes no interpretation of the message
- **AND** it does not read the clinical history

#### Scenario: Return a general conversation outcome
- **WHEN** the message contains no registrable clinical event and does not depend on the clinical history
- **THEN** the response status is `general_conversation` and includes the conversational Spanish reply for that message
- **AND** the reply addresses the message instead of acknowledging it
- **AND** no clinical event is persisted

#### Scenario: Return a failure outcome
- **WHEN** the provider, contract validation, or event publication fails
- **THEN** the response status is `failed` with a retryable Spanish message
- **AND** no partial clinical event is visible

#### Scenario: Keep the general part of a mixed message separate from the history outcome
- **WHEN** the message contains a general part and a part that depends on the clinical history
- **THEN** the response carries the general part in its own field and the outcome of the history part in its own state
- **AND** a client can present the general part as conversation and the history outcome as a grounded result without reinterpreting either text

## ADDED Requirements

### Requirement: Answer general conversation without consulting the clinical history

The system MUST compose a conversational Spanish reply for every message that neither registers a clinical event nor depends on the clinical history, MUST address what the message asks instead of acknowledging it, and MUST NOT read, search, or cite the clinical history while composing it. The system MUST NOT modify the clinical history in this outcome. A general question MUST be answered, a general medical concept MUST be explained without applying it to the user's own case, and a greeting, farewell, or courtesy MUST be answered briefly. The system MUST NOT present any part of the reply as a fact recorded in the clinical history, MUST NOT attribute the reply to the history, and MUST NOT attach clinical events to it. When the reply cannot be composed, the system MUST return the `failed` outcome with a retryable Spanish message and MUST preserve the message for retry.

#### Scenario: Answer a question that does not depend on the clinical history
- **WHEN** the user sends a question that does not depend on the clinical history
- **THEN** the response status is `general_conversation` and the Spanish reply answers the question as conversation
- **AND** the system does not read the clinical history
- **AND** the response carries no clinical events and no absence reason
- **AND** the clinical history remains exactly as it was

#### Scenario: Explain a general medical concept without applying it
- **WHEN** the user asks for a general explanation of a medical concept
- **THEN** the reply explains the concept in general terms
- **AND** it does not apply the explanation to the user's own case
- **AND** it does not interpret the user's data
- **AND** it does not recommend a treatment

#### Scenario: Answer a greeting or a courtesy
- **WHEN** the message is a greeting, a farewell, or a courtesy with no clinical content
- **THEN** the reply is brief and conversational
- **AND** the system does not read the clinical history
- **AND** the reply does not mention clinical records

#### Scenario: Answer without treating conversation facts as records
- **WHEN** the user has mentioned facts about themselves in the active conversation and now asks something that does not depend on the clinical history
- **THEN** the reply answers as general conversation
- **AND** it does not treat those facts as clinical records
- **AND** it does not read the clinical history

#### Scenario: Keep the general conversation path free of clinical reads
- **WHEN** the system answers as general conversation
- **THEN** no read of the derived clinical index or of the stored clinical documents is performed for that turn
- **AND** no clinical event is created, changed, or removed

### Requirement: Decline requests for diagnosis, recommendation or interpretation

The system MUST decline, explicitly and in Spanish, any request for a diagnosis, a treatment recommendation, or an interpretation of the user's own case. The reply MUST state that the assistant does not diagnose and does not recommend treatment. The system MUST NOT replace the request with an invented answer, with general knowledge presented as advice for the user, or with an answer grounded in the clinical history, and MUST NOT modify the clinical history while declining.

#### Scenario: Decline a request for a recommendation
- **WHEN** the user asks the assistant to recommend a treatment or to say what to do about their case
- **THEN** the reply declines the request and states that the assistant does not diagnose and does not recommend treatment
- **AND** the reply is understandable in Spanish
- **AND** the clinical history remains unchanged

#### Scenario: Decline a request for an interpretation of the user's case
- **WHEN** the user asks the assistant to interpret their own data or situation
- **THEN** the reply declines the interpretation
- **AND** it does not substitute the request with an invented answer
- **AND** it does not answer from the clinical history

#### Scenario: Keep the declination out of the grounded answer path
- **WHEN** the system declines a request
- **THEN** the response status is `general_conversation`
- **AND** the response carries no references to clinical events
- **AND** no clinical event is presented as support

### Requirement: Separate the general part of a mixed message

When a message contains both a general part and a part that depends on the clinical history, the system MUST answer the general part as conversation and MUST handle the history part through the clinical history outcome. The system MUST NOT blend the two parts into a single statement, MUST NOT use the general part to answer the history part, and MUST NOT use the history part, its events, or its absence to answer the general part. The general part MUST NOT be presented as support of an answer about the clinical history.

#### Scenario: Answer both parts of a mixed message
- **WHEN** the message contains a general question and a question that depends on the clinical history
- **THEN** the response carries the general part as conversation and the history part through its own outcome
- **AND** the two parts are not presented in a single statement
- **AND** the clinical history remains unchanged

#### Scenario: Do not fill an absence with the general part
- **WHEN** the history part of a mixed message has no supporting registered events
- **THEN** the response declares the absence with its reason and keeps the general part separate from that declaration
- **AND** the general conversation reply does not supply the missing clinical information
- **AND** no clinical event is presented as support

#### Scenario: Do not present the general part as support of a grounded answer
- **WHEN** the history part of a mixed message is answered from registered events
- **THEN** the response includes only those events as support
- **AND** the general part is not presented as support of that answer
