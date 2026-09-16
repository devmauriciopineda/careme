## MODIFIED Requirements

### Requirement: Declarar explícitamente la ausencia de registros

Cuando no existan hechos que respalden la pregunta, el sistema MUST declararlo de
forma explícita en español y MUST NOT completar la respuesta con conocimiento
general ni con suposiciones. La ausencia MUST declararse tanto cuando la búsqueda
no devuelve ningún hecho como cuando devuelve hechos registrados que no responden
a la pregunta. La declaración MUST identificar cuál de las siguientes situaciones
de ausencia aplica:

- la historia clínica no contiene ningún hecho registrado;
- la historia no contiene hechos del tipo consultado;
- la historia no contiene hechos en el periodo consultado;
- la historia contiene hechos, pero ninguno responde a la pregunta.

Cuando la ausencia se deba a un tipo o a un periodo consultado, la declaración
MUST acotar el alcance a ese tipo o a ese periodo y MUST NOT presentarlo como una
ausencia en toda la historia. La declaración MUST NOT afirmar ni sugerir que el
hecho no ocurrió: MUST dejar claro que la ausencia se refiere a lo que consta
registrado.

Un hecho recuperado que no responde a la pregunta MUST NOT presentarse como apoyo
de una respuesta ni usarse para responderla. La decisión de si los hechos
recuperados responden a la pregunta corresponde a quien compone la respuesta, no
a la búsqueda: MUST NOT darse por respondida una pregunta solo porque la búsqueda
devolvió algo.

#### Scenario: No hay hechos que respondan la pregunta
- **WHEN** la historia clínica no contiene hechos que respondan la pregunta
- **THEN** el sistema responde explícitamente que no encuentra registros
- **AND** no completa la respuesta con conocimiento general ni con suposiciones
- **AND** la historia clínica permanece sin cambios

#### Scenario: La búsqueda no devuelve ningún hecho
- **WHEN** la búsqueda en la historia no devuelve ningún hecho
- **THEN** el sistema declara la ausencia de registros conforme a esta especificación

#### Scenario: La búsqueda devuelve hechos que no responden a la pregunta
- **WHEN** la búsqueda devuelve hechos registrados que no responden a la pregunta
- **THEN** el sistema declara la ausencia de registros como si la búsqueda no hubiera devuelto nada
- **AND** identifica la situación de ausencia que aplica
- **AND** no responde a la pregunta con esos hechos
- **AND** no presenta ninguno de ellos como apoyo de una respuesta
- **AND** la historia clínica permanece sin cambios

#### Scenario: Historia clínica sin ningún hecho registrado
- **WHEN** la persona pregunta por información clínica propia y la historia no contiene ningún hecho registrado
- **THEN** el sistema declara que todavía no hay registros en la historia
- **AND** presenta esa situación como el estado inicial y no como un fallo
- **AND** no completa la respuesta con conocimiento general ni con suposiciones

#### Scenario: Ausencia acotada al tipo de hecho consultado
- **WHEN** la persona pregunta por un tipo de hecho que no está registrado en su historia
- **THEN** el sistema declara que no encuentra registros de ese tipo
- **AND** no deduce la respuesta a partir de hechos registrados de otro tipo

#### Scenario: Ausencia acotada al periodo consultado
- **WHEN** la persona pregunta por un periodo que no contiene hechos registrados
- **THEN** el sistema declara que no encuentra registros en ese periodo
- **AND** aclara que la ausencia se refiere a ese periodo y no a toda la historia

#### Scenario: Ausencia porque ningún hecho responde a la pregunta
- **WHEN** la búsqueda no encuentra hechos que respondan a la pregunta
- **THEN** el sistema declara que no encuentra registros que respondan a la pregunta
- **AND** no presenta ninguna afirmación como si estuviera respaldada

#### Scenario: La ausencia no afirma que el hecho no ocurriera
- **WHEN** el sistema declara que no encuentra registros para una pregunta
- **THEN** la declaración se refiere a lo que consta en la historia
- **AND** no afirma ni niega que el hecho haya ocurrido

## ADDED Requirements

### Requirement: Ofrecer una salida accionable tras declarar la ausencia

Toda declaración de ausencia MUST ofrecer a la persona un camino para continuar:
reformular la pregunta o registrar el hecho que no consta. La salida ofrecida
MUST NOT registrar nada por sí misma.

#### Scenario: Ofrecer continuar tras la ausencia
- **WHEN** el sistema declara que no encuentra registros para una pregunta
- **THEN** la declaración ofrece reformular la pregunta o registrar el hecho que no consta
- **AND** no se incorpora ningún hecho a la historia durante esa respuesta
- **AND** la historia clínica permanece sin cambios

#### Scenario: Registrar únicamente cuando la persona lo pide
- **WHEN** la persona indica que el hecho ocurrió pero no consta
- **THEN** el sistema le ofrece registrarlo por el cauce de registro clínico
- **AND** no incorpora el hecho por su cuenta en esa respuesta

### Requirement: Responder la parte respaldada y declarar la parte ausente

Cuando una misma pregunta pueda responderse en parte y en parte no, el sistema
MUST responder únicamente con la parte respaldada por hechos registrados, MUST
declarar de forma explícita qué parte no tiene registros y MUST NOT completar la
parte ausente con conocimiento general ni con suposiciones.

#### Scenario: Pregunta parcialmente respaldada
- **WHEN** la persona formula una pregunta en la que solo una parte está respaldada por hechos registrados
- **THEN** el sistema responde con la parte respaldada y sus referencias
- **AND** declara explícitamente la parte de la pregunta que no tiene registros
- **AND** no completa la parte ausente con conocimiento general ni con suposiciones

### Requirement: Mantener la declaración de ausencia ante la insistencia

Cuando la persona insista en una pregunta ya declarada sin registros, el sistema
MUST mantener la misma declaración, MUST NOT fabricar información para satisfacer
la petición y MUST NOT modificar la historia clínica.

#### Scenario: La persona insiste tras la negativa
- **WHEN** el sistema ha declarado que no encuentra registros y la persona insiste en la misma pregunta
- **THEN** el sistema mantiene la misma declaración de ausencia
- **AND** no fabrica información para satisfacer la petición
- **AND** la historia clínica permanece sin cambios

### Requirement: Distinguir la ausencia de registros de un fallo de búsqueda

Una búsqueda que no pudo completarse MUST NOT presentarse como una ausencia de
registros. El sistema MUST informar del fallo de forma recuperable y MUST NOT
declarar una ausencia que no ha podido comprobar.

#### Scenario: La búsqueda falla y no puede comprobarse la ausencia
- **WHEN** el sistema no puede completar la búsqueda en la historia
- **THEN** informa de un fallo recuperable en español sin mostrar detalles internos
- **AND** no declara una ausencia de registros que no ha podido comprobar
- **AND** conserva la pregunta para que la persona pueda volver a intentarlo

#### Scenario: La ausencia no se comunica como fallo
- **WHEN** la búsqueda se completa y no encuentra hechos que respondan la pregunta
- **THEN** el sistema declara la ausencia de registros
- **AND** no la comunica como un fallo del sistema
