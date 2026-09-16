# UC-010 — Consultar información que no pertenece a la historia clínica — Acceptance Criteria

## Feature

**ID:** UC-010  
**Use case:** Consultar información que no pertenece a la historia clínica  
**Source:** [UC-010.md](./UC-010.md)

La persona puede conversar con el asistente sobre asuntos que no dependen de sus
registros clínicos y recibir una respuesta útil, sin que se busque en su historia
ni se presente nada como un hecho registrado.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario incluye una referencia al flujo, excepción o regla de origen.

## Scenarios

### Scenario: Responder una pregunta de carácter general sin consultar la historia

**Ref:** Flujo principal  

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And la pregunta no depende de su historia clínica  
When la persona envía la pregunta  
Then el sistema responde como conversación general en español  
And no busca en la historia clínica  
And no presenta ninguna parte de la respuesta como un hecho de su historia  
And la historia clínica no se modifica

### Scenario: Explicar un concepto médico sin aplicarlo al caso de la persona

**Ref:** A1 — La pregunta pide una explicación general de un concepto médico  

Given la persona tiene acceso al chat del asistente  
And la pregunta pide la explicación general de un concepto médico  
When la persona envía la pregunta  
Then el sistema explica el concepto en términos generales  
And no lo aplica al caso concreto de la persona  
And no interpreta sus datos  
And no recomienda tratamiento  
And la historia clínica permanece sin cambios

### Scenario: Separar una pregunta general de la parte que depende de la historia

**Ref:** A2 — La pregunta mezcla un asunto general con información propia  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene una pregunta general y una parte que depende de su historia clínica  
When la persona envía el mensaje  
Then el sistema responde la parte general como conversación  
And atiende la parte que depende de su historia por el cauce que corresponda  
And no mezcla ambas partes en una misma afirmación  
And la historia clínica no se modifica

### Scenario: Atender como consulta de la historia una pregunta coloquial sobre registros

**Ref:** A3 — La pregunta es una reformulación coloquial de algo registrado  

Given la historia clínica contiene hechos que responden a lo que la persona pregunta  
And la persona formula la pregunta en lenguaje coloquial  
When la persona envía la pregunta  
Then el sistema reconoce que la pregunta depende de su historia clínica  
And la atiende como consulta de la historia conforme a UC-007  
And no la responde como conversación general

### Scenario: Responder una fórmula de cortesía sin mencionar registros

**Ref:** A4 — El mensaje es un saludo, una despedida o una cortesía  

Given la persona tiene acceso al chat del asistente  
And el mensaje es un saludo, una despedida o una fórmula de cortesía sin contenido clínico  
When la persona envía el mensaje  
Then el sistema responde de forma breve y natural  
And no busca en la historia clínica  
And no menciona registros clínicos  
And la historia clínica permanece sin cambios

### Scenario: Declinar una petición de recomendación o diagnóstico

**Ref:** A5 — La persona pide una recomendación, un diagnóstico o una interpretación  

Given la persona tiene acceso al chat del asistente  
When la persona pide una recomendación, un diagnóstico o una interpretación de su caso  
Then el sistema declina la petición de forma explícita y comprensible en español  
And explica que no diagnostica ni recomienda tratamiento  
And no sustituye la petición por una respuesta inventada  
And la historia clínica permanece sin cambios

### Scenario: Responder sin tratar como registros los hechos mencionados en la conversación

**Ref:** A6 — La pregunta general se apoya en hechos mencionados en la conversación  

Given la persona mantiene una conversación activa y ha mencionado hechos propios en ella  
And la pregunta no pretende consultar su historia clínica  
When la persona envía la pregunta  
Then el sistema responde como conversación general  
And no trata los hechos mencionados como registros clínicos  
And no busca en la historia clínica  
And la historia clínica permanece sin cambios

### Scenario: Pedir aclaración cuando no puede determinarse si la pregunta depende de la historia

**Ref:** A7 — No puede determinarse si la pregunta depende de la historia  

Given la persona ha enviado una pregunta que puede o no depender de su historia clínica  
When el sistema no puede determinarlo  
Then el sistema pide una aclaración comprensible en español antes de responder  
And no asume ninguna interpretación  
And no busca en la historia clínica  
And la historia clínica permanece sin cambios

### Scenario: Informar un fallo al elaborar la respuesta conversacional

**Ref:** E1 — El sistema no logra elaborar la respuesta conversacional  

Given la persona ha enviado un mensaje que no depende de su historia clínica  
When el sistema no logra elaborar la respuesta conversacional  
Then informa de un fallo recuperable en español sin mostrar detalles internos  
And no busca en la historia clínica  
And conserva el mensaje para que la persona pueda volver a intentarlo  
And la historia clínica permanece exactamente como estaba

## Coverage notes

- **Happy path:** Cubierto por `Responder una pregunta de carácter general sin consultar la historia`.
- **Alternate flows:** Cubiertos por `Explicar un concepto médico sin aplicarlo al caso de la persona` (`A1`), `Separar una pregunta general de la parte que depende de la historia` (`A2`), `Atender como consulta de la historia una pregunta coloquial sobre registros` (`A3`), `Responder una fórmula de cortesía sin mencionar registros` (`A4`), `Declinar una petición de recomendación o diagnóstico` (`A5`), `Responder sin tratar como registros los hechos mencionados en la conversación` (`A6`) y `Pedir aclaración cuando no puede determinarse si la pregunta depende de la historia` (`A7`).
- **Exceptions:** Cubiertas por `Informar un fallo al elaborar la respuesta conversacional` (`E1`).
- **Edge cases:** No aplican como categoría independiente; los límites y variaciones explícitamente definidos por el caso de uso están cubiertos por el flujo principal, `A1`–`A7` y `E1`.

## Boundary scenarios with UC-007 and UC-008

Estos escenarios verifican la frontera declarada en la sección 11 de UC-010. No
sustituyen los escenarios anteriores.

### Scenario: Distinguir la conversación general de una respuesta fundada

**Ref:** Frontera UC-007 / UC-010 — pregunta que no depende de la historia

Given la pregunta de la persona sí depende de su historia clínica  
When la historia clínica contiene hechos que la responden  
Then el sistema se comporta conforme a UC-007 con una respuesta fundada y sus referencias  
And no responde como conversación general

### Scenario: No producir una ausencia de registros desde la conversación general

**Ref:** Frontera UC-008 / UC-010 — desenlaces mutuamente excluyentes

Given la persona envía una pregunta que no depende de su historia clínica  
When el sistema identifica que no procede buscar en la historia  
Then no declara una ausencia de registros  
And responde como conversación general  
And la historia clínica no se modifica

### Scenario: Mantener el límite clínico en una explicación general

**Ref:** Regla de negocio — relación de registro y consulta, no clínica

Given la persona pide una explicación general con posible aplicación a su caso  
When el sistema elabora la respuesta  
Then explica únicamente el concepto general  
And no interpreta datos de la persona  
And no emite diagnóstico ni recomendación de tratamiento  
And la historia clínica permanece sin cambios
