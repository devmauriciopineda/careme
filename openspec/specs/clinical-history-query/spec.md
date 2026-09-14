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
general ni con suposiciones.

#### Scenario: No hay hechos que respondan la pregunta
- **WHEN** la historia clínica no contiene hechos que respondan la pregunta
- **THEN** el sistema responde explícitamente que no encuentra registros
- **AND** no completa la respuesta con conocimiento general ni con suposiciones
- **AND** la historia clínica permanece sin cambios

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
