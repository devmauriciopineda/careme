# assistant-conversation Specification

## Purpose

Define the provider-independent conversation contract that lets the application receive chat messages, coordinate clinical registration, and return deterministic outcomes without persisting conversation history.

## Requirements

### Requirement: Process chat messages through a bounded conversation

The system MUST expose a non-streaming JSON chat operation that accepts a user message, an idempotent message identifier, and an optional conversation identifier. When no conversation identifier is supplied, the system MUST create one, open the consultation for that conversation, and return the identifier. The system MUST keep the open consultation with the notes it has collected, pending clarification state, duplicate detection, and a bounded buffer of the most recent turns only for the active conversation, and MUST NOT persist the conversation history itself. The consultation and the notes it collects are not conversation history: they MUST be persisted with the consultation so an interruption does not lose what the person narrated.

#### Scenario: Start a conversation and register an event
- **WHEN** the client sends a valid message with a new message identifier and no conversation identifier
- **THEN** the system creates a conversation identifier, opens the consultation for that conversation, processes the message, and returns the identifier with the turn outcome
- **AND** the clinical fact the message mentions is collected as a note of that consultation
- **AND** the clinical event is registered as Markdown and made available through the derived index when that consultation is closed

#### Scenario: Continue a conversation after clarification
- **WHEN** the system has requested clarification and the client sends the next message with the same conversation identifier
- **THEN** the system correlates the answer with the pending clarification and processes the combined clinical intent
- **AND** the system does not require persisted conversation history

#### Scenario: Answer a follow-up question from recent turns
- **WHEN** the client sends, in the same conversation, a follow-up question that depends on the recent turns
- **THEN** the system interprets the turn together with the bounded buffer of recent turns
- **AND** it answers from the clinical history, not from the buffer
- **AND** it does not require persisted conversation history

#### Scenario: Conversation state is lost after restart
- **WHEN** the backend restarts while a conversation has a pending clarification and a consultation with notes collected
- **THEN** the pending clarification is discarded
- **AND** the open consultation and the notes it collected survive
- **AND** the next message is processed as a new turn without corrupting persisted clinical events

#### Scenario: Discard the ephemeral buffer after an interruption
- **WHEN** the conversation is interrupted before the answer completes
- **THEN** the ephemeral conversation state is discarded
- **AND** the consultation is closed with the notes it had collected
- **AND** the admissible facts are registered and the clinical history only gains those facts

### Requirement: Report the outcome of the turn and the operations it went through

El sistema MUST devolver exactamente un resultado observable por mensaje aceptado: `noted`, `answered`, `no_records`, `clarification_required`, `general_conversation`, `duplicate` o `failed`. Un turno MAY completar más de una operación. El sistema MUST informar de **todas** las operaciones por las que pasó el turno, en el orden en que se ejecutaron, cada una con su propio resultado, de modo que un cliente pueda distinguir lo que quedó completado de lo que no. La respuesta MUST incluir **una sola** respuesta visible para la persona, en español, que cubra todo lo que el mensaje requería, y MUST NOT exponer prompts del proveedor, credenciales, trazas ni detalles internos de la persistencia.

El registro de hechos MUST NOT formar parte del catálogo de resultados del turno: un turno recoge hechos en notas para el cierre de la consulta y MUST NOT incorporarlos a la historia clínica. El resultado `noted` MUST indicar que el turno recogió uno o más hechos clínicos sin registrarlos, y MUST NOT presentarse como un hecho ya incorporado a la historia.

El estado del turno MUST describir el turno en su conjunto: MUST ser el resultado de la operación cuando el turno ejecutó una sola; MUST ser `answered` cuando el turno completó más de una operación y produjo una respuesta; y MUST ser `failed` cuando alguna operación no se completó, aunque otra del mismo turno sí lo hiciera.

