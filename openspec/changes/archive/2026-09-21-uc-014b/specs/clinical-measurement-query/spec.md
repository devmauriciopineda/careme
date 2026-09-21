## Purpose

Permitir que la persona pregunte por sus mediciones —peso, circunferencia abdominal, presión arterial,
colesterol o cualquier otra métrica que el sistema admita— en la conversación y obtenga una respuesta con
los valores y las fechas tal como quedaron registrados, sin que se altere ningún registro.

## ADDED Requirements

### Requirement: Determinar la métrica y el periodo de la pregunta

El sistema MUST determinar, a partir de la pregunta en lenguaje natural y del catálogo de métricas, qué
métrica o métricas abarca y qué periodo, cuando la pregunta lo concrete. Cuando la pregunta no concrete
qué métrica se pregunta, el sistema MUST NOT suponer cuál es y MUST pedir en español que la concrete
antes de responder. Un periodo concretado MUST acotar la recuperación a ese periodo.

#### Scenario: Determinar la métrica y el periodo de la pregunta

- **WHEN** la persona pregunta por una métrica concreta de su seguimiento, concretando además un periodo
- **THEN** el sistema determina la métrica y el periodo que abarca la pregunta
- **AND** recupera las mediciones de esa métrica dentro de ese periodo

#### Scenario: Pedir que se concrete la métrica preguntada

- **WHEN** la persona pregunta por sus mediciones sin concretar cuál, habiendo mediciones de más de una métrica
- **THEN** el sistema no supone qué métrica pregunta
- **AND** pide que la concrete antes de responder
- **AND** no responde con una suposición

#### Scenario: Preguntar por varias métricas a la vez

- **WHEN** la persona pregunta por varias métricas a la vez
- **THEN** el sistema determina todas las métricas que abarca la pregunta
- **AND** responde por cada una, con los valores y las fechas de sus mediciones

### Requirement: Recuperar las mediciones sin alterar el seguimiento

El sistema MUST recuperar del seguimiento las mediciones que corresponden a la métrica o métricas y al
periodo determinados, y MUST limitarse a las mediciones registradas, sin completar lo que falta con
suposiciones ni con conocimiento general. La consulta de mediciones MUST NOT modificar ningún registro:
el seguimiento MUST permanecer exactamente como estaba.

#### Scenario: Recuperar las mediciones relevantes

- **WHEN** la pregunta puede responderse con mediciones registradas
- **THEN** el sistema recupera las mediciones correspondientes del seguimiento
- **AND** no incluye mediciones que no respondan a la pregunta

#### Scenario: Acotar la recuperación al periodo preguntado

- **WHEN** la persona pregunta por las mediciones de una métrica en un periodo concreto
- **THEN** el sistema recupera las mediciones de ese periodo
- **AND** no incluye mediciones de fuera de ese periodo

#### Scenario: No alterar ningún registro al consultar

- **WHEN** el sistema atiende una consulta de mediciones
- **THEN** el seguimiento permanece exactamente como estaba
- **AND** ninguna medición queda modificada, añadida ni eliminada

#### Scenario: No completar lo que falta con suposiciones

- **WHEN** parte de lo que la pregunta requiere no consta entre las mediciones recuperadas
- **THEN** el sistema no completa esa parte con suposiciones ni con conocimiento general
- **AND** se apoya solo en las mediciones recuperadas

### Requirement: Responder con los valores, las unidades y las fechas registrados

El sistema MUST responder con el valor, la unidad y la fecha de cada medición tal como quedaron
registrados, sin redondearlos, sin completarlos y sin alterarlos más allá del cambio de unidad. La
respuesta MUST presentarse en la unidad de referencia de cada métrica y MUST indicarla de forma
explícita, con independencia de la unidad en la que la persona haya preguntado. Cuando la pregunta
abarque varias métricas, el sistema MUST responder por cada una con los valores y las fechas de sus
mediciones, ofreciendo una sola respuesta coherente.

#### Scenario: Responder con los valores, las unidades y las fechas registrados

- **WHEN** la persona pregunta por sus mediciones y hay mediciones registradas de la métrica preguntada
- **THEN** el sistema responde con sus valores, sus unidades y sus fechas tal como quedaron registrados
- **AND** no se altera ningún registro

#### Scenario: Responder en la unidad de referencia de la métrica

- **WHEN** la persona pregunta por sus mediciones expresando ella los valores en otra unidad
- **THEN** el sistema responde en la unidad de referencia de la métrica
- **AND** indica de forma explícita en qué unidad está respondiendo
- **AND** no deja la unidad implícita

#### Scenario: Responder por varias métricas en una sola respuesta

- **WHEN** la persona pregunta por varias métricas a la vez
- **THEN** el sistema responde por cada una en una sola respuesta
- **AND** presenta los valores y las fechas de las mediciones de cada métrica

#### Scenario: No redondear ni completar el valor registrado

- **WHEN** el sistema responde con el valor de una medición
- **THEN** el valor se presenta tal como quedó registrado
- **AND** no se redondea, no se completa ni se altera más allá del cambio de unidad

### Requirement: Presentar una métrica compuesta como una sola medición

Cuando una medición sea de una métrica compuesta, el sistema MUST presentarla con sus valores como una
sola medición y MUST NOT presentarla como mediciones distintas. La presión arterial MUST presentarse con
sus dos valores, sistólica y diastólica, como una sola medición.

