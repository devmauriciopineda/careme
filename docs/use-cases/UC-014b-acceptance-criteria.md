# UC-014b — Consultar las mediciones por lenguaje natural — Acceptance Criteria

## Feature

**ID:** UC-014b  
**Use case:** Consultar las mediciones por lenguaje natural  
**Source:** [UC-014b.md](./UC-014b.md)

La persona pregunta por sus mediciones en la conversación y obtiene una respuesta con los valores y las
fechas tal como quedaron registrados, sin que se altere ningún registro.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`, usando los
  identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md) cuando corresponda.

## Scenarios

### Scenario: Responder con los valores y las fechas registrados

**Ref:** Flujo principal / UC-014b-R2, UC-014b-R3, UC-014b-R5

Given existe una consulta en curso  
And hay mediciones registradas de la métrica por la que se pregunta  
When la persona pregunta por sus mediciones  
Then el sistema determina qué métrica o métricas y qué periodo abarca la pregunta  
And recupera las mediciones que corresponden  
And responde con sus valores, sus unidades y sus fechas tal como quedaron registrados  
And permite identificar las mediciones en que se apoya  
And no se altera ningún registro

### Scenario: Declarar que una medición no consta

**Ref:** A1 / RN-010, RN-011, UC-014b-R4

Given una consulta en curso  
And no hay ninguna medición registrada de la métrica por la que se pregunta  
When la persona pregunta por esa métrica  
Then el sistema declara explícitamente que esa medición no consta  
And no responde al vacío con silencio ni con una respuesta aparente  
And no afirma ni niega que la medición haya ocurrido

### Scenario: Responder por un periodo concreto

**Ref:** A2

Given una consulta en curso con mediciones registradas de una métrica  
When la persona pregunta por las mediciones de esa métrica en un periodo concreto  
Then el sistema recupera las mediciones de ese periodo  
And responde con sus valores y sus fechas  
And no incluye mediciones de fuera de ese periodo

### Scenario: Responder por varias métricas a la vez

**Ref:** A3

Given una consulta en curso con mediciones registradas de más de una métrica  
When la persona pregunta por varias métricas a la vez  
Then el sistema responde por cada una  
And presenta los valores y las fechas de las mediciones de cada métrica

### Scenario: Responder sobre la presión arterial como una sola medición

**Ref:** A4 / UC-014b-R6

Given una consulta en curso con una presión arterial registrada con sus dos valores  
When la persona pregunta por su presión arterial  
Then el sistema responde con sus dos valores, sistólica y diastólica  
And los presenta como una sola medición  
And no los presenta como dos mediciones distintas

### Scenario: Pedir que se concrete la métrica preguntada

**Ref:** A5 / RN-023

Given una consulta en curso con mediciones registradas de más de una métrica  
When la persona pregunta por sus mediciones sin concretar cuál  
Then el sistema no supone qué métrica pregunta  
And pide que la concrete antes de responder

### Scenario: Declarar que una métrica no forma parte del seguimiento

**Ref:** A6 / RN-001

Given una consulta en curso  
And la persona pregunta por una métrica que el sistema todavía no admite  
When el sistema atiende la pregunta  
Then declara que esa métrica no forma parte de su seguimiento  
And no inventa valores para ella

### Scenario: Declinar una interpretación o recomendación sobre las mediciones

**Ref:** A7 / RN-015, RN-016

Given una consulta en curso con mediciones registradas  
When la persona pide una interpretación de sus valores o una recomendación  
Then el sistema declina la petición de forma explícita y comprensible  
And no la sustituye por una interpretación inventada  
And le ofrece los valores registrados

### Scenario: Responder en la unidad de referencia de la métrica

**Ref:** A8 / UC-014b-R9

Given una consulta en curso con mediciones registradas de una métrica  
When la persona pregunta por ellas expresando ella los valores en otra unidad  
Then el sistema responde en la unidad de referencia de la métrica  
And indica de forma explícita en qué unidad está respondiendo  
And no deja la unidad implícita

### Scenario: Informar de un fallo al recuperar las mediciones

**Ref:** E1 / RN-012

Given una consulta en curso  
And la persona pregunta por sus mediciones  
When el sistema no puede recuperarlas  
Then el sistema informa del fallo en el idioma de la persona  
And ofrece reintentar  
And no lo presenta como una ausencia de mediciones

## Coverage notes

- **Happy path:** cubierto por `Responder con los valores y las fechas registrados`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6`, `A7` y `A8` tienen un escenario cada uno.
- **Exceptions:** `E1` tiene un escenario. No aplica ninguna otra excepción a este caso de uso.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan cubiertos dentro de
  los escenarios existentes: métrica sin mediciones (`A1`), periodo concreto (`A2`), medición compuesta
  (`A4`), pregunta sin métrica concreta (`A5`), métrica todavía no admitida (`A6`) y pregunta expresada
  en otra unidad (`A8`).
- **Reglas sin escenario propio:** RN-002, RN-003, RN-005, RN-006, RN-007, RN-009, RN-013, RN-023,
  RN-025, RN-026, RN-027, RN-029 y RN-030 son reglas transversales que se verifican en los escenarios de
  sus casos de uso de origen; `RN-025` se ejercita como «no se altera ningún registro» en el escenario
  principal, y `RN-003` como «permite identificar las mediciones en que se apoya».
- **Automatización:** este caso de uso todavía no está implementado, de modo que aún no existe una
  prueba que lo cubra. Los escenarios están redactados para poder convertirse en pruebas sin añadir
  decisiones de negocio.
