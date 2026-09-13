# UC-004 — Registrar un evento clínico — Acceptance Criteria

## Feature

**ID:** UC-004  
**Use case:** Registrar un evento clínico  
**Source:** [UC-004.md](./UC-004.md)

La persona puede dejar constancia de hechos médicos propios en su historia clínica y recibir una confirmación clara de lo registrado.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario incluye una referencia al flujo, excepción o regla de origen.

## Scenarios

### Scenario: Registrar un hecho médico con información suficiente

**Ref:** Flujo principal  

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And el mensaje contiene un hecho médico propio con información suficiente para registrarlo  
When la persona envía el mensaje  
Then el sistema incorpora el hecho a su historia clínica con su tipo, contenido y fecha con el grado de precisión aportado  
And el hecho queda disponible para futuras consultas de inmediato  
And la persona recibe una confirmación breve que resume el hecho registrado

### Scenario: Conservar una fecha aproximada

**Ref:** A1 — El mensaje expresa una fecha aproximada  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene un hecho médico propio y una fecha aproximada, como “hace unos dos años” o “el mes pasado”  
When la persona envía el mensaje  
Then el sistema registra el hecho conservando la imprecisión temporal expresada  
And la fecha no se presenta como una fecha exacta  
And la confirmación resume el hecho sin inventar una precisión mayor

### Scenario: Registrar un hecho sin fecha conocida

**Ref:** A2 — El mensaje no indica ninguna fecha  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene un hecho médico propio pero no indica una fecha  
When la persona envía el mensaje  
Then el sistema registra el hecho sin fecha  
And deja constancia de que la fecha es desconocida  
And la persona recibe una confirmación breve del hecho registrado

### Scenario: Resolver una expresión temporal relativa

**Ref:** A3 — El mensaje expresa una fecha relativa  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene un hecho médico propio con una expresión temporal relativa, como “hoy”, “ayer” o “hace dos semanas”  
When la persona envía el mensaje  
Then el sistema interpreta la expresión tomando como referencia el día actual  
And registra el hecho con la fecha resultante y conserva la expresión temporal original  
And la persona recibe una confirmación breve del hecho registrado

### Scenario: Registrar varios hechos médicos del mismo mensaje

**Ref:** A4 — El mensaje contiene varios hechos médicos distintos  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene varios hechos médicos propios que pueden distinguirse entre sí  
When la persona envía el mensaje  
Then el sistema registra un evento independiente por cada hecho médico  
And la persona recibe una confirmación que informa de los hechos registrados

### Scenario: Pedir aclaración antes de registrar un mensaje ambiguo

**Ref:** A5 — El mensaje es ambiguo o carece de información suficiente  

Given la persona tiene acceso al chat del asistente  
And el mensaje podría referirse a un hecho médico pero no permite determinarlo con suficiente claridad  
When la persona envía el mensaje  
Then el sistema pide una aclaración comprensible en español  
And no incorpora ningún evento a la historia clínica  
And la historia clínica permanece sin cambios hasta recibir la aclaración

### Scenario: No registrar una creencia o sospecha como hecho

**Ref:** A6 — El mensaje expresa una creencia o sospecha  

Given la persona tiene acceso al chat del asistente  
And el mensaje expresa una creencia o sospecha, como “creo que podría tener hipertensión”, y no un hecho vivido  
When la persona envía el mensaje  
Then el sistema no registra el mensaje como un hecho clínico  
And explica a la persona que no se ha registrado como hecho  
And la historia clínica permanece sin cambios

### Scenario: Responder normalmente a un mensaje sin hecho médico

**Ref:** A7 — El mensaje no contiene ningún hecho médico  

Given la persona tiene acceso al chat del asistente  
And el mensaje contiene una pregunta o una conversación general sin ningún hecho médico  
When la persona envía el mensaje  
Then el sistema responde normalmente a la conversación  
And no incorpora ningún evento a la historia clínica

### Scenario: Evitar duplicar un hecho ya registrado en la conversación

**Ref:** A8 — La persona repite un hecho registrado en la conversación en curso  

