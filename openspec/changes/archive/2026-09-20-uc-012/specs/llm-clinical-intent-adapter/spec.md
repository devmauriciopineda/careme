## MODIFIED Requirements

### Requirement: Produce provider-neutral clinical intents

The adapter MUST send the user message together with the current reference date, the applicable timezone
context, the recent turns of the active conversation, and the set of operations the assistant is allowed
to request. The set MUST contain only consulting the clinical history and registering clinical facts. The
provider MUST choose which of those operations it needs and MAY request more than one within the same
turn, and MAY use the result of one operation to decide the next. The adapter MUST execute every requested
operation through the application, which validates it before producing any effect, and MUST NOT let the
provider reach clinical persistence, the filesystem, or any operation outside the set. The adapter MUST
continue until the provider produces the user-facing response or an operation does not complete. Event
candidates MUST contain only the supported type, user-expressed content, date when known, date precision,
and original date text when present. A history-consulting operation MUST contain the question the user
asked and MUST NOT contain event candidates. The adapter MUST treat as a clinical history question every
message in which any part depends on the clinical history, including a colloquial reformulation of a
recorded fact, and MUST NOT answer such a message as general conversation. When a message also contains a
general part, the adapter MUST preserve that general part so it can be answered as conversation instead of
being searched or ignored. When the adapter cannot determine whether the message depends on the clinical
history, it MUST ask for clarification and MUST NOT guess a channel.

#### Scenario: Map one or more event candidates

- **WHEN** the provider requests a registration operation with valid structured content
- **THEN** the application validates the operation and registers one event per distinct fact
- **AND** the provider never reaches clinical persistence directly

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

## ADDED Requirements

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
