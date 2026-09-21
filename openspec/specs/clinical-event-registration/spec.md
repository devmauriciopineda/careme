# clinical-event-registration Specification

## Purpose

Permite registrar hechos médicos propios expresados en lenguaje natural como
parte de la historia clínica personal, conservando fielmente su significado y
la precisión temporal aportada por la persona.

## Requirements

### Requirement: Registrar hechos clínicos propios

El sistema MUST identificar los hechos médicos propios que la persona expresa y registrar cada uno con un
tipo admitido: diagnóstico, medicación o nota. Una medición MUST NOT ser un tipo admitido de hecho
clínico: una medición que la persona menciona MUST atenderse por el cauce de las mediciones y MUST NOT
registrarse como un evento de la historia clínica. Cada evento MUST conservar el contenido expresado y
pertenecer a la única historia clínica de la persona. Un hecho MUST quedar disponible para consulta al
cerrarse la consulta en la que se recogió, no antes.

#### Scenario: Registrar un hecho médico con información suficiente
- **WHEN** un hecho recogido en una consulta alcanza la información mínima requerida
- **THEN** el sistema registra un evento con tipo, contenido y fecha con la precisión disponible al cerrar la consulta
- **AND** el evento queda disponible para consultas posteriores

#### Scenario: Registrar varios hechos del mismo mensaje
- **WHEN** un mensaje contiene varios hechos médicos propios distinguibles
- **THEN** el cierre de la consulta registra un evento independiente por cada uno
- **AND** cada evento declara su propia procedencia

#### Scenario: No registrar una medición como hecho clínico
- **WHEN** la persona menciona una medición
- **THEN** el sistema no la registra como un evento de la historia clínica
- **AND** la atiende por el cauce de las mediciones

#### Scenario: Rechazar un tipo de hecho no admitido
- **WHEN** un hecho clínico se propone con un tipo que no está admitido
- **THEN** el sistema no lo registra
- **AND** la historia clínica permanece sin cambios

### Requirement: Conservar la precisión temporal

El sistema MUST representar la fecha como exacta, aproximada o desconocida y no
MUST aumentar la precisión que la persona aportó. Cuando exista una expresión
temporal original, el sistema MUST conservarla además de la fecha interpretada.
Una fecha ausente MUST permanecer desconocida.

#### Scenario: Conservar una fecha aproximada
- **WHEN** la persona expresa un hecho con una fecha aproximada como "hace unos dos años" o "el mes pasado"
- **THEN** el sistema registra la fecha como aproximada y conserva la expresión original
- **AND** ninguna confirmación presenta esa fecha como exacta

#### Scenario: Registrar un hecho sin fecha
- **WHEN** la persona expresa un hecho sin indicar fecha
- **THEN** el sistema registra el evento con fecha desconocida

#### Scenario: Resolver una fecha relativa
- **WHEN** la persona expresa un hecho con una referencia relativa como "hoy", "ayer" o "hace dos semanas"
- **THEN** el sistema calcula la fecha tomando como referencia el día actual
- **AND** conserva la expresión temporal original

### Requirement: Rechazar contenido no registrable o ambiguo

El sistema MUST registrar únicamente hechos médicos vividos por la persona. No
MUST registrar preguntas, conversación general, creencias, sospechas ni
inferencias como hechos. Si el mensaje puede contener un hecho pero no aporta
información suficiente, el sistema MUST pedir una aclaración en español antes
de registrar cualquier evento.

#### Scenario: Pedir aclaración ante ambigüedad
- **WHEN** el mensaje podría referirse a un hecho médico pero no permite determinarlo con suficiente claridad
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no modifica la historia clínica

#### Scenario: No registrar una sospecha
- **WHEN** el mensaje expresa una creencia o sospecha y no un hecho vivido
- **THEN** el sistema no lo registra como evento
- **AND** explica en español que no se ha registrado como hecho

#### Scenario: Responder normalmente sin hecho médico
- **WHEN** el mensaje contiene una pregunta o conversación general sin hechos médicos
- **THEN** el sistema responde normalmente
- **AND** no modifica la historia clínica

