# UC-013b — Registrar con procedencia los hechos de la consulta — Acceptance Criteria

## Feature

**ID:** UC-013b  
**Use case:** Registrar con procedencia los hechos de la consulta  
**Source:** [UC-013b.md](./UC-013b.md)

Al cerrar una consulta, los hechos que la persona mencionó durante ella se incorporan a su historia
clínica con la información disponible, declarando de qué consulta proceden, y la consulta conserva un
resumen de lo tratado marcado como información derivada.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`, usando los
  identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md) cuando corresponda.

## Scenarios

### Scenario: Registrar al cerrar los hechos de la consulta con su procedencia

**Ref:** Flujo principal / UC-013b-R1, UC-013b-R5, UC-013b-R8

Given existe una consulta en curso, abierta al iniciar la conversación  
And la conversación ha recogido hechos clínicos con la información disponible y la precisión temporal que la persona aportó  
When la consulta se cierra  
Then el sistema comprueba, hecho por hecho, que alcanza la información mínima requerida  
And incorpora cada hecho admisible a la historia clínica declarando su procedencia: quién lo aportó, cuándo se registró y de qué consulta procede  
And guarda el resumen de la consulta a partir de sus hechos registrados y lo marca como información derivada  
And el resumen incluye un motivo a modo de título, generado automáticamente a partir de lo conversado  
And la persona no ve ese motivo  
And los hechos quedan disponibles para consulta al terminar la consulta

### Scenario: Cerrar una consulta sin hechos clínicos

**Ref:** A1

Given una consulta en curso en la que la persona solo conversó o consultó su historia  
When la consulta se cierra  
Then el sistema no registra ningún hecho  
And la consulta se cierra con su resumen  
And la historia clínica permanece exactamente como estaba

### Scenario: No registrar un hecho que no alcanza la información mínima

**Ref:** A2 / UC-013b-R3

Given una consulta en curso con un hecho recogido que no alcanza la información mínima requerida  
When la consulta se cierra  
Then el sistema no registra ese hecho  
And no completa la información que falta con suposiciones  
And si la persona está presente al cerrar, se lo indica para que pueda contarlo en una consulta nueva

### Scenario: Registrar varios hechos admisibles de la misma consulta

**Ref:** A3 / UC-013b-R1

Given una consulta en curso con varios hechos admisibles recogidos  
When la consulta se cierra  
Then el sistema registra un hecho por cada uno  
And cada hecho declara su propia procedencia  
And la consulta conserva un solo resumen

### Scenario: No duplicar un hecho ya registrado

**Ref:** A4 / RN-022

Given un hecho de la consulta ya estaba registrado con anterioridad  
When la consulta se cierra  
Then el sistema no crea un duplicado  
And no altera la procedencia que el hecho ya tenía

### Scenario: Registrar y cerrar cuando la consulta se cierra sin que la persona lo pida

**Ref:** A5

Given una consulta en curso con hechos admisibles recogidos  
When la consulta se cierra sin que la persona lo pida, por interrupción de la conversación o porque inicia otra consulta  
Then el sistema registra igualmente lo admisible  
And guarda el resumen de la consulta  
And la consulta queda cerrada

### Scenario: Registrar un hecho con fecha aproximada o desconocida

**Ref:** A6 / RN-006

Given una consulta en curso con un hecho cuya fecha es aproximada o desconocida  
When la consulta se cierra  
Then el sistema lo registra conservando exactamente esa precisión  
And no lo convierte en una fecha exacta  
And no lo sustituye por una suposición

### Scenario: Registrar una sola vez un hecho mencionado varias veces

**Ref:** A7

Given una consulta en curso en la que la persona mencionó el mismo hecho varias veces  
When la consulta se cierra  
Then el sistema registra el hecho una sola vez  
And conserva la información más completa que haya recogido

### Scenario: No incorporar un hecho que no supera la comprobación previa

**Ref:** E1 / RN-018, RN-020

Given una consulta en curso con un hecho que no supera la comprobación previa al registro  
When la consulta se cierra  
Then ese hecho no se incorpora a la historia clínica  
And no queda ningún registro a medias de ese hecho  
And los demás hechos admisibles sí se registran

### Scenario: Informar de un hecho que falla tras otro ya registrado

**Ref:** E2 / RN-012

Given una consulta en curso con más de un hecho admisible  
When el registro de un hecho falla después de que otro ya se haya registrado  
Then el sistema informa de lo que sí quedó registrado y de lo que no  
And no presenta como registrado lo que no lo está  
And no comunica el fallo como una ausencia de registros

### Scenario: Cerrar la consulta cuando el resumen no puede guardarse

**Ref:** E3 / RN-032

Given una consulta en curso con hechos admisibles recogidos  
When el resumen de la consulta no puede guardarse  
Then la consulta queda cerrada con sus hechos  
And queda cerrada sin resumen  
And el resumen puede reconstruirse después a partir de los hechos de la consulta  
And no se pierde ningún hecho

### Scenario: Consulta que no se da por cerrada porque el cierre no pudo completarse

**Ref:** E4 / UC-013b-R6

Given una consulta en curso con hechos recogidos  
When el cierre no puede completarse  
Then la consulta no se da por cerrada  
And conserva lo recogido  
And la persona puede volver a intentar el cierre  
And nada queda registrado a medias

## Coverage notes

- **Happy path:** cubierto por `Registrar al cerrar los hechos de la consulta con su procedencia`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6` y `A7` tienen un escenario cada uno.
- **Exceptions:** `E1`, `E2`, `E3` y `E4` tienen un escenario cada uno.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan cubiertos dentro de
  los escenarios existentes: consulta sin hechos que registrar (`A1`), hecho sin información mínima
  (`A2`) y hecho con precisión temporal imprecisa (`A6`).
- **Reglas sin escenario propio:** RN-001, RN-002, RN-003, RN-005, RN-007, RN-008, RN-021, RN-024,
  RN-025, RN-030 y RN-031 son reglas transversales que se verifican en los escenarios de sus casos de
  uso de origen; `RN-031` se ejercita a través de `UC-013b-R1`, ya cubierta por el escenario principal
  y por `A3`.
