# clinical-history-query Specification

## Purpose

Permitir que la persona pregunte por su propia historia clínica en lenguaje
natural y reciba una respuesta fundada únicamente en los hechos registrados, con
las referencias que permiten verificarla.

## Requirements

### Requirement: Reconocer las preguntas sobre la propia historia clínica

El sistema MUST distinguir un mensaje que pregunta por la propia historia
clínica de un mensaje que registra un hecho, de la conversación general y de una
pregunta sobre peso o circunferencia abdominal. Un mensaje que no depende de la
historia MUST NOT activar ninguna búsqueda.

#### Scenario: Pregunta sobre la propia historia
- **WHEN** la persona envía en el chat una pregunta sobre su propia historia clínica
- **THEN** el sistema la reconoce como una consulta y busca en los hechos registrados
- **AND** no registra ningún hecho nuevo

#### Scenario: Pregunta que no depende de la historia
- **WHEN** la persona formula una pregunta que no depende de su historia clínica
- **THEN** el sistema no busca en la historia clínica
- **AND** responde como conversación general
- **AND** la historia clínica no se modifica

#### Scenario: Pregunta sobre peso o circunferencia abdominal
- **WHEN** la persona pregunta por su peso o su circunferencia abdominal
- **THEN** el sistema indica que ese seguimiento tiene su propio espacio
- **AND** aclara que no forma parte de la historia clínica consultada

### Requirement: Recuperar únicamente hechos registrados

El sistema MUST recuperar los hechos que pueden responder la pregunta buscando
por texto y filtrando por los metadatos disponibles, como el tipo y la fecha. La
recuperación MUST limitarse a los hechos presentes en el índice de la historia y
MUST NOT construir hechos que no estén registrados.

#### Scenario: Recuperar los hechos relevantes
- **WHEN** la pregunta puede responderse con hechos registrados
- **THEN** el sistema recupera los hechos correspondientes de la historia
- **AND** no incluye hechos que no respondan a la pregunta

#### Scenario: No construir hechos no registrados
- **WHEN** el índice no contiene ningún hecho relacionado con la pregunta
- **THEN** el sistema no recupera ni construye ningún hecho
- **AND** no presenta ninguna afirmación como si estuviera respaldada

### Requirement: Redactar la respuesta solo con los hechos recuperados

El sistema MUST elaborar una respuesta en español usando únicamente los hechos
recuperados, conservando su contenido y la precisión temporal registrada. Cuando
la pregunta necesite varios hechos, el sistema MUST ofrecer una sola respuesta
coherente apoyada en todos ellos.

#### Scenario: Responder una pregunta con hechos registrados
- **WHEN** la persona envía una pregunta sobre su propia historia clínica y existen hechos que la responden
- **THEN** el sistema responde apoyándose únicamente en los hechos que encontró
- **AND** la respuesta conserva el contenido y la precisión temporal registrados
- **AND** la historia clínica no se modifica

#### Scenario: Preguntar por un hecho concreto y su fecha
- **WHEN** la persona pregunta cuándo ocurrió un hecho registrado
- **THEN** el sistema encuentra el hecho y responde indicando la fecha registrada
- **AND** presenta la fecha con el grado de precisión con que quedó registrada

#### Scenario: Preguntar por valores o mediciones registradas
- **WHEN** la persona pregunta qué valores tiene registrados
- **THEN** el sistema encuentra los hechos de medición correspondientes
- **AND** responde con los valores tal como quedaron registrados

#### Scenario: Conservar la imprecisión temporal
- **WHEN** la persona pregunta por un hecho cuya fecha quedó registrada como aproximada
- **THEN** el sistema responde conservando esa imprecisión
- **AND** no presenta la fecha como exacta

#### Scenario: Responder una pregunta que requiere varios hechos
- **WHEN** la persona formula una pregunta que abarca más de un hecho
- **THEN** el sistema reúne todos los hechos necesarios
- **AND** ofrece una sola respuesta coherente apoyada en ellos

### Requirement: Mostrar los hechos que sustentan la respuesta

La respuesta MUST permitir a la persona identificar los hechos registrados en los
que se apoya, para que pueda verificarlos.

#### Scenario: Identificar los hechos de apoyo
- **WHEN** el sistema entrega una respuesta apoyada en hechos registrados
- **THEN** la respuesta incluye las referencias a esos hechos
- **AND** cada referencia corresponde a un hecho realmente recuperado

### Requirement: Interpretar preguntas de seguimiento con la conversación activa

Cuando una pregunta dependa de los turnos recientes de la conversación en curso,
el sistema MUST interpretarla junto con ellos y responder con la misma base
documental. El historial MUST NOT conservarse más allá de la conversación activa.

