## MODIFIED Requirements

### Requirement: Reconocer las preguntas sobre la propia historia clínica

El sistema MUST distinguir un mensaje que pregunta por la propia historia clínica de
un mensaje que registra un hecho, de la conversación general y de una pregunta sobre
peso o circunferencia abdominal. Un mensaje que no depende de la historia MUST NOT
activar ninguna búsqueda. Cuando el mensaje contenga, además, una parte que no depende
de la historia, la búsqueda MUST limitarse a la parte que sí depende de ella y esa
otra parte MUST NOT usarse como criterio de recuperación ni responderse con hechos
registrados. Una pregunta formulada en lenguaje coloquial sobre algo registrado MUST
atenderse como consulta de la historia: el registro del lenguaje no cambia el cauce.
Cuando no pueda determinarse si la pregunta depende de la historia, el sistema MUST
pedir una aclaración antes de buscar y MUST NOT asumir un cauce.

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

### Requirement: Pedir aclaración ante una pregunta ambigua

Cuando la pregunta admita varias interpretaciones y no permita determinar qué se
consulta, el sistema MUST pedir una aclaración comprensible en español antes de
responder y MUST NOT asumir ninguna interpretación. El sistema MUST pedir la aclaración
también cuando no pueda determinar si la pregunta depende de la historia clínica, en
cuyo caso MUST NOT buscar en la historia ni responder como conversación general.

#### Scenario: Pregunta ambigua
- **WHEN** la persona envía una pregunta que admite varias interpretaciones
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no asume ninguna interpretación
- **AND** la historia clínica permanece sin cambios

#### Scenario: Cauce por determinar
- **WHEN** el sistema no puede determinar si la pregunta depende de la historia clínica
- **THEN** el sistema pide una aclaración comprensible en español antes de responder
- **AND** no busca en la historia clínica
- **AND** no responde como conversación general
- **AND** la historia clínica permanece sin cambios