Para un resultado `answered`, la respuesta MUST incluir las referencias a los hechos clínicos o a las mediciones que sustentan la respuesta, según el cauce que la haya producido, y MUST distinguir si la respuesta se apoya en hechos clínicos o en mediciones del seguimiento. Una ausencia de mediciones MUST NOT presentarse como una ausencia de hechos clínicos ni al revés. Para `no_records`, MUST incluir el motivo por el que la ausencia aplica y la continuación ofrecida a la persona. Para `general_conversation`, MUST llevar la réplica conversacional compuesta para ese mensaje, MUST NOT llevar referencias a hechos clínicos y MUST NOT presentar ninguna parte de la réplica como un hecho registrado. Para `clarification_required`, MUST incluir una pregunta de aclaración en español, y MUST devolverlo también cuando no pueda determinarse si el mensaje depende de la historia clínica. El sistema MUST NOT modificar la historia clínica al responder. Una consulta del seguimiento de mediciones MUST NOT modificar el seguimiento.

#### Scenario: Report a turn that registered a fact and answered a question
- **WHEN** el turno recoge un hecho clínico y responde una consulta a la historia
- **THEN** la respuesta informa de las dos operaciones, cada una con su propio resultado
- **AND** el estado es `answered`
- **AND** la persona recibe una sola respuesta que cubre ambas cosas
- **AND** el hecho recogido no se presenta como registrado y se registra al cerrar la consulta

#### Scenario: Report a turn that only collected clinical facts
- **WHEN** el turno no ejecuta ninguna consulta y recoge uno o más hechos clínicos
- **THEN** el estado es `noted`
- **AND** la respuesta informa de lo que quedó recogido para el cierre de la consulta
- **AND** no se incorpora ningún hecho a la historia clínica

#### Scenario: Report a turn that went through several consultations
- **WHEN** el turno ejecuta más de una consulta de la historia clínica
- **THEN** la respuesta informa de cada consulta en el orden en que se ejecutó
- **AND** ninguna operación completada se omite de la respuesta

#### Scenario: Report a turn that answered a measurement query
- **WHEN** el turno responde una consulta del seguimiento de mediciones
- **THEN** el estado es `answered` y la respuesta lleva los valores, las unidades y las fechas recuperados
- **AND** la respuesta incluye las referencias a las mediciones que sustentan la respuesta
- **AND** el seguimiento de mediciones permanece exactamente como estaba

#### Scenario: Report a measurement absence distinctly from a clinical-history absence
- **WHEN** el turno responde una consulta de mediciones y no consta ninguna medición de la métrica preguntada
- **THEN** la respuesta declara la ausencia de mediciones con ese motivo
- **AND** la ausencia se distingue de una ausencia de hechos clínicos
- **AND** no se presenta ninguna medición como sustento

#### Scenario: Report a completed operation when another one failed
- **WHEN** una operación del turno no se completa después de que otra sí lo hiciera
- **THEN** el estado es `failed` con un mensaje reintentable en español
- **AND** la respuesta informa de la operación que sí se completó, con su propio resultado
- **AND** declara la operación que no se completó
- **AND** no presenta el turno como completamente exitoso

#### Scenario: Report an operation that was rejected for missing information
- **WHEN** una operación no se ejecuta porque le falta información o contradice las reglas
- **THEN** el estado es `clarification_required` e incluye una pregunta de aclaración en español
- **AND** la respuesta informa de la operación como no completada
- **AND** no se registra ningún hecho por esa operación

#### Scenario: Report a no-records outcome with its reason and its continuation
- **WHEN** una consulta de la historia no encuentra hechos que respondan la pregunta
- **THEN** la respuesta informa de esa operación como una ausencia
- **AND** identifica cuál de las situaciones de ausencia aplica
- **AND** incluye la continuación ofrecida a la persona
- **AND** no presenta ningún hecho clínico como sustento

#### Scenario: Report a failure with a retryable message
- **WHEN** el proveedor, la validación del contrato o la publicación de un hecho falla
- **THEN** el estado es `failed` con un mensaje reintentable en español
- **AND** ningún hecho clínico parcial queda visible
- **AND** la respuesta informa de las operaciones del turno que sí se completaron