#### Scenario: Pregunta de seguimiento
- **WHEN** la persona mantiene una conversación activa y formula una pregunta de seguimiento que depende de los turnos recientes
- **THEN** el sistema interpreta la pregunta junto con esos turnos recientes
- **AND** responde apoyándose en los hechos registrados correspondientes
- **AND** el historial no se conserva más allá de la conversación activa

### Requirement: Declarar explícitamente la ausencia de registros

Cuando no existan hechos que respalden la pregunta, el sistema MUST declararlo de
forma explícita en español y MUST NOT completar la respuesta con conocimiento
general ni con suposiciones. La ausencia MUST declararse tanto cuando la búsqueda
no devuelve ningún hecho como cuando devuelve hechos registrados que no responden
a la pregunta. La declaración MUST identificar cuál de las siguientes situaciones
de ausencia aplica:

- la historia clínica no contiene ningún hecho registrado;
- la historia no contiene hechos del tipo consultado;
- la historia no contiene hechos en el periodo consultado;
- la historia contiene hechos, pero ninguno responde a la pregunta.

Cuando la ausencia se deba a un tipo o a un periodo consultado, la declaración
MUST acotar el alcance a ese tipo o a ese periodo y MUST NOT presentarlo como una
ausencia en toda la historia. La declaración MUST NOT afirmar ni sugerir que el
hecho no ocurrió: MUST dejar claro que la ausencia se refiere a lo que consta
registrado.

Un hecho recuperado que no responde a la pregunta MUST NOT presentarse como apoyo
de una respuesta ni usarse para responderla. La decisión de si los hechos
recuperados responden a la pregunta corresponde a quien compone la respuesta, no
a la búsqueda: MUST NOT darse por respondida una pregunta solo porque la búsqueda
devolvió algo.

#### Scenario: No hay hechos que respondan la pregunta
- **WHEN** la historia clínica no contiene hechos que respondan la pregunta
- **THEN** el sistema responde explícitamente que no encuentra registros
- **AND** no completa la respuesta con conocimiento general ni con suposiciones
- **AND** la historia clínica permanece sin cambios

#### Scenario: La búsqueda no devuelve ningún hecho
- **WHEN** la búsqueda en la historia no devuelve ningún hecho
- **THEN** el sistema declara la ausencia de registros conforme a esta especificación

#### Scenario: La búsqueda devuelve hechos que no responden a la pregunta
- **WHEN** la búsqueda devuelve hechos registrados que no responden a la pregunta
- **THEN** el sistema declara la ausencia de registros como si la búsqueda no hubiera devuelto nada
- **AND** identifica la situación de ausencia que aplica
- **AND** no responde a la pregunta con esos hechos
- **AND** no presenta ninguno de ellos como apoyo de una respuesta
- **AND** la historia clínica permanece sin cambios

#### Scenario: Historia clínica sin ningún hecho registrado
- **WHEN** la persona pregunta por información clínica propia y la historia no contiene ningún hecho registrado
- **THEN** el sistema declara que todavía no hay registros en la historia
- **AND** presenta esa situación como el estado inicial y no como un fallo
- **AND** no completa la respuesta con conocimiento general ni con suposiciones

#### Scenario: Ausencia acotada al tipo de hecho consultado
- **WHEN** la persona pregunta por un tipo de hecho que no está registrado en su historia
- **THEN** el sistema declara que no encuentra registros de ese tipo
- **AND** no deduce la respuesta a partir de hechos registrados de otro tipo

#### Scenario: Ausencia acotada al periodo consultado
- **WHEN** la persona pregunta por un periodo que no contiene hechos registrados
- **THEN** el sistema declara que no encuentra registros en ese periodo
- **AND** aclara que la ausencia se refiere a ese periodo y no a toda la historia

#### Scenario: Ausencia porque ningún hecho responde a la pregunta
- **WHEN** la búsqueda no encuentra hechos que respondan a la pregunta
- **THEN** el sistema declara que no encuentra registros que respondan a la pregunta
- **AND** no presenta ninguna afirmación como si estuviera respaldada

#### Scenario: La ausencia no afirma que el hecho no ocurriera
- **WHEN** el sistema declara que no encuentra registros para una pregunta
- **THEN** la declaración se refiere a lo que consta en la historia
- **AND** no afirma ni niega que el hecho haya ocurrido

### Requirement: Pedir aclaración ante una pregunta ambigua

Cuando la pregunta admita varias interpretaciones y no permita determinar qué se
consulta, el sistema MUST pedir una aclaración comprensible en español antes de
responder y MUST NOT asumir ninguna interpretación.

#### Scenario: Pregunta ambigua
- **WHEN** la persona envía una pregunta que admite varias interpretaciones
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no asume ninguna interpretación
- **AND** la historia clínica permanece sin cambios

### Requirement: Informar fallos sin inventar una respuesta