### Requirement: Evitar duplicados dentro de la conversación

El sistema MUST reconocer cuando la persona vuelve a contar un hecho ya
registrado, o ya recogido en la consulta en curso, dentro de la misma
conversación. En ese caso MUST conservar el evento original, no crear otro evento
y comunicar que el hecho ya estaba registrado. Un hecho mencionado varias veces a
lo largo de la consulta MUST registrarse una sola vez, con la información más
completa recogida. La detección es **dentro de la misma conversación**: el mismo
hecho mencionado en otra conversación es un hecho nuevo, con su propia
procedencia, y no se compara con lo registrado antes.

#### Scenario: Repetir un hecho registrado
- **WHEN** la persona vuelve a contar en la misma conversación un hecho ya registrado
- **THEN** el sistema no crea un evento nuevo
- **AND** confirma el hecho existente conservando su contenido y precisión temporal

#### Scenario: Repetir un hecho ya recogido en la consulta
- **WHEN** la persona menciona varias veces a lo largo de la consulta el mismo hecho
- **THEN** el cierre lo registra una sola vez
- **AND** conserva la información más completa recogida

#### Scenario: No duplicar un hecho en el cierre
- **WHEN** el cierre encuentra en la consulta varias notas del mismo hecho
- **THEN** registra un solo evento
- **AND** el resumen lo menciona una sola vez

### Requirement: Confirmar el resultado y mantener atomicidad ante fallos

Tras un registro exitoso, el sistema MUST responder en español con una
confirmación breve que resuma el hecho o hechos registrados sin detalles
internos. La confirmación MUST producirse al cerrar la consulta, que es cuando se
registran los hechos, y MUST NOT presentarse antes como si el hecho ya estuviera
incorporado a la historia. Si falta la aclaración solicitada o no puede
completarse el registro, el sistema MUST dejar la historia exactamente sin
cambios para los hechos no completados, informar el resultado en español y
permitir volver a intentarlo cuando corresponda.

#### Scenario: Confirmar un registro exitoso
- **WHEN** el cierre de la consulta completa el registro de uno o más hechos
- **THEN** la persona recibe una confirmación breve en español que resume lo registrado

#### Scenario: No confirmar como registrado lo que solo se recogió
- **WHEN** un turno recoge un hecho clínico sin registrarlo
- **THEN** la respuesta no presenta el hecho como incorporado a la historia
- **AND** la persona no recibe una confirmación de registro antes del cierre

#### Scenario: No registrar sin aclaración suficiente
- **WHEN** la persona no aporta la aclaración necesaria después de una solicitud
- **THEN** el sistema no registra ningún evento por ese hecho
- **AND** la historia clínica permanece sin cambios

#### Scenario: Informar un fallo sin registro parcial
- **WHEN** el sistema no puede completar el registro de un hecho recogido
- **THEN** informa del fallo en español sin detalles internos
- **AND** no deja eventos incompletos ni parciales
- **AND** ofrece volver a intentarlo

#### Scenario: No destruir hechos ya registrados
- **WHEN** el registro de un hecho falla
- **THEN** los hechos registrados antes del intento permanecen intactos
- **AND** sus documentos Markdown y sus códigos siguen siendo consultables

### Requirement: Registrar eventos clínicos mediante la frontera conversacional

Las reglas existentes de registro de eventos clínicos MUST seguir siendo la autoridad cuando se invoquen desde el flujo conversacional. En ese flujo, la operación de registro MUST producirse al cerrar la consulta, con los hechos recogidos a lo largo de la conversación, y cada hecho MUST declarar su procedencia. La validación determinista MUST ocurrir dentro del camino de la operación de registro, antes de cualquier escritura, con independencia de quién haya decidido la operación. Un registro ya completado MUST NOT revertirse porque otra operación posterior falle. Los documentos Markdown en un directorio de eventos configurable, `data/events/` por defecto, MUST seguir siendo la fuente de verdad, PostgreSQL MUST seguir siendo un índice derivado consultable y la publicación MUST ser atómica desde la perspectiva de la persona.

