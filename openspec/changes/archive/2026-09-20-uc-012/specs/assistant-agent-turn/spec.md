## Purpose

Permitir que el asistente decida por sí mismo qué operaciones acotadas necesita un mensaje y las ejecute
dentro del mismo intercambio, entregando una sola respuesta que cubre todo lo que la persona pidió.

## ADDED Requirements

### Requirement: Decidir y ejecutar las operaciones del mensaje

El sistema MUST determinar qué operaciones necesita un mensaje y ejecutarlas dentro del mismo
intercambio, y MUST poder servirse del resultado de una operación para decidir la siguiente. El sistema
MUST NOT exigir a la persona que declare qué operación debe realizarse y MUST NOT pedirle que confirme
cada operación antes de ejecutarla.

#### Scenario: Atender un mensaje que necesita más de una operación

- **WHEN** la persona envía un mensaje que requiere consultar la historia y registrar un hecho
- **THEN** el sistema ejecuta las dos operaciones dentro del mismo intercambio
- **AND** devuelve una sola respuesta que cubre lo que el mensaje requería
- **AND** la persona no tiene que repetir su mensaje ni declarar qué quería

#### Scenario: Encadenar una consulta y un registro

- **WHEN** para completar un registro el sistema necesita antes el resultado de una consulta a la historia
- **THEN** el sistema consulta la historia, usa lo encontrado para decidir la operación siguiente y registra el hecho
- **AND** la persona recibe una sola respuesta

#### Scenario: No exigir a la persona que declare la operación

- **WHEN** la persona cuenta o pregunta algo con sus propias palabras, sin decir qué quiere que el sistema haga
- **THEN** el sistema determina por sí mismo las operaciones necesarias
- **AND** no pide confirmación de cada operación por separado

### Requirement: Acotar las operaciones disponibles

Las operaciones a disposición del asistente MUST limitarse a consultar la historia clínica y registrar
hechos clínicos. Una operación fuera de ese conjunto MUST NOT ejecutarse y MUST NOT presentarse como
resultado. Cuando el mensaje pida algo que ninguna operación disponible puede producir, el sistema MUST
declararlo en español y MUST NOT responderlo con una suposición.

#### Scenario: No ejecutar una operación fuera del conjunto disponible

- **WHEN** el mensaje pide borrar o modificar hechos ya registrados, o consultar la historia de otra persona
- **THEN** el sistema no ejecuta la operación
- **AND** explica en español que no puede hacerlo
- **AND** la historia clínica permanece exactamente como estaba

#### Scenario: Declarar lo que ninguna operación puede producir

- **WHEN** el mensaje pide algo que ni la consulta ni el registro pueden producir
- **THEN** el sistema declara que no puede atender esa parte
- **AND** no la responde con una suposición

### Requirement: Validar y escribir dentro del camino de la operación

Con independencia de cómo se haya decidido una operación, el sistema MUST validar de forma determinista
antes de incorporar cualquier hecho a la historia, y la escritura MUST ocurrir dentro del camino de esa
operación. Una operación que no pasa la validación MUST NOT producir escritura alguna. Una operación ya
completada MUST NOT revertirse porque otra operación del mismo turno falle después.

#### Scenario: No incorporar un hecho que no pasa la validación

- **WHEN** una operación de registro no pasa la validación determinista
- **THEN** el sistema no incorpora nada a la historia
- **AND** explica en español por qué el hecho no puede registrarse
- **AND** no deja ningún registro a medias

#### Scenario: Conservar lo completado ante un fallo posterior

- **WHEN** una operación falla después de que otra operación de registro ya se haya completado
- **THEN** el hecho ya registrado permanece en la historia
- **AND** la respuesta informa de lo que quedó registrado y de lo que no pudo completarse
- **AND** el sistema no presenta el turno como completo

#### Scenario: No delegar la escritura en quien decide la operación

- **WHEN** el asistente solicita una operación de registro
- **THEN** la comprobación y la escritura las realiza la aplicación dentro del camino de esa operación
- **AND** el asistente no escribe directamente en la historia clínica

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
