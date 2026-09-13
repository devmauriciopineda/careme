## MODIFIED Requirements

### Requirement: Registrar eventos clínicos mediante la frontera conversacional

Las reglas existentes de registro de eventos clínicos MUST seguir siendo la autoridad cuando se invoquen desde el flujo conversacional. Un resultado del chat MAY solicitar registro solo después de que la intención estructurada haya pasado validación determinista. Los documentos Markdown en un directorio de eventos configurable, `data/events/` por defecto, MUST seguir siendo la fuente de verdad, PostgreSQL MUST seguir siendo un índice derivado consultable y la publicación MUST ser atómica desde la perspectiva de la persona.

#### Scenario: Registrar mediante el flujo conversacional
- **WHEN** una conversación válida produce una intención `EVENTS`
- **THEN** el sistema invoca el comportamiento existente de registro clínico
- **AND** la respuesta confirma el evento registrado sin exponer detalles del almacenamiento interno

#### Scenario: Mantener publicación atómica
- **WHEN** falla la publicación Markdown o la actualización del índice derivado
- **THEN** la respuesta del chat es `failed`
- **AND** no queda ningún evento parcial visible en Markdown ni mediante el índice derivado

#### Scenario: Reconstruir el índice derivado
- **WHEN** el índice PostgreSQL está vacío o es inconsistente con los documentos Markdown de eventos
- **THEN** el backend puede reconstruirlo desde Markdown sin tratar PostgreSQL como fuente de verdad

### Requirement: Confirmar el resultado y mantener atomicidad ante fallos

Tras un registro exitoso, el sistema MUST responder en español con una
confirmación breve que resuma el hecho o hechos registrados sin detalles
internos. Si falta la aclaración solicitada o no puede completarse el registro,
el sistema MUST dejar la historia exactamente sin cambios, informar el resultado
en español y permitir volver a intentarlo cuando corresponda.

#### Scenario: Confirmar un registro exitoso
- **WHEN** el sistema completa el registro de uno o más hechos
- **THEN** la persona recibe una confirmación breve en español que resume lo registrado

#### Scenario: No registrar sin aclaración suficiente
- **WHEN** la persona no aporta la aclaración necesaria después de una solicitud
- **THEN** el sistema no registra ningún evento
- **AND** la historia clínica permanece sin cambios

#### Scenario: Informar un fallo sin registro parcial
- **WHEN** el sistema no puede completar el registro de un mensaje registrable
- **THEN** informa del fallo en español sin detalles internos
- **AND** no deja eventos incompletos ni parciales
- **AND** ofrece volver a intentarlo

#### Scenario: No destruir hechos ya registrados
- **WHEN** el registro de un mensaje registrable falla
- **THEN** los hechos registrados antes del intento permanecen intactos
- **AND** sus documentos Markdown y sus códigos siguen siendo consultables

## ADDED Requirements

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
