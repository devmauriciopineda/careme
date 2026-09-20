## Purpose

Declarar el origen de todo hecho registrado desde una consulta —quién lo aportó, cuándo se registró y de
qué consulta procede— para que la historia clínica pueda dar cuenta de dónde salió cada afirmación.

## ADDED Requirements

### Requirement: Declarar la procedencia de todo hecho registrado desde una consulta

Todo hecho registrado al cerrar una consulta MUST declarar su procedencia: quién lo aportó, cuándo se
registró y de qué consulta procede. La procedencia MUST fijarse en el momento del registro y MUST NOT
modificarse después.

#### Scenario: Registrar un hecho con su procedencia

- **WHEN** un hecho recogido en una consulta se registra al cerrarla
- **THEN** el hecho declara quién lo aportó, cuándo se registró y de qué consulta procede
- **AND** la consulta declarada es la que estaba en curso

#### Scenario: Atribuir la procedencia a la persona

- **WHEN** el hecho registrado procede de lo que la persona contó
- **THEN** su procedencia identifica a la persona como quien lo aportó

#### Scenario: No alterar la procedencia de un hecho ya registrado

- **WHEN** un hecho ya registrado se vuelve a mencionar, en la misma conversación o en otra
- **THEN** el hecho registrado no se reescribe y su procedencia permanece como estaba
- **AND** si la mención ocurre en otra consulta, se registra como un hecho propio con su procedencia

### Requirement: Conservar la procedencia como parte de la fuente de verdad

La procedencia MUST conservarse junto al hecho y MUST sobrevivir a la reconstrucción del índice derivado,
de modo que ningún hecho registrado quede sin un origen identificable.

#### Scenario: Sobrevivir a la reconstrucción del índice

- **WHEN** el índice derivado se reconstruye desde los documentos de la fuente de verdad
- **THEN** la procedencia de cada hecho se conserva
- **AND** ningún hecho queda sin origen identificable

#### Scenario: No atribuir una consulta inexistente

- **WHEN** un hecho se registró sin haber sido recogido en una consulta
- **THEN** el sistema no le atribuye una consulta de origen
- **AND** su procedencia no se presenta como si viniera de una consulta

### Requirement: No exponer la procedencia en la conversación

La procedencia MUST NOT exponerse en la conversación ni en la respuesta del asistente: consultarla es una
operación distinta. La confirmación del cierre MUST NOT describir el registro, la consulta ni sus
identificadores.

#### Scenario: Responder sin exponer la procedencia

- **WHEN** el sistema confirma a la persona lo que quedó registrado al cerrar una consulta
- **THEN** la confirmación no describe la procedencia ni los identificadores de la consulta
- **AND** no expone detalles internos del sistema

#### Scenario: Cubrir hecho y respuesta sin enumerar el origen

- **WHEN** el cierre registra varios hechos y la persona recibe la confirmación
- **THEN** la confirmación resume lo registrado sin enumerar la procedencia de cada hecho
- **AND** no describe cómo se decidió el registro
