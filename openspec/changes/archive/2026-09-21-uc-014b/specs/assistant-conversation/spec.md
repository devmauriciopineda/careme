## MODIFIED Requirements

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
