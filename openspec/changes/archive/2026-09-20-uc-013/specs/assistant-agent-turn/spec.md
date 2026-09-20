## MODIFIED Requirements

### Requirement: Decidir y ejecutar las operaciones del mensaje

El sistema MUST determinar qué operaciones necesita un mensaje y ejecutarlas dentro del mismo
intercambio, y MUST poder servirse del resultado de una operación para decidir la siguiente. El sistema
MUST NOT exigir a la persona que declare qué operación debe realizarse y MUST NOT pedirle que confirme
cada operación antes de ejecutarla. Las operaciones de un turno MUST limitarse a consultar la historia
clínica y a recoger en notas los hechos clínicos que la persona menciona, dentro de la consulta en curso.

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

#### Scenario: No exigir a la persona que declare la operación

- **WHEN** la persona cuenta o pregunta algo con sus propias palabras, sin decir qué quiere que el sistema haga
- **THEN** el sistema determina por sí mismo las operaciones necesarias
- **AND** no pide confirmación de cada operación por separado

### Requirement: Acotar las operaciones disponibles

Las operaciones a disposición del asistente durante una consulta MUST limitarse a consultar la historia
clínica y a recoger en notas los hechos clínicos que la persona menciona. Registrar hechos MUST NOT ser
una operación del turno: la incorporación a la historia ocurre al cerrar la consulta. Una operación fuera
de ese conjunto MUST NOT ejecutarse y MUST NOT presentarse como resultado. Cuando el mensaje pida algo que
ninguna operación disponible puede producir, el sistema MUST declararlo en español y MUST NOT responderlo
con una suposición.

#### Scenario: No ejecutar una operación fuera del conjunto disponible

- **WHEN** el mensaje pide borrar o modificar hechos ya registrados, o consultar la historia de otra persona
- **THEN** el sistema no ejecuta la operación
- **AND** explica en español que no puede hacerlo
- **AND** la historia clínica permanece exactamente como estaba

#### Scenario: Declarar lo que ninguna operación puede producir

- **WHEN** el mensaje pide algo que ni la consulta a la historia ni la recogida de notas pueden producir
- **THEN** el sistema declara que no puede atender esa parte
- **AND** no la responde con una suposición

#### Scenario: No registrar dentro del turno

- **WHEN** el mensaje contiene un hecho clínico registrable
- **THEN** el asistente lo deja recogido en una nota de la consulta
- **AND** no solicita el registro del hecho dentro del turno
- **AND** la historia clínica no cambia durante el turno

### Requirement: Validar y escribir dentro del camino de la operación

Con independencia de cómo se haya decidido una operación, el sistema MUST validar de forma determinista
antes de incorporar cualquier hecho a la historia, y la escritura MUST ocurrir dentro del camino de la
operación que la produce. Mientras la consulta está en curso, ninguna operación del turno escribe: la
escritura de los hechos recogidos ocurre en el camino del cierre de la consulta. Una operación que no
pasa la validación MUST NOT producir escritura alguna. Una operación ya completada MUST NOT revertirse
porque otra operación posterior falle.

#### Scenario: No incorporar un hecho que no pasa la validación

- **WHEN** un hecho recogido no pasa la validación determinista al cerrar la consulta
- **THEN** el sistema no incorpora ese hecho a la historia
- **AND** explica en español por qué el hecho no puede registrarse
- **AND** no deja ningún registro a medias

#### Scenario: Conservar lo completado ante un fallo posterior

- **WHEN** el cierre falla al registrar un hecho después de haber registrado otro
- **THEN** el hecho ya registrado permanece en la historia
- **AND** la respuesta informa de lo que quedó registrado y de lo que no pudo completarse
- **AND** el sistema no presenta el cierre como completo

#### Scenario: No delegar la escritura en quien decide la operación

- **WHEN** el asistente deja recogido un hecho en una nota de la consulta
- **THEN** la comprobación y la escritura las realiza la aplicación al cerrar la consulta
- **AND** el asistente no escribe directamente en la historia clínica
- **AND** el asistente no puede escribir en la historia dentro del turno

#### Scenario: No escribir mientras la consulta está en curso

- **WHEN** un turno recoge uno o más hechos clínicos
- **THEN** ninguna escritura se produce en la historia clínica durante ese turno
- **AND** los hechos quedan disponibles en el cierre de la consulta