#### Scenario: Responder sobre la presión arterial como una sola medición

- **WHEN** la persona pregunta por su presión arterial y hay una presión arterial registrada
- **THEN** el sistema responde con sus dos valores, sistólica y diastólica
- **AND** los presenta como una sola medición
- **AND** no los presenta como dos mediciones distintas

#### Scenario: No separar los valores de una métrica compuesta

- **WHEN** el sistema responde sobre una métrica compuesta
- **THEN** la presenta como una sola medición
- **AND** no la presenta como una medición independiente por cada valor

### Requirement: Permitir identificar las mediciones en que se apoya la respuesta

La respuesta MUST permitir a la persona identificar las mediciones registradas en las que se apoya, para
que pueda verificarlas, y cada referencia MUST corresponder a una medición realmente recuperada.

#### Scenario: Identificar las mediciones de apoyo

- **WHEN** el sistema entrega una respuesta apoyada en mediciones registradas
- **THEN** la respuesta permite identificar las mediciones en que se apoya
- **AND** cada referencia corresponde a una medición realmente recuperada

#### Scenario: No presentar como apoyo una medición no recuperada

- **WHEN** el sistema compone una respuesta apoyada en mediciones
- **THEN** no presenta como apoyo ninguna medición que no haya recuperado

### Requirement: Declarar explícitamente la ausencia de mediciones

Cuando no conste ninguna medición de la métrica preguntada, el sistema MUST declararlo de forma explícita
en español, MUST NOT responder al vacío con silencio ni con una respuesta aparente y MUST NOT sustituir
la ausencia por valores inventados. La declaración MUST referirse a lo que consta registrado y MUST NOT
afirmar ni negar que la medición haya ocurrido.

#### Scenario: Declarar que una medición no consta

- **WHEN** no hay ninguna medición registrada de la métrica por la que se pregunta
- **THEN** el sistema declara explícitamente que esa medición no consta
- **AND** no responde al vacío con silencio ni con una respuesta aparente

#### Scenario: La ausencia no afirma ni niega que la medición ocurriera

- **WHEN** el sistema declara que no consta ninguna medición de la métrica preguntada
- **THEN** la declaración se refiere a lo que consta en el seguimiento
- **AND** no afirma ni niega que la medición haya ocurrido

#### Scenario: No inventar valores para cubrir una ausencia

- **WHEN** no consta ninguna medición de la métrica preguntada
- **THEN** el sistema no inventa valores ni responde con los de otra métrica

### Requirement: Declarar una métrica que no forma parte del seguimiento

Cuando la persona pregunte por una métrica que el sistema todavía no admite, el sistema MUST declarar que
esa métrica no forma parte de su seguimiento y MUST NOT inventar valores para ella ni sustituirla por
otra métrica.

#### Scenario: Declarar que una métrica no forma parte del seguimiento

- **WHEN** la persona pregunta por una métrica que el sistema todavía no admite
- **THEN** el sistema declara que esa métrica no forma parte de su seguimiento
- **AND** no inventa valores para ella

#### Scenario: No sustituir la métrica no admitida por otra

- **WHEN** la persona pregunta por una métrica que el sistema todavía no admite
- **THEN** el sistema no responde con los valores de otra métrica
- **AND** no presenta la métrica preguntada como si estuviera en el seguimiento

### Requirement: Declinar interpretaciones y recomendaciones sobre las mediciones

Cuando la persona pida una interpretación de sus valores o una recomendación sobre ellos, el sistema MUST
declinar la petición de forma explícita y comprensible en español, MUST NOT sustituirla por una
interpretación inventada y MUST ofrecerle los valores registrados. El sistema MUST NOT valorar los
valores, diagnosticar ni recomendar nada sobre ellos.

#### Scenario: Declinar una interpretación o recomendación

- **WHEN** la persona pide una interpretación de sus valores o una recomendación sobre ellos
- **THEN** el sistema declina la petición de forma explícita y comprensible
- **AND** no la sustituye por una interpretación inventada
- **AND** no valora los valores ni recomienda nada sobre ellos

#### Scenario: Ofrecer los valores registrados tras declinar

- **WHEN** el sistema declina una interpretación o una recomendación sobre los valores
- **THEN** ofrece los valores registrados de las mediciones recuperadas
- **AND** no presenta la declinación como una ausencia de mediciones

### Requirement: Informar de un fallo al recuperar sin presentarlo como ausencia

Cuando el sistema no pueda recuperar las mediciones, MUST informar de un fallo recuperable en español,
MUST NOT presentarlo como una ausencia de mediciones, MUST NOT entregar una respuesta sin respaldo ni con
valores inventados, y MUST ofrecer reintentar sin exponer detalles internos.

#### Scenario: Informar de un fallo al recuperar las mediciones

- **WHEN** el sistema no puede recuperar las mediciones
- **THEN** informa del fallo en español, sin exponer detalles internos
- **AND** ofrece reintentar
- **AND** no lo presenta como una ausencia de mediciones

#### Scenario: No sustituir el fallo por una respuesta sin respaldo

- **WHEN** la recuperación de las mediciones no puede completarse
- **THEN** el sistema no entrega una respuesta apoyada en valores inventados
- **AND** no declara la ausencia de unas mediciones que no pudo verificar