Given la persona ya ha registrado ese mismo hecho médico en la conversación en curso  
When la persona vuelve a contar el hecho  
Then el sistema reconoce que el hecho ya está registrado  
And no crea un evento nuevo  
And se lo indica mediante la confirmación del hecho existente  
And el evento original conserva su contenido y precisión temporal

### Scenario: No registrar si la persona no aporta la aclaración solicitada

**Ref:** E1 — No se aporta la aclaración pedida  

Given el sistema ha pedido a la persona que aclare un mensaje antes de registrarlo  
When la persona no aporta la aclaración necesaria  
Then el sistema no registra ningún evento  
And la historia clínica permanece exactamente como estaba antes del mensaje ambiguo

### Scenario: Informar un fallo sin dejar un registro incompleto

**Ref:** E2 — El sistema no puede completar el registro  

Given la persona ha enviado un mensaje que contiene un hecho médico registrable  
When el sistema no puede completar el registro  
Then informa del fallo en español sin mostrar detalles internos  
And no deja ningún evento registrado de forma incompleta  
And la historia clínica permanece sin cambios  
And ofrece a la persona volver a intentarlo

## Coverage notes

- **Happy path:** Cubierto por `Registrar un hecho médico con información suficiente`.
- **Alternate flows:** Cubiertos por `Conservar una fecha aproximada` (`A1`), `Registrar un hecho sin fecha conocida` (`A2`), `Resolver una expresión temporal relativa` (`A3`), `Registrar varios hechos médicos del mismo mensaje` (`A4`), `Pedir aclaración antes de registrar un mensaje ambiguo` (`A5`), `No registrar una creencia o sospecha como hecho` (`A6`), `Responder normalmente a un mensaje sin hecho médico` (`A7`) y `Evitar duplicar un hecho ya registrado en la conversación` (`A8`).
- **Exceptions:** Cubiertas por `No registrar si la persona no aporta la aclaración solicitada` (`E1`) e `Informar un fallo sin dejar un registro incompleto` (`E2`).
- **Edge cases:** No aplican como categoría independiente; los límites y variaciones explícitamente definidos por el caso de uso están cubiertos por el flujo principal, `A1`–`A8` y `E1`–`E2`.

## Conversation integration scenarios

Estos escenarios completan la frontera entre el chat, el intérprete de lenguaje
natural y el registro clínico. No sustituyen las reglas clínicas anteriores.

### Scenario: Reintentar el mismo mensaje sin duplicar el evento

**Ref:** Contrato de conversación — idempotencia

Given la persona envía un mensaje con un `conversationId` y un `messageId`
And el sistema completa el registro
When la misma solicitud se repite con los mismos identificadores
Then el sistema devuelve el resultado original
And no invoca una nueva escritura ni crea otro evento

### Scenario: Rechazar una respuesta estructurada inválida del intérprete

**Ref:** Contrato LLM — validación antes de persistir

Given la persona envía un mensaje registrable
When el intérprete devuelve JSON malformado, un tipo no admitido o campos incompletos
Then el sistema informa de un fallo controlado en español
And no persiste ningún evento
And no muestra el prompt ni detalles internos del proveedor

### Scenario: Informar indisponibilidad temporal del proveedor

**Ref:** Contrato LLM — timeout o proveedor no disponible

Given la persona envía un mensaje al asistente
When el proveedor no responde dentro del tiempo configurado o está indisponible
Then el sistema informa de un fallo recuperable en español
And conserva el texto enviado para que pueda reintentarse
And la historia clínica permanece sin cambios

### Scenario: Continuar una aclaración durante la sesión

**Ref:** Estado conversacional efímero

Given el asistente solicita una aclaración antes de registrar un hecho
When la persona responde en la misma conversación activa
Then el sistema interpreta la respuesta junto con el mensaje pendiente
And registra solo el hecho validado

### Scenario: Perder una aclaración tras reiniciar el backend

**Ref:** Estado conversacional efímero

Given existe una aclaración pendiente en memoria
When el backend se reinicia antes de recibir la respuesta
Then la aclaración pendiente se descarta
And ningún evento parcial aparece en la historia clínica
And los eventos ya persistidos como Markdown permanecen disponibles
