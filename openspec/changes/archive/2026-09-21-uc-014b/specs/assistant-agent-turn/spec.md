## MODIFIED Requirements

### Requirement: Decidir y ejecutar las operaciones del mensaje

El sistema MUST determinar qué operaciones necesita un mensaje y ejecutarlas dentro del mismo
intercambio, y MUST poder servirse del resultado de una operación para decidir la siguiente. El sistema
MUST NOT exigir a la persona que declare qué operación debe realizarse y MUST NOT pedirle que confirme
cada operación antes de ejecutarla. Las operaciones de un turno MUST limitarse a consultar la historia
clínica, a consultar el seguimiento de mediciones y a recoger en notas los hechos clínicos y las
mediciones que la persona menciona, dentro de la consulta en curso.

#### Scenario: Atender un mensaje que necesita más de una operación

- **WHEN** la persona envía un mensaje que requiere consultar la historia y recoger un hecho clínico
- **THEN** el sistema ejecuta las dos operaciones dentro del mismo intercambio
- **AND** devuelve una sola respuesta que cubre lo que el mensaje requería
- **AND** la persona no tiene que repetir su mensaje ni declarar qué quería

#### Scenario: Encadenar una consulta y un registro

- **WHEN** para completar un hecho recogido el sistema necesita antes el resultado de una consulta a la historia
- **THEN** el sistema consulta la historia, usa lo encontrado para completar el hecho y lo deja recogido para el cierre
- **AND** la persona recibe una sola respuesta
- **AND** el hecho se registra al cerrar la consulta, no dentro del turno

#### Scenario: Recoger una medición mencionada en el turno

- **WHEN** la persona menciona una medición en un mensaje
- **THEN** el sistema la deja recogida en una nota de la consulta
- **AND** no la incorpora al seguimiento dentro del turno
- **AND** la persona recibe una sola respuesta

#### Scenario: Atender un mensaje que consulta el seguimiento de mediciones

- **WHEN** la persona pregunta por sus mediciones en un mensaje
- **THEN** el sistema ejecuta la operación de consultar el seguimiento de mediciones dentro del mismo intercambio
- **AND** devuelve una sola respuesta con los valores, las unidades y las fechas recuperados
- **AND** el seguimiento no cambia durante el turno

#### Scenario: No exigir a la persona que declare la operación

- **WHEN** la persona cuenta o pregunta algo con sus propias palabras, sin decir qué quiere que el sistema haga
- **THEN** el sistema determina por sí mismo las operaciones necesarias
- **AND** no pide confirmación de cada operación por separado

### Requirement: Acotar las operaciones disponibles

Las operaciones a disposición del asistente durante una consulta MUST limitarse a consultar la historia
clínica, a consultar el seguimiento de mediciones y a recoger en notas los hechos clínicos y las
mediciones que la persona menciona. Registrar hechos y registrar mediciones MUST NOT ser operaciones del
turno: la incorporación a la historia y al seguimiento ocurre al cerrar la consulta. La consulta del
seguimiento de mediciones MUST ser de solo lectura. Una operación fuera de ese conjunto MUST NOT
ejecutarse y MUST NOT presentarse como resultado. Cuando el mensaje pida algo que ninguna operación
disponible puede producir, el sistema MUST declararlo en español y MUST NOT responderlo con una
suposición.

#### Scenario: No ejecutar una operación fuera del conjunto disponible

- **WHEN** el mensaje pide borrar o modificar hechos ya registrados, o consultar la historia de otra persona
- **THEN** el sistema no ejecuta la operación
- **AND** explica en español que no puede hacerlo
- **AND** la historia clínica permanece exactamente como estaba

#### Scenario: Declarar lo que ninguna operación puede producir

- **WHEN** el mensaje pide algo que ni la consulta a la historia, ni la consulta del seguimiento de mediciones, ni la recogida de notas pueden producir
- **THEN** el sistema declara que no puede atender esa parte
- **AND** no la responde con una suposición

#### Scenario: No registrar dentro del turno

- **WHEN** el mensaje contiene un hecho clínico registrable
- **THEN** el asistente lo deja recogido en una nota de la consulta
- **AND** no solicita el registro del hecho dentro del turno
- **AND** la historia clínica no cambia durante el turno

#### Scenario: No incorporar una medición durante el turno

- **WHEN** el mensaje contiene una medición
- **THEN** el asistente la deja recogida en una nota de la consulta
- **AND** no la incorpora al seguimiento durante el turno
- **AND** el seguimiento de mediciones no cambia hasta el cierre de la consulta

#### Scenario: No escribir en el seguimiento al consultar mediciones

- **WHEN** el turno consulta el seguimiento de mediciones
- **THEN** el sistema no escribe en el seguimiento durante el turno
- **AND** el seguimiento permanece exactamente como estaba
