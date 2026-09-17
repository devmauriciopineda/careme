## MODIFIED Requirements

### Requirement: Produce provider-neutral clinical intents

The adapter MUST send the user message together with the current reference date and applicable timezone context to the configured provider. It MUST map the provider response to one of `EVENTS`, `QUERY`, `CLARIFICATION`, or `CONVERSATION`. Event candidates MUST contain only the supported type, user-expressed content, date when known, date precision, and original date text when present. A `QUERY` intent MUST contain the question the user asked and MUST NOT contain event candidates. The adapter MUST classify as a clinical history question every message in which any part depends on the clinical history, including a colloquial reformulation of a recorded fact, and MUST NOT classify such a message as `CONVERSATION`. When a message also contains a general part, the adapter MUST preserve that general part in the intent so it can be answered as conversation instead of being searched or ignored. When the adapter cannot determine whether the message depends on the clinical history, it MUST return `CLARIFICATION` and MUST NOT guess a channel.

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

#### Scenario: Ask for clarification when the channel is undetermined
- **WHEN** the provider cannot determine whether the message depends on the user's clinical history
- **THEN** the adapter returns a `CLARIFICATION` intent with a Spanish question
- **AND** it does not return a `QUERY` intent and does not return a `CONVERSATION` intent

#### Scenario: Keep a colloquial question in the clinical history channel
- **WHEN** the message asks about something the user has recorded, using colloquial words
- **THEN** the adapter returns a `QUERY` intent
- **AND** it does not return a `CONVERSATION` intent

#### Scenario: Map a mixed message to the clinical history channel
- **WHEN** the message contains a general part and a part that depends on the clinical history
- **THEN** the adapter returns a `QUERY` intent containing the part that depends on the clinical history
- **AND** it preserves the general part in the intent, outside the query's search terms
- **AND** the general part is not used as a retrieval criterion

#### Scenario: Map general conversation
- **WHEN** the provider determines that the message is not a registrable clinical event and does not depend on the clinical history
- **THEN** the adapter returns a `CONVERSATION` intent with no event candidates
- **AND** it does not return a `QUERY` intent

## ADDED Requirements

### Requirement: Compose conversational replies outside the clinical history

The adapter MUST compose the conversational Spanish reply for a message that neither registers a clinical event nor depends on the clinical history, and MUST compose it only from the message and the active conversation's recent turns. It MUST NOT retrieve, read, or cite clinical events, MUST NOT present any retrieved event as support, and MUST NOT use the user's recorded data to answer. It MUST NOT introduce facts about the user's clinical history and MUST NOT apply a general explanation to the user's own case. When the user asks for a diagnosis, a treatment recommendation, or an interpretation of their own case, the composed reply MUST decline and MUST state that the assistant does not diagnose and does not recommend treatment. A failure to compose MUST be reported as a controlled integration failure and MUST NOT be worked around with an answer taken from the clinical history or from the model's own assumptions about the user.

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
