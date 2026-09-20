## MODIFIED Requirements

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

Para un resultado `answered`, la respuesta MUST incluir las referencias a los hechos clínicos que sustentan la respuesta. Para `no_records`, MUST incluir el motivo por el que la ausencia aplica y la continuación ofrecida a la persona. Para `general_conversation`, MUST llevar la réplica conversacional compuesta para ese mensaje, MUST NOT llevar referencias a hechos clínicos y MUST NOT presentar ninguna parte de la réplica como un hecho registrado. Para `clarification_required`, MUST incluir una pregunta de aclaración en español, y MUST devolverlo también cuando no pueda determinarse si el mensaje depende de la historia clínica. El sistema MUST NOT modificar la historia clínica al responder.

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

#### Scenario: Return a clarification outcome for an undetermined channel
- **WHEN** el mensaje puede o no depender de la historia clínica y el sistema no puede determinarlo
- **THEN** el estado es `clarification_required` e incluye una pregunta de aclaración en español
- **AND** el sistema no asume ninguna interpretación del mensaje
- **AND** no lee la historia clínica

## ADDED Requirements

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
