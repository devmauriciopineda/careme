# assistant-agent-turn Specification

## Purpose

Permitir que el asistente decida por sí mismo qué operaciones acotadas necesita un mensaje y las ejecute
dentro del mismo intercambio, entregando una sola respuesta que cubre todo lo que la persona pidió.

## Requirements

### Requirement: Decidir y ejecutar las operaciones del mensaje

El sistema MUST determinar qué operaciones necesita un mensaje y ejecutarlas dentro del mismo
intercambio, y MUST poder servirse del resultado de una operación para decidir la siguiente. El sistema
MUST NOT exigir a la persona que declare qué operación debe realizarse y MUST NOT pedirle que confirme
cada operación antes de ejecutarla. Las operaciones de un turno MUST limitarse a consultar la historia
clínica y a recoger en notas los hechos clínicos y las mediciones que la persona menciona, dentro de la
consulta en curso.

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

#### Scenario: No exigir a la persona que declare la operación

- **WHEN** la persona cuenta o pregunta algo con sus propias palabras, sin decir qué quiere que el sistema haga
- **THEN** el sistema determina por sí mismo las operaciones necesarias
- **AND** no pide confirmación de cada operación por separado

### Requirement: Acotar las operaciones disponibles

Las operaciones a disposición del asistente durante una consulta MUST limitarse a consultar la historia
clínica y a recoger en notas los hechos clínicos y las mediciones que la persona menciona. Registrar
hechos y registrar mediciones MUST NOT ser operaciones del turno: la incorporación a la historia y al
seguimiento ocurre al cerrar la consulta. Una operación fuera de ese conjunto MUST NOT ejecutarse y MUST
NOT presentarse como resultado. Cuando el mensaje pida algo que ninguna operación disponible puede
producir, el sistema MUST declararlo en español y MUST NOT responderlo con una suposición.

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

#### Scenario: No incorporar una medición durante el turno

- **WHEN** el mensaje contiene una medición
- **THEN** el asistente la deja recogida en una nota de la consulta
- **AND** no la incorpora al seguimiento durante el turno
- **AND** el seguimiento de mediciones no cambia hasta el cierre de la consulta

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

### Requirement: Entregar una sola respuesta para todo el mensaje

El sistema MUST devolver una sola respuesta visible para la persona que cubra todo lo que el mensaje
requería, MUST NOT exponer las operaciones ejecutadas ni cómo se decidieron y MUST NOT dividir la
atención en una respuesta por operación. El sistema MUST NOT presentar como completo lo que no lo está.

#### Scenario: Cubrir registro y respuesta en un solo mensaje del asistente

- **WHEN** el turno registra un hecho y responde una pregunta
- **THEN** la persona recibe la confirmación del hecho registrado y la respuesta en una sola respuesta
- **AND** no recibe dos mensajes separados, uno por operación

#### Scenario: No exponer las operaciones ejecutadas

- **WHEN** el sistema atiende un mensaje que requirió varias operaciones
- **THEN** la respuesta no enumera las operaciones ni describe cómo se decidieron
- **AND** no expone detalles internos del sistema

#### Scenario: No presentar como completo un turno incompleto

- **WHEN** una operación del turno no pudo completarse
- **THEN** la respuesta declara la parte que no pudo atenderse
- **AND** no la presenta como atendida

### Requirement: Pedir la información que falta sin ejecutar la operación

Cuando el mensaje no aporte lo necesario para completar una operación, el sistema MUST pedir en español la
información que falta, MUST NOT ejecutar esa operación y MUST NOT registrar nada por ella. El sistema MUST
retomar la operación cuando la persona aporte la información.

#### Scenario: Pedir la información que falta

- **WHEN** el mensaje podría registrarse pero no aporta información suficiente
- **THEN** el sistema pide en español la información que falta
- **AND** no registra ningún hecho
- **AND** la historia clínica permanece sin cambios

#### Scenario: Retomar la operación con la información aportada

- **WHEN** la persona responde con la información que se le pidió
- **THEN** el sistema retoma la operación pendiente
- **AND** no pide de nuevo información que la persona ya dio

### Requirement: Acotar la actuación al mensaje que la origina

La actuación del asistente MUST limitarse al intercambio del mensaje que la origina. El sistema MUST NOT
iniciar operaciones por su cuenta, MUST NOT mantener la actuación después de entregar la respuesta y MUST
NOT encadenar etapas más allá de ese mensaje ni ejecutar operaciones en segundo plano o de forma
programada.

#### Scenario: No actuar sin un mensaje que lo origine

- **WHEN** el sistema ha entregado la respuesta de un turno
- **THEN** no inicia ninguna operación hasta que llegue un mensaje nuevo
- **AND** no ejecuta operaciones en segundo plano ni de forma programada

#### Scenario: Agotar la actuación en el turno

- **WHEN** el mensaje requería varias operaciones y todas se completaron
- **THEN** el sistema entrega la respuesta y cierra el turno
- **AND** no continúa actuando por su cuenta

### Requirement: Informar una indisponibilidad del asistente sin efecto alguno

Cuando el asistente no esté disponible o no esté configurado, el sistema MUST devolver el resultado
`failed` con un mensaje recuperable en español, MUST NOT ejecutar ninguna operación, MUST NOT registrar
nada y MUST NOT entregar una respuesta compuesta por otro medio. La persona MUST conservar su mensaje para
volver a intentarlo.

#### Scenario: Asistente no disponible

- **WHEN** la persona envía un mensaje y el asistente no está disponible o no está configurado
- **THEN** el sistema devuelve un fallo recuperable en español, sin detalles internos
- **AND** no ejecuta ninguna operación
- **AND** no registra nada
- **AND** no entrega una respuesta inventada ni compuesta por otro medio
- **AND** la persona conserva su mensaje para volver a intentarlo
