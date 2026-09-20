## REMOVED Requirements

### Requirement: Return explicit chat outcomes

**Reason**: El turno deja de ser «un resultado por mensaje». El asistente decide qué operaciones necesita el mensaje y el backend las ejecuta, de modo que un mismo turno puede completar más de una —registrar un hecho y responder una pregunta— y el resultado deja de ser único. El catálogo de resultados se conserva; lo que desaparece es el contrato de un solo resultado por mensaje.

**Migration**: Sustituido por «Report the outcome of the turn and the operations it went through», que conserva los siete resultados, su contenido en español y sus reglas de ausencia, y añade el informe de las operaciones por las que pasó el turno. El estado del turno pasa a derivarse de esas operaciones.

### Requirement: Separate the general part of a mixed message

**Reason**: El asistente compone **una sola respuesta** por mensaje, que es el objetivo de este cambio. Con una respuesta única, la parte general de un mensaje mixto ya no puede viajar en un campo aparte del resultado clínico, porque no hay dos entregas que separar.

**Migration**: La parte del mensaje que no depende de la historia clínica se atiende dentro de la respuesta única, y sigue sin usarse como criterio de recuperación. Desaparecen el campo `generalReply` de la respuesta y la parte general del contrato de consulta; la interfaz deja de presentar dos clases de respuesta.

## ADDED Requirements

### Requirement: Report the outcome of the turn and the operations it went through

El sistema MUST devolver exactamente un resultado observable por mensaje aceptado: `registered`, `answered`, `no_records`, `clarification_required`, `general_conversation`, `duplicate` o `failed`. Un turno MAY completar más de una operación. El sistema MUST informar de **todas** las operaciones por las que pasó el turno, en el orden en que se ejecutaron, cada una con su propio resultado, de modo que un cliente pueda distinguir lo que quedó completado de lo que no. La respuesta MUST incluir **una sola** respuesta visible para la persona, en español, que cubra todo lo que el mensaje requería, y MUST NOT exponer prompts del proveedor, credenciales, trazas ni detalles internos de la persistencia.

El estado del turno MUST describir el turno en su conjunto: MUST ser el resultado de la operación cuando el turno ejecutó una sola; MUST ser `answered` cuando el turno completó más de una operación y produjo una respuesta; y MUST ser `failed` cuando alguna operación no se completó, aunque otra del mismo turno sí lo hiciera.

Para un resultado `answered`, la respuesta MUST incluir las referencias a los hechos clínicos que sustentan la respuesta. Para `no_records`, MUST incluir el motivo por el que la ausencia aplica y la continuación ofrecida a la persona. Para `general_conversation`, MUST llevar la réplica conversacional compuesta para ese mensaje, MUST NOT llevar referencias a hechos clínicos y MUST NOT presentar ninguna parte de la réplica como un hecho registrado. Para `clarification_required`, MUST incluir una pregunta de aclaración en español, y MUST devolverlo también cuando no pueda determinarse si el mensaje depende de la historia clínica. El sistema MUST NOT modificar la historia clínica al responder.

#### Scenario: Report a turn that registered a fact and answered a question

- **WHEN** el turno completa un registro y una consulta
- **THEN** la respuesta informa de las dos operaciones, cada una con su propio resultado
- **AND** el estado es `answered`
- **AND** la persona recibe una sola respuesta que cubre ambas cosas

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
