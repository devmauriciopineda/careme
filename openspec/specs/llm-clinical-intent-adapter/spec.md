# llm-clinical-intent-adapter Specification

## Purpose

Define the structured, provider-neutral boundary that converts natural-language messages into validated clinical intents while preventing the language model from writing directly to clinical persistence.

## Requirements

### Requirement: Produce provider-neutral clinical intents

The adapter MUST send the user message together with the current reference date, the applicable timezone
context, the recent turns of the active conversation, and the set of operations the assistant is allowed
to request. The set MUST contain only consulting the clinical history, consulting the measurement
tracking, collecting clinical-fact notes and collecting measurement notes. The
provider MUST choose which of those operations it needs and MAY request more than one within the same
turn, and MAY use the result of one operation to decide the next. The adapter MUST execute every requested
operation through the application, which validates it before producing any effect, and MUST NOT let the
provider reach clinical persistence, the measurement tracking, the filesystem, or any operation outside
the set. The adapter MUST
continue until the provider produces the user-facing response or an operation does not complete. Event
candidates MUST contain only the supported type, user-expressed content, date when known, date precision,
and original date text when present. Measurement candidates MUST contain the metric the user expressed,
its value or values, the unit when the user stated it, and the date with its precision, and MUST NOT
contain a clinical-event type. A candidate that carries a measurement MUST NOT be treated as a clinical
fact: the adapter MUST route a message that contains a measurement through the measurement channel and
MUST NOT offer it as a clinical-fact candidate. A history-consulting operation MUST contain the question
the user asked and MUST NOT contain event candidates. A measurement-consulting operation MUST contain the
question the user asked, and MUST NOT contain event candidates or measurement candidates. It MUST be
read-only: it MUST return the retrieved measurements with their metric, their values, their reference unit
and their date, and MUST NOT create, change or remove any measurement. A measurement-collecting operation
MUST NOT write
to the tracking: measurements are incorporated when the consultation closes. The adapter MUST treat as a clinical history question every
message in which any part depends on the clinical history, including a colloquial reformulation of a
recorded fact, and MUST NOT answer such a message as general conversation. When a message also contains a
general part, the adapter MUST preserve that general part so it can be answered as conversation instead of
being searched or ignored. When the adapter cannot determine whether the message depends on the clinical
history, it MUST ask for clarification and MUST NOT guess a channel.

#### Scenario: Map one or more event candidates
- **WHEN** the provider requests a registration operation with valid structured content
- **THEN** the application validates the operation and registers one event per distinct fact
- **AND** the provider never reaches clinical persistence directly

#### Scenario: Map a measurement candidate
- **WHEN** the provider requests a measurement-collecting operation with a metric, its value or values and a date
- **THEN** the application collects it as a measurement of the active consultation
- **AND** the provider never reaches the measurement tracking directly

#### Scenario: Map a measurement query
- **WHEN** the provider requests a measurement-consulting operation with the question
- **THEN** the application retrieves the matching measurements of the tracking and returns them to the provider
- **AND** the operation returns no event candidates and no measurement candidates
- **AND** the tracking remains exactly as it was

#### Scenario: Route a measurement message through the measurement channel
- **WHEN** a message contains a measurement
- **THEN** the adapter routes it through the measurement channel
- **AND** it does not offer the measurement as a clinical-fact candidate
- **AND** it does not register the measurement as a clinical event

#### Scenario: Preserve temporal uncertainty
- **WHEN** the provider identifies an approximate, relative, or unknown date
- **THEN** the adapter preserves the date precision and original date expression
- **AND** it does not manufacture an exact date from missing information

#### Scenario: Map a query
- **WHEN** the provider requests a history-consulting operation
- **THEN** the application retrieves the matching events of the clinical history and returns them to the provider
- **AND** the operation returns no event candidates

#### Scenario: Consult the clinical history more than once in a turn
- **WHEN** the provider requests a second consultation within the same turn to answer the message
- **THEN** the adapter executes it and returns its result to the provider
- **AND** the person still receives a single response

#### Scenario: Map a clarification
- **WHEN** a possible event needs more information to be registered
- **THEN** the adapter asks the person for the missing information in Spanish
- **AND** it registers nothing for that operation

