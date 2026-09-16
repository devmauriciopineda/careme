# UC-008 — Gestionar información clínica ausente — Acceptance Criteria

## Feature

**ID:** UC-008  
**Use case:** Gestionar información clínica ausente  
**Source:** [UC-008.md](./UC-008.md)

La persona recibe una declaración explícita y comprensible cuando su historia
clínica no respalda la información que pregunta, sin que el asistente complete
el vacío con suposiciones.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario incluye una referencia al flujo, excepción o regla de origen.

## Scenarios

### Scenario: Declarar la ausencia de registros ante una pregunta no respaldada

**Ref:** Flujo principal  

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And la pregunta pretende consultar la historia clínica  
And la historia clínica no contiene ningún hecho que responda la pregunta  
When la persona envía la pregunta  
Then el sistema declara explícitamente que no encuentra registros que respalden lo preguntado  
And no presenta ninguna afirmación como si estuviera respaldada  
And indica que la ausencia se refiere a lo que consta en la historia y no a lo que haya ocurrido  
And ofrece continuar reformulando la pregunta o registrando el hecho si no consta  
And la historia clínica permanece exactamente como estaba

### Scenario: Declarar el estado inicial cuando la historia está vacía

**Ref:** A1 — La historia clínica está completamente vacía  

Given la persona tiene acceso al chat del asistente  
And la historia clínica no contiene ningún hecho registrado  
When la persona pregunta por información clínica propia  
Then el sistema declara que todavía no hay registros en la historia  
And presenta esa situación como el estado inicial esperable y no como un error  
And no completa la respuesta con conocimiento general ni con suposiciones

### Scenario: Declarar que no hay registros de un tipo de hecho

**Ref:** A2 — La pregunta busca un tipo de hecho que nunca se registró  

Given la persona tiene acceso al chat del asistente  
And la historia clínica no contiene hechos del tipo por el que se pregunta  
When la persona pregunta por ese tipo de hecho  
Then el sistema declara que no encuentra registros de ese tipo  
And no deduce la respuesta a partir de otros hechos registrados  
And la historia clínica permanece sin cambios

### Scenario: Acotar la ausencia a un periodo concreto

**Ref:** A3 — La pregunta busca un hecho en un periodo sin resultados  

Given la persona tiene acceso al chat del asistente  
And la historia clínica no contiene hechos en el periodo por el que se pregunta  
When la persona pregunta por ese periodo  
Then el sistema declara que no encuentra registros en ese periodo  
And aclara que la ausencia se refiere a ese periodo y no a toda la historia  
And no completa la respuesta con suposiciones

### Scenario: Declarar la ausencia ante términos que no coinciden con el registro

**Ref:** A4 — Las palabras de la pregunta no coinciden con las del registro  

Given la persona tiene acceso al chat del asistente  
And la búsqueda no encuentra hechos con los términos empleados en la pregunta  
When la persona envía la pregunta  
Then el sistema declara que no encuentra registros con esos términos  
And ofrece reformular la pregunta con otras palabras  
And no presenta ninguna afirmación como si estuviera respaldada

### Scenario: Responder la parte respaldada y declarar la parte ausente

**Ref:** A5 — Parte de la pregunta puede responderse y parte no  

Given la persona tiene acceso al chat del asistente  
And la historia clínica contiene hechos que responden solo una parte de la pregunta  
When la persona envía la pregunta  
Then el sistema responde únicamente con la parte respaldada por registros  
And declara explícitamente qué parte de la pregunta no tiene registros  
And no completa la parte ausente con conocimiento general ni con suposiciones

### Scenario: Mantener la declaración ante la insistencia

**Ref:** A6 — La persona insiste en la misma pregunta tras la negativa  

Given el sistema ha declarado que no hay registros que respondan la pregunta  
When la persona insiste en la misma pregunta  
Then el sistema mantiene la misma declaración de ausencia  
And no fabrica información para satisfacer la petición  
And la historia clínica permanece sin cambios

### Scenario: No declarar ausencia para una pregunta de carácter general

**Ref:** A7 — La pregunta nunca pretendió consultar la historia  

Given la persona tiene acceso al chat del asistente  
And la pregunta no depende de su historia clínica  
When la persona envía la pregunta  
Then el sistema no declara una ausencia de registros  
And atiende la pregunta como conversación general  
And la historia clínica no se modifica

### Scenario: Informar un fallo en lugar de declarar una ausencia no comprobada

**Ref:** E1 — No se puede completar la búsqueda en la historia  

Given la persona ha enviado una pregunta sobre su historia clínica  
When el sistema no puede completar la búsqueda en la historia  
Then informa del fallo en español sin mostrar detalles internos  
And no declara una ausencia de registros que no ha podido comprobar  
And conserva la pregunta para que la persona pueda volver a intentarlo  
And la historia clínica permanece exactamente como estaba

## Coverage notes

- **Happy path:** Cubierto por `Declarar la ausencia de registros ante una pregunta no respaldada`.
- **Alternate flows:** Cubiertos por `Declarar el estado inicial cuando la historia está vacía` (`A1`), `Declarar que no hay registros de un tipo de hecho` (`A2`), `Acotar la ausencia a un periodo concreto` (`A3`), `Declarar la ausencia ante términos que no coinciden con el registro` (`A4`), `Responder la parte respaldada y declarar la parte ausente` (`A5`), `Mantener la declaración ante la insistencia` (`A6`) y `No declarar ausencia para una pregunta de carácter general` (`A7`).
- **Exceptions:** Cubiertas por `Informar un fallo en lugar de declarar una ausencia no comprobada` (`E1`).
- **Edge cases:** No aplican como categoría independiente; los límites y variaciones explícitamente definidos por el caso de uso están cubiertos por el flujo principal, `A1`–`A7` y `E1`.

## Boundary scenarios with UC-007 and UC-010

Estos escenarios verifican la frontera declarada en la sección 11 de UC-008. No
sustituyen los escenarios anteriores.

### Scenario: Distinguir la ausencia de registros de un fallo de búsqueda

**Ref:** Frontera UC-007 / UC-008 — fallo de búsqueda

Given la persona ha enviado una pregunta sobre su historia clínica  
When la búsqueda no puede completarse  
Then el sistema se comporta conforme a las excepciones de UC-007 y no declara una ausencia  
And la persona puede distinguir ese resultado de una ausencia de registros

### Scenario: Distinguir la ausencia de registros de una respuesta fundamentada

**Ref:** Frontera UC-007 / UC-008 — respuesta respaldada

Given la historia clínica contiene hechos que responden la pregunta  
When la persona envía la pregunta  
Then el sistema responde conforme a UC-007 con una respuesta fundada y sus referencias  
And no declara ninguna ausencia de registros

### Scenario: Distinguir la ausencia de registros de una consulta general

**Ref:** Frontera UC-008 / UC-010 — pregunta que no consulta la historia

Given la persona envía una pregunta que no depende de su historia clínica  
When el sistema identifica que no procede buscar en la historia  
Then el sistema se comporta conforme a UC-010  
And no declara ninguna ausencia de registros

### Scenario: Ofrecer el registro del hecho que no consta

**Ref:** Regla de negocio — salida accionable

Given el sistema ha declarado que no hay registros que respondan la pregunta  
When la persona indica que el hecho sí ocurrió pero no consta  
Then el sistema le ofrece registrarlo conforme a UC-004  
And no incorpora ningún registro por su cuenta durante esta respuesta  
And la historia clínica permanece sin cambios hasta que se registre el hecho
