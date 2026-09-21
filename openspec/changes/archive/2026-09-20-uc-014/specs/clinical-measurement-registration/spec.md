## Purpose

Incorporar al seguimiento de la persona las mediciones que menciona al contarla en la consulta, con su
métrica, su valor en la unidad de referencia, su fecha exacta y la consulta de la que procede, sin
suposiciones y sin registros a medias.

## ADDED Requirements

### Requirement: Registrar al cerrar la consulta las mediciones mencionadas

El sistema MUST reunir las mediciones que la persona mencionó durante la consulta y MUST incorporarlas a
su seguimiento al cerrarse esta, no antes. Mientras la consulta siga en curso, la persona MUST poder
corregir o ampliar lo contado sin que nada quede registrado. Cada medición MUST registrarse con su
métrica, su valor en la unidad de referencia de esa métrica y su fecha exacta, y MUST quedar disponible
de inmediato para consulta y para las vistas de seguimiento. Cuando la consulta contenga varias
mediciones, de métricas distintas o de días distintos, el sistema MUST registrar una medición por cada
una. Cuando la consulta no contenga ninguna medición, el sistema MUST NOT registrar nada y el seguimiento
MUST permanecer exactamente como estaba.

#### Scenario: Registrar una medición mencionada durante la consulta

- **WHEN** la consulta se cierra habiendo recogido una medición con su métrica, su valor y su fecha
- **THEN** el sistema comprueba que la métrica está admitida y que el valor es válido para ella
- **AND** incorpora la medición al seguimiento con su valor, su unidad de referencia y su fecha exacta
- **AND** la medición queda disponible de inmediato para consulta y para las vistas de seguimiento

#### Scenario: Registrar varias mediciones de la misma consulta

- **WHEN** la consulta se cierra habiendo mencionado la persona mediciones de métricas distintas o de días distintos
- **THEN** el sistema registra una medición por cada una
- **AND** cada medición declara su propia procedencia
- **AND** cada métrica conserva una sola medición por día

#### Scenario: Cerrar una consulta sin ninguna medición

- **WHEN** la consulta se cierra sin que la persona haya mencionado ninguna medición
- **THEN** el sistema no registra ninguna medición
- **AND** el seguimiento permanece exactamente como estaba

#### Scenario: No registrar una medición antes de cerrar la consulta

- **WHEN** la persona menciona una medición mientras la consulta sigue en curso
- **THEN** el sistema no la incorpora todavía al seguimiento
- **AND** la persona puede corregirla o ampliarla sin que nada quede registrado

### Requirement: Exigir una fecha exacta a toda medición

Toda medición registrada MUST tener una fecha exacta: un día concreto del calendario, sin hora. Una fecha
aproximada o desconocida MUST NOT convertirse en exacta: el sistema MUST pedir la fecha exacta y, si no la
obtiene, MUST NOT registrar la medición y MUST NOT sustituirla por una fecha supuesta. Cuando la persona
no indique ninguna fecha, el sistema MUST registrarla con el día en que se atiende el mensaje, o MUST pedir
confirmación antes de guardar cuando no pueda darlo por seguro.

#### Scenario: No registrar una medición con fecha aproximada o desconocida

- **WHEN** la consulta se cierra habiendo mencionado la persona una medición con una fecha aproximada o desconocida
- **THEN** el sistema no convierte esa fecha en exacta
- **AND** pide la fecha exacta
- **AND** si no la obtiene, no registra la medición

#### Scenario: Registrar una medición sin que la persona indique la fecha

- **WHEN** la consulta se cierra habiendo mencionado la persona una medición sin indicar ninguna fecha
- **THEN** el sistema la registra con el día en que se atiende el mensaje
- **AND** cuando no pueda darlo por seguro, pide confirmación antes de guardar
- **AND** no la registra con una fecha que la persona no haya dado

#### Scenario: Registrar la fecha como un día del calendario

- **WHEN** una medición se incorpora al seguimiento con su fecha exacta
- **THEN** el sistema la guarda como un día sin hora
- **AND** la fecha se conserva igual con independencia de dónde esté la persona

### Requirement: Mantener una sola medición por métrica y día

El sistema MUST admitir una sola medición por métrica y día. Cuando una métrica y un día ya tengan una
medición registrada y la consulta vuelva a aportar esa métrica y ese día, el sistema MUST reemplazar el
valor de ese día e informar de que el registro quedó actualizado, y MUST NOT crear una segunda medición.

#### Scenario: Actualizar la medición de una métrica y un día ya registrados

- **WHEN** esa métrica y ese día ya tenían una medición registrada
- **AND** la consulta se cierra habiendo mencionado la persona una medición nueva de esa métrica y ese día
- **THEN** el sistema reemplaza el valor de ese día
- **AND** informa de que el registro quedó actualizado
- **AND** no crea una segunda medición para esa métrica y ese día

#### Scenario: No duplicar la métrica y el día ya registrados

- **WHEN** el reemplazo de una métrica y un día ya registrados termina
- **THEN** existe una sola medición para esa métrica y ese día
- **AND** conserva el valor actualizado

### Requirement: Registrar una sola vez una medición mencionada varias veces