Cuando el sistema no logre interpretar la pregunta o no pueda completar la
búsqueda, MUST informar de un fallo recuperable en español sin exponer detalles
internos, MUST NOT entregar una respuesta sin respaldo y MUST conservar la
pregunta para que la persona pueda volver a intentarlo.

#### Scenario: No se logra interpretar la pregunta
- **WHEN** el sistema no logra interpretar una pregunta sobre la historia
- **THEN** informa de un fallo recuperable en español sin mostrar detalles internos
- **AND** no responde con suposiciones
- **AND** conserva la pregunta para que la persona pueda volver a intentarlo
- **AND** la historia clínica permanece sin cambios

#### Scenario: No se puede completar la búsqueda
- **WHEN** el sistema no puede completar la búsqueda en la historia
- **THEN** informa del fallo en español sin mostrar detalles internos
- **AND** no entrega una respuesta sin respaldo
- **AND** ofrece volver a intentarlo
- **AND** la historia clínica permanece exactamente como estaba

### Requirement: Mantener la historia intacta

Una consulta MUST NOT modificar la historia clínica. Si la conversación se
interrumpe antes de completar la respuesta, el sistema MUST descartar el estado
efímero y la historia MUST permanecer intacta.

#### Scenario: La consulta no modifica la historia
- **WHEN** el sistema completa, declara ausencia, pide aclaración o falla al responder una consulta
- **THEN** la historia clínica permanece exactamente como estaba

#### Scenario: Interrupción antes de completar la respuesta
- **WHEN** la conversación se interrumpe antes de completar la respuesta
- **THEN** el estado efímero de la conversación se descarta
- **AND** la historia clínica permanece intacta
- **AND** la persona puede volver a formular su pregunta

### Requirement: Ofrecer una salida accionable tras declarar la ausencia

Toda declaración de ausencia MUST ofrecer a la persona un camino para continuar:
reformular la pregunta o registrar el hecho que no consta. La salida ofrecida
MUST NOT registrar nada por sí misma.

#### Scenario: Ofrecer continuar tras la ausencia
- **WHEN** el sistema declara que no encuentra registros para una pregunta
- **THEN** la declaración ofrece reformular la pregunta o registrar el hecho que no consta
- **AND** no se incorpora ningún hecho a la historia durante esa respuesta
- **AND** la historia clínica permanece sin cambios

#### Scenario: Registrar únicamente cuando la persona lo pide
- **WHEN** la persona indica que el hecho ocurrió pero no consta
- **THEN** el sistema le ofrece registrarlo por el cauce de registro clínico
- **AND** no incorpora el hecho por su cuenta en esa respuesta

### Requirement: Responder la parte respaldada y declarar la parte ausente

Cuando una misma pregunta pueda responderse en parte y en parte no, el sistema
MUST responder únicamente con la parte respaldada por hechos registrados, MUST
declarar de forma explícita qué parte no tiene registros y MUST NOT completar la
parte ausente con conocimiento general ni con suposiciones.

#### Scenario: Pregunta parcialmente respaldada
- **WHEN** la persona formula una pregunta en la que solo una parte está respaldada por hechos registrados
- **THEN** el sistema responde con la parte respaldada y sus referencias
- **AND** declara explícitamente la parte de la pregunta que no tiene registros
- **AND** no completa la parte ausente con conocimiento general ni con suposiciones

### Requirement: Mantener la declaración de ausencia ante la insistencia

Cuando la persona insista en una pregunta ya declarada sin registros, el sistema
MUST mantener la misma declaración, MUST NOT fabricar información para satisfacer
la petición y MUST NOT modificar la historia clínica.

#### Scenario: La persona insiste tras la negativa
- **WHEN** el sistema ha declarado que no encuentra registros y la persona insiste en la misma pregunta
- **THEN** el sistema mantiene la misma declaración de ausencia
- **AND** no fabrica información para satisfacer la petición
- **AND** la historia clínica permanece sin cambios

### Requirement: Distinguir la ausencia de registros de un fallo de búsqueda

Una búsqueda que no pudo completarse MUST NOT presentarse como una ausencia de
registros. El sistema MUST informar del fallo de forma recuperable y MUST NOT
declarar una ausencia que no ha podido comprobar.

#### Scenario: La búsqueda falla y no puede comprobarse la ausencia
- **WHEN** el sistema no puede completar la búsqueda en la historia
- **THEN** informa de un fallo recuperable en español sin mostrar detalles internos
- **AND** no declara una ausencia de registros que no ha podido comprobar
- **AND** conserva la pregunta para que la persona pueda volver a intentarlo

#### Scenario: La ausencia no se comunica como fallo
- **WHEN** la búsqueda se completa y no encuentra hechos que respondan la pregunta
- **THEN** el sistema declara la ausencia de registros
- **AND** no la comunica como un fallo del sistema
