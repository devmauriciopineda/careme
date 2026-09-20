# UC-012 — Atender un mensaje que requiere varias operaciones — Acceptance Criteria

## Feature

**ID:** UC-012  
**Use case:** Atender un mensaje que requiere varias operaciones  
**Source:** [UC-012.md](./UC-012.md)

La persona escribe con sus propias palabras y el sistema decide y ejecuta lo que
el mensaje necesita —consultar, registrar o ambas cosas— entregando una sola
respuesta, sin que la persona declare qué quiere ni repita su mensaje.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante
  `Ref`, usando los identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md)
  cuando corresponda.

## Scenarios

### Scenario: Atender en un solo intercambio un mensaje que requiere varias operaciones

**Ref:** Flujo principal

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And el asistente solo puede consultar la historia y registrar hechos  
And el mensaje de la persona no declara qué operación debe realizarse  
When la persona envía un mensaje que requiere más de una operación  
Then el sistema determina las operaciones que necesita y las ejecuta en el orden necesario  
And comprueba cada operación antes de que cualquier hecho se incorpore a la historia  
And responde una sola vez, cubriendo todo lo que el mensaje requería  
And no expone las operaciones que ejecutó  
And la persona no tiene que repetir su mensaje

### Scenario: Registrar un hecho y responder una pregunta en el mismo mensaje

**Ref:** A1 — El mensaje contiene un hecho y una pregunta sobre la historia

Given la historia clínica contiene hechos registrados  
When la persona cuenta un hecho nuevo y en el mismo mensaje pregunta por su historia  
Then el sistema registra el hecho nuevo  
And responde la pregunta con los hechos que encuentra  
And entrega ambas cosas en una sola respuesta  
And el hecho registrado puede consultarse de inmediato

### Scenario: Encadenar varias consultas antes de responder

**Ref:** A2 — Responder exige consultar la historia más de una vez

Given la historia clínica contiene hechos que deben combinarse para responder  
When la persona formula un mensaje cuya respuesta requiere más de una consulta  
Then el sistema realiza las consultas que necesita, sirviéndose del resultado de una para decidir la siguiente  
And ofrece una sola respuesta coherente apoyada en los hechos encontrados  
And la historia clínica no se modifica

### Scenario: Registrar varios hechos distintos del mismo mensaje

**Ref:** A3 — El mensaje contiene varios hechos médicos distintos

Given la persona tiene acceso al chat del asistente  
When la persona cuenta varios hechos médicos distintos en un mismo mensaje  
Then el sistema registra un hecho por cada uno de ellos  
And lo confirma en una sola respuesta

### Scenario: Completar un registro con lo que consta en la historia

**Ref:** A4 — El registro solo puede completarse con información ya registrada

Given la historia clínica contiene el hecho del que depende el registro  
When la persona registra un hecho sin repetir la información que ya consta  
Then el sistema consulta la historia para completar lo que falta  
And registra el hecho con lo que encuentra  
And no inventa la información que no encuentra

### Scenario: Pedir la información que falta sin ejecutar la operación

**Ref:** A5 / RN-023 — Falta información para completar la operación

Given la persona tiene acceso al chat del asistente  
When la persona envía un mensaje que no aporta lo necesario para completar una operación  
Then el sistema pide la información que falta  
And no ejecuta esa operación  
And no registra nada por ahora  
And retoma la operación cuando la persona aporta la información

### Scenario: Registrar lo registrable y declinar la parte clínica

**Ref:** A6 / RN-016 — El mensaje mezcla un hecho con una petición clínica

Given la persona tiene acceso al chat del asistente  
When la persona cuenta un hecho y en el mismo mensaje pide una recomendación, un diagnóstico o una interpretación  
Then el sistema registra el hecho  
And declina la parte clínica de forma explícita y comprensible  
And no sustituye la parte declinada por una respuesta inventada  
And la historia clínica no contiene ninguna interpretación ni recomendación