#### Scenario: Ask for clarification when the channel is undetermined
- **WHEN** the provider cannot determine whether the message depends on the user's clinical history
- **THEN** the adapter asks for clarification in Spanish
- **AND** it does not consult the clinical history and does not answer as general conversation

#### Scenario: Keep a colloquial question in the clinical history channel
- **WHEN** the message asks about something the user has recorded, using colloquial words
- **THEN** the provider consults the clinical history
- **AND** it does not answer as general conversation

#### Scenario: Map a mixed message to the clinical history channel
- **WHEN** the message contains a general part and a part that depends on the clinical history
- **THEN** the consultation uses only the part that depends on the clinical history
- **AND** the part that does not depend on it is addressed inside the single response
- **AND** that part is not used as a retrieval criterion

#### Scenario: Map general conversation
- **WHEN** the message is not a registrable clinical fact and does not depend on the clinical history
- **THEN** the adapter composes the conversational reply from the message and the recent turns
- **AND** no consultation of the clinical history is performed
- **AND** no clinical event is created, changed, or removed

#### Scenario: Refuse an operation outside the available set
- **WHEN** the provider requests an operation that is not in the set offered to it
- **THEN** the adapter does not execute it
- **AND** it does not fall back to an operation that can approximate it

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

### Requirement: Compose grounded answers from retrieved events

The adapter MUST compose the Spanish answer from the retrieved clinical events it receives, MUST use only their content and temporal precision, MUST NOT introduce facts, dates, or interpretations absent from that set, and MUST report the references of the events the answer relies on.

The adapter MUST also report how much of the question the retrieved events support: the whole question, part of it, or none of it. When the retrieved set supports only part of the question, the adapter MUST answer the supported part and MUST report the part of the question it could not support. When the retrieved set does not support the question at all, the adapter MUST report that the events do not answer it, MUST NOT present any of them as support, and MUST NOT compose an answer from them. Deciding this is the adapter's job: the caller MUST NOT treat a question as answered merely because retrieval returned events.

#### Scenario: Compose from retrieved events
- **WHEN** the adapter receives a question and the set of clinical events retrieved for it
- **THEN** it returns a Spanish answer that uses only those events
- **AND** it reports the references of the events the answer relies on
- **AND** it reports that the retrieved events support the whole question

#### Scenario: Preserve temporal precision in the answer
- **WHEN** a retrieved event carries an approximate or unknown date
- **THEN** the composed answer keeps that precision and does not state an exact date

#### Scenario: No retrieved events
- **WHEN** the set of retrieved events is empty
- **THEN** the adapter reports that there is nothing to answer with
- **AND** it does not fall back to general knowledge or assumptions

#### Scenario: Retrieved events support only part of the question
- **WHEN** the retrieved events answer part of the question and leave another part unsupported
- **THEN** the adapter composes the answer for the supported part
- **AND** it reports that the retrieved events support the question only in part
- **AND** it reports the part of the question that the retrieved events do not support
- **AND** it does not fill the unsupported part with general knowledge, assumptions, or events outside the retrieved set

#### Scenario: Retrieved events do not support the question
- **WHEN** the retrieved events do not answer the question
- **THEN** the adapter reports that the retrieved events support none of the question
- **AND** it reports no references, because no event supports the answer
- **AND** it does not compose an answer from those events
- **AND** it does not fall back to general knowledge or assumptions

### Requirement: Compose grounded answers from retrieved measurements

The adapter MUST compose the Spanish answer for a measurement query using only the measurements the
application retrieved, MUST present each value in the reference unit of its metric and MUST state that
unit explicitly, MUST preserve each value and each date as registered without rounding, completing or
altering them beyond a change of unit, and MUST present a compound metric as a single measurement with
its values. The adapter MUST report the references of the measurements the answer relies on. It MUST NOT
introduce values, dates or interpretations absent from the retrieved set, MUST NOT interpret or assess
the values, and MUST NOT recommend anything about them. When the person did not specify which metric they
ask about, the adapter MUST ask which one and MUST NOT assume it. When the retrieved set is empty, the
adapter MUST report that the measurement does not appear in the tracking and MUST NOT answer with an
apparent response. When the question names a metric the system does not admit, the adapter MUST report
that the metric is not part of the tracking and MUST NOT invent values for it. When the question asks for
an interpretation or a recommendation, the adapter MUST decline explicitly and MUST offer the registered
values instead. A failure to retrieve MUST be reported as a controlled integration failure and MUST NOT be
presented as an absence of measurements.