#### Scenario: Answer general conversation without touching the history
- **WHEN** el turno no ejecuta ninguna operación porque el mensaje no depende de la historia clínica
- **THEN** el estado es `general_conversation` y lleva la réplica conversacional en español
- **AND** la respuesta informa de que el turno no pasó por ninguna operación
- **AND** no se registra ningún hecho clínico

#### Scenario: Keep absence out of the failed outcome
- **WHEN** la búsqueda no puede completarse, de modo que ninguna ausencia puede verificarse
- **THEN** el estado es `failed` con un mensaje reintentable en español
- **AND** no se devuelve como `no_records`

#### Scenario: Keep a measurement retrieval failure out of the absence outcome
- **WHEN** la recuperación de las mediciones no puede completarse
- **THEN** el estado es `failed` con un mensaje reintentable en español
- **AND** no se devuelve como una ausencia de mediciones
- **AND** el seguimiento permanece exactamente como estaba

#### Scenario: Return a clarification outcome for an undetermined channel
- **WHEN** el mensaje puede o no depender de la historia clínica y el sistema no puede determinarlo
- **THEN** el estado es `clarification_required` e incluye una pregunta de aclaración en español
- **AND** el sistema no asume ninguna interpretación del mensaje
- **AND** no lee la historia clínica

### Requirement: Make message retries idempotent

The system MUST process a given `(conversationId, messageId)` at most once for side effects. Repeating the same request MUST return the original outcome without creating another clinical event or invoking a second persistence operation.

#### Scenario: Retry a completed message
- **WHEN** the client repeats the same message with the same conversation and message identifiers
- **THEN** the system returns the original outcome
- **AND** the number and contents of persisted events remain unchanged

#### Scenario: Reject an invalid chat request
- **WHEN** the message is blank, exceeds the configured size limit, or has an invalid identifier
- **THEN** the system returns a client error with a Spanish validation message
- **AND** it does not call the LLM or modify clinical persistence

### Requirement: Allow the declared origin to reach the operations a browser client uses

The API MUST accept a cross-origin preflight from every configured browser origin for the HTTP verbs those clients use, so a browser client is not rejected before its request is read. The API MUST keep every other verb closed for the same mapping.

#### Scenario: Accept a preflight for a browser write
- **WHEN** a browser client at a configured origin sends a preflight for a write operation it uses
- **THEN** the API answers it successfully
- **AND** the answer names that origin and that verb

#### Scenario: Keep unused verbs closed
- **WHEN** a browser client asks for a verb outside the exposed set
- **THEN** the API does not allow it

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

### Requirement: Close the consultation in progress

El sistema MUST ofrecer una operación que termine la consulta en curso y MUST informar de su resultado de cierre: los hechos registrados con su procedencia y si el resumen quedó guardado, o que no hubo hechos que registrar. La operación MUST ser idempotente para la misma consulta, MUST cerrar como máximo una consulta y MUST NOT registrar nada cuando la consulta ya estaba cerrada. La respuesta MUST ser un único mensaje en español para la persona y MUST NOT exponer detalles internos.

#### Scenario: Close an open consultation and report the outcome
- **WHEN** el cliente solicita cerrar la consulta en curso
- **THEN** el sistema registra los hechos admisibles con su procedencia y guarda el resumen
- **AND** informa de los hechos registrados y del resumen guardado en un solo mensaje en español
- **AND** la consulta queda cerrada

#### Scenario: Repeat the close of the same consultation
- **WHEN** el cliente solicita cerrar una consulta que ya estaba cerrada
- **THEN** el sistema devuelve el mismo resultado de cierre
- **AND** no registra ningún hecho nuevo
- **AND** no abre ninguna consulta

#### Scenario: Close a consultation that collected no clinical facts
- **WHEN** el cliente solicita cerrar una consulta que no recogió hechos clínicos
- **THEN** el sistema informa de que no hubo hechos que registrar
- **AND** la consulta queda cerrada con su resumen
- **AND** la historia clínica permanece exactamente como estaba
