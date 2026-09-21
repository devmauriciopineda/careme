## MODIFIED Requirements

### Requirement: Reconocer las preguntas sobre la propia historia clínica

El sistema MUST distinguir un mensaje que pregunta por la propia historia clínica de un mensaje que
registra un hecho, de la conversación general y de una pregunta sobre las mediciones del seguimiento. Un
mensaje que no depende de la historia MUST NOT activar ninguna búsqueda. Cuando el mensaje contenga,
además, una parte que no depende de la historia, la búsqueda MUST limitarse a la parte que sí depende de
ella y esa otra parte MUST NOT usarse como criterio de recuperación ni responderse con hechos registrados.
Una pregunta formulada en lenguaje coloquial sobre algo registrado MUST atenderse como consulta de la
historia: el registro del lenguaje no cambia el cauce. Cuando no pueda determinarse si la pregunta depende
de la historia, el sistema MUST pedir una aclaración antes de buscar y MUST NOT asumir un cauce.

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
- **WHEN** la persona pregunta por su peso, su circunferencia abdominal, su presión arterial o su colesterol
- **THEN** el sistema la reconoce como una pregunta sobre su seguimiento de mediciones y la atiende por su cauce
- **AND** no la busca en la historia clínica ni la presenta como un hecho clínico

#### Scenario: Pregunta coloquial sobre hechos registrados
- **WHEN** la persona reformula en lenguaje coloquial una pregunta sobre hechos que sí están registrados
- **THEN** el sistema la reconoce como una consulta de la historia
- **AND** responde conforme a la consulta de la propia historia clínica
- **AND** no la responde como conversación general

#### Scenario: Mensaje que mezcla una parte general con una consulta sobre la historia
- **WHEN** la persona envía un mensaje que contiene una parte general y una pregunta que depende de su historia clínica
- **THEN** el sistema busca en la historia solo con la parte que depende de ella
- **AND** no usa la parte general como criterio de recuperación ni la responde con hechos registrados
- **AND** la historia clínica no se modifica

#### Scenario: No puede determinarse si la pregunta depende de la historia
- **WHEN** la persona envía una pregunta que puede o no depender de su historia clínica
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no busca en la historia clínica mientras no se determine el cauce
- **AND** la historia clínica permanece sin cambios

### Requirement: Redactar la respuesta solo con los hechos recuperados

El sistema MUST elaborar una respuesta en español usando únicamente los hechos recuperados, conservando
su contenido y la precisión temporal registrada. Cuando la pregunta necesite varios hechos, el sistema
MUST ofrecer una sola respuesta coherente apoyada en todos ellos. Los valores de una medición MUST NOT
presentarse como hechos clínicos ni recuperarse de la historia clínica: las mediciones tienen su propio
seguimiento y una pregunta sobre ellas MUST atenderse por el cauce de las mediciones.

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
- **WHEN** la persona pregunta qué valores tiene registrados, por ejemplo su peso o su circunferencia abdominal
- **THEN** el sistema atiende la pregunta por el cauce de las mediciones de su seguimiento
- **AND** no presenta los valores como hechos clínicos ni los recupera de la historia
- **AND** no inventa valores ni responde con los de otra medición

#### Scenario: Conservar la imprecisión temporal
- **WHEN** la persona pregunta por un hecho cuya fecha quedó registrada como aproximada
- **THEN** el sistema responde conservando esa imprecisión
- **AND** no presenta la fecha como exacta

#### Scenario: Responder una pregunta que requiere varios hechos
- **WHEN** la persona formula una pregunta que abarca más de un hecho
- **THEN** el sistema reúne todos los hechos necesarios
- **AND** ofrece una sola respuesta coherente apoyada en ellos