### Scenario: No ejecutar ninguna operación cuando el mensaje no las requiere

**Ref:** A7 / RN-026 — El mensaje no requiere operaciones sobre la historia

Given la persona tiene acceso al chat del asistente  
When la persona envía un mensaje que no requiere consultar ni registrar nada  
Then el sistema no busca en la historia clínica  
And no registra nada  
And responde como conversación general  
And la historia clínica permanece exactamente como estaba

### Scenario: Declarar que una parte del mensaje no puede atenderse

**Ref:** A8 — Ninguna operación disponible puede producir lo que se pide

Given el asistente solo dispone de las operaciones de consultar y registrar  
When la persona pide algo que ninguna de esas operaciones puede producir  
Then el sistema declara que no puede atender esa parte  
And no la responde con una suposición

### Scenario: No incorporar un hecho que no supera la comprobación previa

**Ref:** E1 / RN-018, RN-020 — La operación no supera la comprobación previa

Given la persona ha enviado un mensaje que contiene un hecho  
When la operación no supera la comprobación previa al registro  
Then el sistema no incorpora nada a la historia  
And explica por qué el hecho no puede registrarse  
And no deja ningún registro a medias  
And la historia permanece exactamente como estaba

### Scenario: Informar de una operación que falla tras otra completada

**Ref:** E2 — Una operación falla después de que otra ya se completó

Given la persona ha enviado un mensaje que requiere más de una operación  
When una operación falla después de que otra se haya completado  
Then el sistema informa de la parte que no pudo completarse  
And informa de lo que sí quedó registrado  
And ofrece reintentar la parte fallida  
And no presenta como completo lo que no lo está

### Scenario: Fallo controlado cuando el asistente no está disponible

**Ref:** E3 — El asistente no está disponible o no está configurado

Given la persona ha enviado un mensaje en el chat  
And el asistente real no está disponible o no está configurado  
When el sistema intenta atender el mensaje  
Then informa de un fallo recuperable en español, sin detalles internos  
And no ejecuta ninguna operación  
And no registra nada  
And no entrega una respuesta inventada  
And la persona conserva su mensaje para volver a intentarlo

### Scenario: No ejecutar una operación que el asistente no tiene permitido realizar

**Ref:** E4 / RN-015 — Operación fuera del conjunto disponible

Given el asistente solo puede consultar la historia y registrar hechos  
When el mensaje pide borrar o modificar hechos ya registrados, o consultar la historia de otra persona  
Then el sistema no ejecuta la operación  
And explica que no puede hacerla  
And la historia clínica permanece exactamente como estaba

### Scenario: Interrupción antes de completar la respuesta

**Ref:** E5 — La conversación se interrumpe

Given la persona ha enviado un mensaje que requiere registrar un hecho y responder una pregunta  
When la conversación se interrumpe antes de completar la respuesta  
Then los hechos que ya quedaron registrados permanecen en la historia  
And no se completa la respuesta  
And la persona puede volver a pedirla

## Coverage notes

- **Happy path:** cubierto por `Atender en un solo intercambio un mensaje que
  requiere varias operaciones`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6`, `A7` y `A8` tienen un
  escenario cada uno.
- **Exceptions:** `E1`, `E2`, `E3`, `E4` y `E5` tienen un escenario cada uno.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan
  cubiertos dentro de los escenarios existentes: registro y consulta del mismo
  hecho en un solo intercambio (`A1`), precisión temporal aportada al registrar
  dentro de un mensaje mixto (`A5` con RN-006) y mensaje que no requiere ninguna
  operación (`A7`).
- **Reglas sin escenario propio:** RN-004, RN-009, RN-011, RN-013, RN-017,
  RN-019, RN-025, RN-030, RN-031 y RN-032 son reglas de otros casos de uso o
  están previstas para subfases posteriores; se verifican en los escenarios de
  sus casos de uso de origen, no aquí.
