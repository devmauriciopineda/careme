# UC-007 — Consultar la historia clínica mediante lenguaje natural — Acceptance Criteria

## Feature

**ID:** UC-007  
**Use case:** Consultar la historia clínica mediante lenguaje natural  
**Source:** [UC-007.md](./UC-007.md)

La persona puede preguntar por su historia clínica con sus propias palabras y
recibir una respuesta fundada en los hechos registrados, con las referencias
correspondientes.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario incluye una referencia al flujo, excepción o regla de origen.

## Scenarios

### Scenario: Responder una pregunta sobre la historia con hechos registrados

**Ref:** Flujo principal  

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And la historia clínica contiene hechos que pueden responder la pregunta  
When la persona envía una pregunta sobre su propia historia clínica  
Then el sistema responde apoyándose únicamente en los hechos que encontró  
And la respuesta conserva el contenido y la precisión temporal registrados  
And la persona puede identificar los hechos en que se apoya la respuesta  
And la historia clínica no se modifica

### Scenario: Responder una pregunta temporal sobre un hecho concreto

**Ref:** A1 — Pregunta por un hecho concreto y su fecha  

Given la historia clínica contiene un hecho registrado con su fecha y su grado de precisión  
When la persona pregunta cuándo ocurrió ese hecho  
Then el sistema encuentra el hecho y responde indicando la fecha registrada  
And presenta la fecha con el grado de precisión con que quedó registrada

### Scenario: Responder una pregunta sobre valores o mediciones registradas

**Ref:** A2 — Pregunta por valores o mediciones  

Given la historia clínica contiene hechos de medición registrados  
When la persona pregunta qué valores tiene registrados  
Then el sistema encuentra los hechos de medición correspondientes  
And responde con los valores tal como quedaron registrados

### Scenario: Conservar la imprecisión temporal en la respuesta

**Ref:** A3 — La respuesta depende de un hecho con fecha aproximada  

Given la historia clínica contiene un hecho cuya fecha quedó registrada como aproximada  
When la persona pregunta por ese hecho  
Then el sistema responde conservando la imprecisión temporal registrada  
And no presenta la fecha como exacta

### Scenario: Responder una pregunta de seguimiento en la conversación activa

**Ref:** A4 — La pregunta es un seguimiento de la conversación en curso  

Given la persona mantiene una conversación activa sobre su historia clínica  
When la persona formula una pregunta de seguimiento que depende de los turnos recientes  
Then el sistema interpreta la pregunta junto con esos turnos recientes  
And responde apoyándose en los hechos registrados correspondientes  
And el historial no se conserva más allá de la conversación activa

### Scenario: Responder una pregunta que requiere varios hechos

**Ref:** A5 — La pregunta necesita varios hechos o más de una búsqueda  

Given la historia clínica contiene varios hechos necesarios para responder  
When la persona formula una pregunta que abarca más de un hecho  
Then el sistema reúne todos los hechos necesarios  
And ofrece una sola respuesta coherente apoyada en ellos

### Scenario: Remitir el peso y la circunferencia abdominal a su seguimiento

**Ref:** A6 — La pregunta menciona peso o circunferencia abdominal  

Given la persona tiene acceso al chat del asistente  
When la persona pregunta por su peso o su circunferencia abdominal  
Then el sistema indica que ese seguimiento tiene su propio espacio  
And aclara que no forma parte de la historia clínica consultada

### Scenario: No recuperar para una pregunta que no depende de la historia

**Ref:** A7 — La pregunta no depende de la historia clínica  

Given la persona tiene acceso al chat del asistente  
When la persona formula una pregunta que no depende de su historia clínica  
Then el sistema no busca en la historia clínica  
And responde como conversación general  
And la historia clínica no se modifica

### Scenario: Declarar la ausencia de registros que respalden la pregunta

**Ref:** A8 — No existen hechos que respalden la pregunta  

Given la persona tiene acceso al chat del asistente  
And la historia clínica no contiene hechos que respondan la pregunta  
When la persona formula la pregunta  
Then el sistema responde explícitamente que no encuentra registros  
And no completa la respuesta con conocimiento general ni con suposiciones  
And la historia clínica permanece sin cambios

### Scenario: Informar un fallo al interpretar la pregunta sin inventar

**Ref:** E1 — El sistema no logra interpretar la pregunta  

Given la persona ha enviado una pregunta sobre su historia clínica  
When el sistema no logra interpretarla  
Then informa de un fallo recuperable en español sin mostrar detalles internos  
And no responde con suposiciones  
And conserva la pregunta para que la persona pueda volver a intentarlo  
And la historia clínica permanece sin cambios

### Scenario: Pedir aclaración ante una pregunta ambigua

**Ref:** E2 — La pregunta admite varias interpretaciones  

Given la persona ha enviado una pregunta que admite varias interpretaciones  
When el sistema no puede determinar qué se está consultando  
Then pide una aclaración comprensible en español  
And no asume ninguna interpretación  
And la historia clínica permanece sin cambios

### Scenario: Informar un fallo de búsqueda sin inventar una respuesta

**Ref:** E3 — El sistema no puede completar la búsqueda  

Given la persona ha enviado una pregunta sobre su historia clínica  
When el sistema no puede completar la búsqueda en la historia  
Then informa del fallo en español sin mostrar detalles internos  
And no entrega una respuesta sin respaldo  
And ofrece volver a intentarlo  
And la historia clínica permanece exactamente como estaba

### Scenario: Descartar el estado efímero tras una interrupción

**Ref:** E4 — La conversación se interrumpe antes de completar la respuesta  

Given existe una conversación activa sobre la historia clínica  
When la conversación se interrumpe antes de completar la respuesta  
Then el estado efímero de la conversación se descarta  
And la historia clínica permanece intacta  
And la persona puede volver a formular su pregunta

## Coverage notes

- **Happy path:** Cubierto por `Responder una pregunta sobre la historia con hechos registrados`.
- **Alternate flows:** Cubiertos por `Responder una pregunta temporal sobre un hecho concreto` (`A1`), `Responder una pregunta sobre valores o mediciones registradas` (`A2`), `Conservar la imprecisión temporal en la respuesta` (`A3`), `Responder una pregunta de seguimiento en la conversación activa` (`A4`), `Responder una pregunta que requiere varios hechos` (`A5`), `Remitir el peso y la circunferencia abdominal a su seguimiento` (`A6`), `No recuperar para una pregunta que no depende de la historia` (`A7`) y `Declarar la ausencia de registros que respalden la pregunta` (`A8`).
- **Exceptions:** Cubiertas por `Informar un fallo al interpretar la pregunta sin inventar` (`E1`), `Pedir aclaración ante una pregunta ambigua` (`E2`), `Informar un fallo de búsqueda sin inventar una respuesta` (`E3`) y `Descartar el estado efímero tras una interrupción` (`E4`).
- **Edge cases:** No aplican como categoría independiente; los límites y variaciones explícitamente definidos por el caso de uso están cubiertos por el flujo principal, `A1`–`A8` y `E1`–`E4`.
