# UC-013 — Sostener una consulta — Acceptance Criteria

## Feature

**ID:** UC-013  
**Use case:** Sostener una consulta  
**Source:** [UC-013.md](./UC-013.md)

La conversación es una consulta con identidad propia: empieza cuando la persona inicia la
conversación, la persona decide cuándo termina, y durante ella el asistente recoge todo lo que la
persona menciona sobre su salud tomando notas, para que al cierre quede registrado.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`, usando los
  identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md) cuando corresponda.

## Scenarios

### Scenario: Sostener la consulta y cerrarla con lo que la persona cuenta

**Ref:** Flujo principal / UC-013-R1, UC-013-R2

Given el sistema está disponible y la persona tiene acceso al chat del asistente  
And no hay ninguna otra consulta en curso  
When la persona inicia una conversación, cuenta lo que necesita con sus propias palabras y decide dar por terminada la conversación  
Then el sistema abre con ella una consulta con identidad propia y la mantiene en curso durante toda la conversación  
And procura recoger toda la información posible de los hechos que la persona menciona, completando los datos que faltan, pidiendo aclaración de lo ambiguo y ampliando el contexto  
And toma nota de cada hecho con la información disponible y su precisión temporal, sin incorporarlo a la historia mientras la consulta está en curso  
And al cerrar la consulta lo recogido queda registrado con su procedencia (UC-013b)  
And la persona no tiene que declarar qué debía registrarse

### Scenario: Recoger un hecho que no era el foco del mensaje

**Ref:** A1 / UC-013-R5

Given la persona está conversando dentro de una consulta en curso  
When menciona un hecho clínico que no era el foco de su mensaje  
Then el sistema lo toma en nota igualmente  
And lo tiene en cuenta al cerrar la consulta

### Scenario: Recoger varios hechos distintos a lo largo de la conversación

**Ref:** A2 / UC-013-R5

Given la persona está conversando dentro de una consulta en curso  
When menciona varios hechos médicos distintos a lo largo de la conversación  
Then el sistema los recoge por separado  
And no fusiona unos con otros  
And no da por hecho uno por haber contado otro

### Scenario: Seguir preguntando mientras falta información de un hecho

**Ref:** A3 / UC-013-R6, RN-023

Given la persona está conversando dentro de una consulta en curso  
When la información de un hecho mencionado no queda completa  
Then el sistema sigue conversando y pregunta lo que falta  
And no completa lo que falta con suposiciones  
And al cerrar, el hecho se registra con lo disponible si alcanza la información mínima  
And si no la alcanza, no se registra

### Scenario: Cerrar la consulta aunque quede información por recoger

**Ref:** A4 / UC-013-R7

Given una consulta en curso con información pendiente de recoger  
When la persona pide terminar la consulta  
Then el sistema cierra con lo recogido hasta ese momento  
And no retiene a la persona ni bloquea su decisión

### Scenario: Volver sobre un asunto ya tratado en la misma consulta

**Ref:** A5

Given una consulta en curso en la que ya se trató un asunto  
When la persona vuelve sobre ese asunto en la misma consulta  
Then el sistema lo reconoce dentro de la consulta  
And no lo trata como un hecho nuevo  
And no duplica nada

### Scenario: No retomar una consulta cerrada

**Ref:** A6 / UC-013-R2

Given existe una consulta ya cerrada  
When la persona intenta retomarla o reabrirla  
Then el sistema explica que una consulta cerrada no se retoma  
And le indica que puede contar lo que necesite en una consulta nueva  
And la consulta cerrada permanece cerrada

### Scenario: Cerrar la consulta anterior al iniciar una conversación nueva

**Ref:** A7 / UC-013-R3

Given existe una consulta en curso  
When la persona inicia una conversación nueva  
Then el sistema cierra la consulta anterior con lo que tenía (UC-013b)  
And abre la nueva consulta  
And no quedan dos consultas en curso

### Scenario: No hacer lo que el asistente no tiene permitido

**Ref:** A8 / RN-015

Given la persona está conversando dentro de una consulta en curso  
And el asistente solo puede consultar la historia y tomar notas  
When la persona pide algo que el asistente no tiene permitido hacer, como borrar o modificar hechos ya registrados o consultar a otra persona  
Then el sistema no lo hace  
And lo explica de forma comprensible  
And no lo recoge como hecho

### Scenario: Fallo controlado cuando la consulta no puede abrirse

**Ref:** E1

Given el sistema no está disponible o no puede abrir una consulta  
When la persona inicia una conversación  
Then el sistema informa de un fallo recuperable en español, sin detalles internos  
And no deja ninguna consulta abierta  
And la persona puede volver a intentarlo

### Scenario: Cerrar la consulta cuando la conversación se interrumpe

**Ref:** E2 / UC-013-R2

Given una consulta en curso con hechos recogidos  
When la conversación se interrumpe sin que la persona dé por terminada la consulta  
Then el sistema cierra la consulta con lo recogido hasta ese momento  
And la consulta no queda abierta esperando a la persona  
And la consulta no se retoma después

### Scenario: Consulta que permanece abierta porque su cierre no pudo completarse

**Ref:** E3

Given una consulta en curso con hechos recogidos  
When el cierre no puede completarse  
Then la consulta no se da por cerrada  
And conserva lo recogido  
And el cierre puede reintentarse  
And es la única situación en la que una consulta permanece abierta fuera de su conversación

## Coverage notes

- **Happy path:** cubierto por `Sostener la consulta y cerrarla con lo que la persona cuenta`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6`, `A7` y `A8` tienen un escenario cada uno.
- **Exceptions:** `E1`, `E2` y `E3` tienen un escenario cada uno.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan cubiertos dentro de
  los escenarios existentes: consulta sin hechos que registrar (`A1` de [`UC-013b`](./UC-013b.md)),
  hecho mencionado de pasada (`A1`) y hecho mencionado más de una vez (`A5` con `A7` de
  [`UC-013b`](./UC-013b.md)).
- **Reglas sin escenario propio:** RN-001, RN-002, RN-003, RN-012, RN-014, RN-016, RN-021, RN-025,
  RN-026, RN-027, RN-028, RN-029 y RN-030 son reglas transversales que se verifican en los escenarios
  de sus casos de uso de origen o en [`UC-013b`](./UC-013b.md); no se les asigna escenario aquí para no
  duplicar cobertura.
- **Reglas transversales con matiz:** `RN-024` queda matizada por `UC-013b-R8` (la disponibilidad del
  hecho se produce al cerrar la consulta) y su escenario vive en [`UC-013b`](./UC-013b.md).