#### Scenario: Registrar mediante el flujo conversacional
- **WHEN** el cierre de una consulta registra los hechos recogidos
- **THEN** el sistema invoca el comportamiento existente de registro clínico después de validar cada hecho de forma determinista
- **AND** la confirmación informa del evento registrado sin exponer detalles del almacenamiento interno

#### Scenario: Registrar solo lo recogido en la consulta
- **WHEN** el cierre registra los hechos de una consulta
- **THEN** se registran los hechos recogidos en esa consulta, que ya superaron la comprobación determinista al anotarse
- **AND** cada uno declara de qué consulta procede

#### Scenario: Mantener publicación atómica
- **WHEN** falla la publicación Markdown o la actualización del índice derivado
- **THEN** la respuesta del chat es `failed`
- **AND** no queda ningún evento parcial visible en Markdown ni mediante el índice derivado

#### Scenario: Reconstruir el índice derivado
- **WHEN** el índice PostgreSQL está vacío o es inconsistente con los documentos Markdown de eventos
- **THEN** el backend puede reconstruirlo desde Markdown sin tratar PostgreSQL como fuente de verdad

#### Scenario: Conservar un registro completado tras un fallo posterior
- **WHEN** un registro se completa y otra operación posterior falla
- **THEN** el documento Markdown del evento registrado y su código permanecen consultables
- **AND** el evento no se revierte ni se reescribe
- **AND** el fallo de la operación posterior no se presenta como un fallo del registro

### Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown

La ubicación de la fuente de verdad Markdown MUST ser configurable por el despliegue, con `data/events/` como valor por defecto. El despliegue MUST garantizar que el usuario del proceso pueda escribir en ese directorio y que los documentos sobrevivan a la recreación del contenedor. Cuando la persistencia falle, el sistema MUST registrar la causa en el servidor y MUST NOT exponerla a la persona.

#### Scenario: Configurar la ubicación de la fuente de verdad
- **WHEN** el despliegue define un directorio de eventos distinto al valor por defecto
- **THEN** el sistema escribe los documentos Markdown en el directorio configurado
- **AND** sin configuración explícita usa `data/events/`

#### Scenario: Escribir sin privilegios de root
- **WHEN** el backend corre como usuario sin privilegios en un contenedor
- **THEN** el directorio de eventos configurado es escribible por ese usuario
- **AND** el registro de un hecho no requiere permisos de root

#### Scenario: Conservar la fuente de verdad entre recreaciones
- **WHEN** el contenedor del backend se detiene y se recrea
- **THEN** los documentos Markdown de eventos anteriores siguen disponibles
- **AND** el índice derivado se reconstruye a partir de ellos

#### Scenario: Registrar la causa de un fallo de persistencia
- **WHEN** falla la publicación Markdown o la actualización del índice derivado
- **THEN** el servidor registra la causa con su traza
- **AND** la persona recibe un mensaje en español sin detalles internos

### Requirement: Asignar códigos de evento únicos y no reutilizables

Cada evento MUST recibir un código único derivado de los documentos persistidos. Un reinicio del backend MUST NOT provocar la reutilización de un código ya publicado, y un intento de publicar un documento cuyo código ya existe MUST fallar sin modificar el documento existente.

#### Scenario: Registrar después de un reinicio
- **WHEN** el backend se reinicia con documentos de eventos ya persistidos y registra un hecho nuevo
- **THEN** el hecho recibe un código que ningún documento existente usa
- **AND** los documentos existentes permanecen sin cambios

#### Scenario: Registrar varios hechos de un mismo mensaje
- **WHEN** un mensaje produce varios hechos registrables
- **THEN** cada hecho recibe un código distinto

#### Scenario: No sobrescribir un documento existente
- **WHEN** la publicación apuntaría a un documento que ya existe
- **THEN** la publicación falla
- **AND** el documento existente y su evento permanecen intactos
