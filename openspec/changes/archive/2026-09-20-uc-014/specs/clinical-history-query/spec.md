## MODIFIED Requirements

### Requirement: Redactar la respuesta solo con los hechos recuperados

El sistema MUST elaborar una respuesta en español usando únicamente los hechos
recuperados, conservando su contenido y la precisión temporal registrada. Cuando
la pregunta necesite varios hechos, el sistema MUST ofrecer una sola respuesta
coherente apoyada en todos ellos. Los valores de una medición MUST NOT
presentarse como hechos clínicos ni recuperarse de la historia clínica: las
mediciones tienen su propio seguimiento y una pregunta sobre ellas MUST
atenderse por el cauce de las mediciones.

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
- **THEN** el sistema indica que ese seguimiento tiene su propio espacio
- **AND** no presenta los valores como hechos clínicos
- **AND** no inventa valores ni responde con los de otra medición

#### Scenario: Conservar la imprecisión temporal
- **WHEN** la persona pregunta por un hecho cuya fecha quedó registrada como aproximada
- **THEN** el sistema responde conservando esa imprecisión
- **AND** no presenta la fecha como exacta

#### Scenario: Responder una pregunta que requiere varios hechos
- **WHEN** la persona formula una pregunta que abarca más de un hecho
- **THEN** el sistema reúne todos los hechos necesarios
- **AND** ofrece una sola respuesta coherente apoyada en ellos