Cuando la persona mencione la misma medición varias veces a lo largo de la consulta, el sistema MUST
registrarla una sola vez y MUST conservar la información más completa que haya recogido.

#### Scenario: Registrar una sola vez la medición repetida

- **WHEN** la consulta se cierra habiendo mencionado la persona la misma medición varias veces
- **THEN** el sistema registra la medición una sola vez
- **AND** conserva la información más completa que haya recogido

### Requirement: Declarar la procedencia de toda medición registrada

Toda medición registrada a partir de una consulta MUST declarar de dónde procede, identificando la
consulta de la que viene, y esa procedencia MUST fijarse en el momento del registro y MUST NOT
modificarse después. La procedencia MUST NOT exponerse en la conversación ni en la confirmación.

#### Scenario: Declarar la consulta de origen

- **WHEN** una medición se registra al cerrar la consulta
- **THEN** la medición declara la consulta de la que procede
- **AND** la consulta declarada es la que estaba en curso

#### Scenario: Conservar la procedencia

- **WHEN** una medición ya registrada se vuelve a mencionar
- **THEN** la medición registrada no se reescribe
- **AND** su procedencia permanece como estaba

#### Scenario: No exponer la procedencia en la confirmación

- **WHEN** el sistema confirma a la persona lo que quedó registrado
- **THEN** la confirmación no describe la procedencia ni los identificadores de la consulta
- **AND** no expone detalles internos del sistema

### Requirement: Comprobar antes de escribir y no dejar registros a medias

Ninguna medición MUST incorporarse al seguimiento sin una comprobación previa de que su métrica está
admitida, su valor es válido para esa métrica, su unidad es inequívoca y su fecha es exacta. Una medición
que no supere esa comprobación MUST NOT incorporarse y MUST NOT dejar ningún registro a medias de ella,
mientras las demás mediciones admisibles de la misma consulta MUST registrarse. La escritura del registro
MUST ser de todo o nada: si el registro no puede completarse, el seguimiento MUST permanecer exactamente
como estaba. Cuando el registro falle después de que otra medición ya se haya registrado, el sistema MUST
informar de lo que sí quedó registrado y de lo que no, MUST NOT presentar como registrado lo que no lo
está y MUST NOT comunicar el fallo como una ausencia de mediciones. La persona MUST poder volver a
intentarlo.

#### Scenario: No incorporar una medición que no supera la comprobación previa

- **WHEN** la consulta se cierra con una medición cuyo valor no es válido para su métrica, cuya unidad no puede deducirse o a la que le falta la fecha exacta
- **THEN** esa medición no se incorpora al seguimiento
- **AND** no queda ningún registro a medias de ella
- **AND** las demás mediciones admisibles sí se registran

#### Scenario: Seguimiento intacto cuando el registro no puede completarse

- **WHEN** el registro de las mediciones de una consulta no puede completarse
- **THEN** el seguimiento permanece exactamente como estaba
- **AND** nada queda registrado a medias
- **AND** la persona puede volver a intentarlo

#### Scenario: Informar de una medición que falla tras otra ya registrada

- **WHEN** el registro de una medición falla después de que otra ya se haya registrado
- **THEN** el sistema informa de lo que sí quedó registrado y de lo que no
- **AND** no presenta como registrado lo que no lo está
- **AND** no comunica el fallo como una ausencia de mediciones

### Requirement: Atender por el cauce de las mediciones un mensaje que contiene una

Un mensaje que contiene una medición MUST atenderse por el cauce de las mediciones y MUST NOT tratarse
como un hecho clínico. Una medición MUST NOT convertirse en un evento de la historia clínica ni
incorporarse a ella.

#### Scenario: Atender por el cauce de las mediciones

- **WHEN** la persona menciona una medición en la conversación
- **THEN** el sistema la atiende por el cauce de las mediciones
- **AND** no la trata como un hecho clínico

#### Scenario: No incorporar la medición a la historia clínica

- **WHEN** una medición se registra al cerrar la consulta
- **THEN** queda en el seguimiento de mediciones de la persona
- **AND** no se convierte en un evento de la historia clínica

### Requirement: Confirmar brevemente lo registrado

Tras cerrar la consulta, el sistema MUST comunicar en español una confirmación breve de lo registrado, que
resuma las mediciones incorporadas y, cuando las haya, los hechos clínicos, sin detalles internos. La
confirmación MUST distinguir lo que quedó registrado de lo que no, MUST NOT presentar como registrado lo
que no lo está y MUST NOT confundir un fallo de registro con una ausencia de mediciones.

#### Scenario: Confirmar las mediciones registradas

- **WHEN** el cierre de la consulta incorpora una o más mediciones al seguimiento
- **THEN** la persona recibe una confirmación breve en español que resume lo registrado
- **AND** la confirmación distingue lo que quedó registrado de lo que no

#### Scenario: Distinguir un fallo de una ausencia de mediciones

- **WHEN** una medición no puede registrarse al cerrar la consulta
- **THEN** el sistema declara de forma explícita y comprensible que esa medición no quedó registrada
- **AND** no lo comunica como una ausencia de mediciones