#### Scenario: Compose from retrieved measurements
- **WHEN** the adapter receives a measurement query and the set of measurements retrieved for it
- **THEN** it returns a Spanish answer that uses only those measurements
- **AND** it reports the references of the measurements the answer relies on
- **AND** it presents each value in the reference unit of its metric and states that unit explicitly

#### Scenario: Preserve value and date as registered
- **WHEN** a retrieved measurement carries a value and a date
- **THEN** the composed answer keeps them as registered
- **AND** it does not round, complete or alter them beyond a change of unit

#### Scenario: Present a compound metric as one measurement
- **WHEN** the retrieved measurements include a compound metric such as blood pressure
- **THEN** the composed answer presents it with its values as a single measurement
- **AND** it does not present it as separate measurements

#### Scenario: Ask which metric when the question does not specify one
- **WHEN** the question asks about measurements without specifying which metric
- **THEN** the adapter asks which metric in Spanish
- **AND** it does not assume a metric and does not compose an answer from an assumed one

#### Scenario: Declare an absence of measurements
- **WHEN** the set of measurements retrieved for the question is empty
- **THEN** the adapter reports that the measurement does not appear in the tracking
- **AND** it does not answer with an apparent response and does not fall back to general knowledge

#### Scenario: Report a metric that is not part of the tracking
- **WHEN** the question names a metric the system does not admit
- **THEN** the adapter reports that the metric is not part of the tracking
- **AND** it does not invent values for it and does not substitute another metric

#### Scenario: Decline an interpretation of the values
- **WHEN** the question asks for an interpretation of the values or a recommendation about them
- **THEN** the adapter declines explicitly and states that it does not interpret and does not recommend
- **AND** it offers the registered values instead of an invented interpretation

#### Scenario: Report a retrieval failure without presenting it as an absence
- **WHEN** the measurement retrieval does not complete
- **THEN** the adapter reports a controlled integration failure with a retryable Spanish message
- **AND** it does not present the failure as an absence of measurements
- **AND** internal error details remain hidden

### Requirement: Keep provider access behind the backend

The provider credential MUST be loaded only by the backend. The frontend MUST never receive the credential or call the provider directly. Prompts, model identifiers, and provider responses MUST be versioned or correlated in internal telemetry without logging complete clinical message content.

#### Scenario: Browser sends a chat message
- **WHEN** the frontend submits a chat message
- **THEN** the request targets the backend chat endpoint
- **AND** no provider credential is present in the request or browser configuration

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

### Requirement: Select the assistant provider with the real assistant by default

El sistema MUST utilizar el asistente real como comportamiento por defecto, sin que nadie tenga que
activarlo. Un asistente simulado MAY seleccionarse únicamente para desarrollo y pruebas y MUST NOT
sustituir en silencio al asistente real en operación. Cuando el asistente real no esté disponible o no
esté configurado, el sistema MUST devolver el resultado `failed` con un mensaje recuperable en español,
MUST NOT ejecutar ninguna operación y MUST NOT componer la respuesta por otro medio.

#### Scenario: Use the real assistant without activation

- **WHEN** el sistema atiende un mensaje en operación
- **THEN** lo atiende el asistente real
- **AND** no se requiere una activación manual de nadie para ello

#### Scenario: Keep the simulated assistant out of operation

- **WHEN** el asistente real no está disponible o no está configurado
- **THEN** el sistema devuelve un fallo recuperable en español
- **AND** no degrada en silencio al asistente simulado
- **AND** no presenta la respuesta del asistente simulado como si fuera la del asistente real

#### Scenario: Keep the simulated assistant available for development and testing

- **WHEN** el sistema se ejecuta para desarrollo o para pruebas automatizadas
- **THEN** el asistente simulado puede seleccionarse sin acceso a la red
- **AND** esa selección no afecta al comportamiento por defecto en operación
