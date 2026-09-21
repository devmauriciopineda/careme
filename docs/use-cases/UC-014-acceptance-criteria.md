# UC-014 — Registrar una medición por lenguaje natural — Acceptance Criteria

## Feature

**ID:** UC-014  
**Use case:** Registrar una medición por lenguaje natural  
**Source:** [UC-014.md](./UC-014.md)

Al cerrar la consulta, las mediciones que la persona mencionó se incorporan a su seguimiento con su
métrica, su valor, su unidad y una fecha exacta, declarando de qué consulta proceden.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`, usando los
  identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md) cuando corresponda.

## Scenarios

### Scenario: Registrar al cerrar una medición mencionada durante la consulta

**Ref:** Flujo principal / UC-014-R1, UC-014-R6, UC-014-R9

Given existe una consulta en curso  
And la conversación ha recogido una medición con su métrica, su valor y una fecha exacta  
When la consulta se cierra  
Then el sistema comprueba que la métrica está admitida y que el valor es válido para ella  
And incorpora la medición a su seguimiento con su valor, su unidad y su fecha exacta  
And declara la consulta de la que procede  
And la medición queda disponible de inmediato para consulta y para las vistas de seguimiento  
And la persona recibe una confirmación breve de lo registrado

### Scenario: Registrar varias mediciones de la misma consulta

**Ref:** A1 / UC-014-R8

Given una consulta en curso en la que la persona mencionó mediciones de métricas distintas o de días distintos  
When la consulta se cierra  
Then el sistema registra una medición por cada una  
And cada medición declara su propia procedencia  
And cada métrica conserva una sola medición por día

### Scenario: Registrar una medición sin que la persona indique la fecha

**Ref:** A2 / UC-014-R7

Given una consulta en curso en la que la persona mencionó una medición sin indicar ninguna fecha  
When la consulta se cierra  
Then el sistema la registra con el día en que se atiende el mensaje  
And cuando no pueda darlo por seguro, pide confirmación antes de guardar  
And no la registra con una fecha que la persona no haya dado

### Scenario: No registrar una medición con fecha aproximada o desconocida

**Ref:** A3 / UC-014-R7

Given una consulta en curso en la que la persona mencionó una medición con una fecha aproximada o desconocida  
When la consulta se cierra  
Then el sistema no convierte esa fecha en exacta  
And pide la fecha exacta  
And si no la obtiene, no registra la medición

### Scenario: Actualizar la medición de una métrica y un día ya registrados

**Ref:** A4 / UC-014-R8, RN-019

Given esa métrica y ese día ya tenían una medición registrada  
And la persona mencionó durante la consulta una medición nueva de esa métrica y ese día  
When la consulta se cierra  
Then el sistema reemplaza el valor de ese día  
And informa de que el registro quedó actualizado  
And no crea una segunda medición para esa métrica y ese día

### Scenario: Registrar una sola vez una medición mencionada varias veces

**Ref:** A5

Given una consulta en curso en la que la persona mencionó la misma medición varias veces  
When la consulta se cierra  
Then el sistema registra la medición una sola vez  
And conserva la información más completa que haya recogido

### Scenario: Registrar una medición compuesta como la presión arterial

**Ref:** A6 / UC-014-R6

Given una consulta en curso en la que la persona mencionó su presión arterial con sus dos valores  
When la consulta se cierra  
Then el sistema registra esa presión como una sola medición  
And guarda sus dos valores, sistólica y diastólica  
And no la registra como dos mediciones distintas

### Scenario: Cerrar una consulta sin ninguna medición

**Ref:** A7

Given una consulta en curso en la que la persona solo conversó, consultó su historia o contó hechos clínicos  
When la consulta se cierra  
Then el sistema no registra ninguna medición  
And el seguimiento permanece exactamente como estaba

### Scenario: Ampliar el catálogo cuando la persona menciona una métrica nueva

**Ref:** A8 / UC-014-R5

Given una consulta en curso en la que la persona mencionó una métrica que el sistema todavía no admite  
And la persona está presente al cerrarse la consulta  
When la consulta se cierra  
Then el sistema le informa de que esa medición no está quedando registrada  
And le pregunta si quiere empezar a registrar esa métrica  
And solo si la persona lo confirma, la métrica pasa a admitirse y la medición se registra con ella  
And si no lo confirma, esa medición no se incorpora al seguimiento

### Scenario: Registrar un valor expresado en otra unidad de la misma métrica

**Ref:** A9 / UC-014-R3

Given una consulta en curso en la que la persona mencionó una medición con un valor expresado en otra unidad de esa misma métrica  
When la consulta se cierra  
Then el sistema convierte el valor a la unidad de la métrica  
And guarda la medición con la unidad de la métrica  
And el valor no se altera más allá del cambio de unidad

### Scenario: Pedir que se concrete la unidad de una medición

**Ref:** A10 / UC-014-R4

Given una consulta en curso en la que la persona mencionó una medición sin indicar la unidad  
And del valor y de la métrica no puede deducirse la unidad con seguridad  
When la consulta se cierra  
Then el sistema no supone la unidad  
And pide que la concrete  
And si no la obtiene, no registra la medición

### Scenario: Registrar sin pedir aclaración cuando la unidad es inequívoca

**Ref:** A10 / UC-014-R4

Given una consulta en curso en la que la persona mencionó una medición sin indicar la unidad  
And del valor y de la métrica se deduce una única unidad posible  
When la consulta se cierra  
Then el sistema registra la medición con esa unidad  
And no pide una aclaración innecesaria

### Scenario: No incorporar una medición que no supera la comprobación previa

**Ref:** E1 / RN-018, RN-020

Given una consulta en curso con una medición cuyo valor no es válido para su métrica, cuya unidad no puede deducirse o a la que le falta la fecha exacta  
When la consulta se cierra  
Then esa medición no se incorpora al seguimiento  
And no queda ningún registro a medias de ella  
And las demás mediciones admisibles sí se registran

### Scenario: Informar de una medición que falla tras otra ya registrada

**Ref:** E2 / RN-012

Given una consulta en curso con más de una medición admisible  
When el registro de una medición falla después de que otra ya se haya registrado  
Then el sistema informa de lo que sí quedó registrado y de lo que no  
And no presenta como registrado lo que no lo está  
And no comunica el fallo como una ausencia de mediciones

### Scenario: Seguimiento intacto cuando el registro no puede completarse

**Ref:** E3 / RN-020

Given una consulta en curso con mediciones admisibles recogidas  
When el registro no puede completarse  
Then el seguimiento permanece exactamente como estaba  
And nada queda registrado a medias  
And la persona puede volver a intentarlo

## Coverage notes

- **Happy path:** cubierto por `Registrar al cerrar una medición mencionada durante la consulta`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6`, `A7`, `A8` y `A9` tienen un escenario cada
  uno; `A10` tiene dos, porque el resultado depende de si la unidad puede deducirse con seguridad.
- **Exceptions:** `E1`, `E2` y `E3` tienen un escenario cada uno.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan cubiertos dentro de
  los escenarios existentes: medición sin fecha (`A2`), fecha aproximada o desconocida (`A3`),
  reemplazo de una métrica y un día ya registrados (`A4`), medición compuesta (`A6`), consulta sin
  mediciones (`A7`), métrica todavía no admitida (`A8`), cambio de unidad (`A9`) y unidad que no puede
  deducirse (`A10`).
- **Reglas sin escenario propio:** RN-005, RN-006, RN-007, RN-009, RN-023, RN-024, RN-026, RN-029,
  RN-030 y RN-031 son reglas transversales que se verifican en los escenarios de sus casos de uso de
  origen; `RN-031` se ejercita a través de `UC-014-R9`, ya cubierta por el escenario principal y por
  `A1`.
- **Automatización:** este caso de uso todavía no está implementado, de modo que aún no existe una
  prueba que lo cubra. Los escenarios están redactados para poder convertirse en pruebas sin añadir
  decisiones de negocio.
