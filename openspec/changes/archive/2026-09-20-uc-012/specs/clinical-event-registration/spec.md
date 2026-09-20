## MODIFIED Requirements

### Requirement: Registrar eventos clínicos mediante la frontera conversacional

Las reglas existentes de registro de eventos clínicos MUST seguir siendo la autoridad cuando se invoquen desde el flujo conversacional. La validación determinista MUST ocurrir dentro del camino de la operación de registro, antes de cualquier escritura, con independencia de quién haya decidido la operación. Un registro ya completado MUST NOT revertirse porque otra operación del mismo turno falle después. Los documentos Markdown en un directorio de eventos configurable, `data/events/` por defecto, MUST seguir siendo la fuente de verdad, PostgreSQL MUST seguir siendo un índice derivado consultable y la publicación MUST ser atómica desde la perspectiva de la persona.

#### Scenario: Registrar mediante el flujo conversacional

- **WHEN** el asistente solicita una operación de registro durante un turno
- **THEN** el sistema invoca el comportamiento existente de registro clínico después de validar la operación de forma determinista
- **AND** la respuesta confirma el evento registrado sin exponer detalles del almacenamiento interno

#### Scenario: Mantener publicación atómica

- **WHEN** falla la publicación Markdown o la actualización del índice derivado
- **THEN** la respuesta del chat es `failed`
- **AND** no queda ningún evento parcial visible en Markdown ni mediante el índice derivado

#### Scenario: Reconstruir el índice derivado

- **WHEN** el índice PostgreSQL está vacío o es inconsistente con los documentos Markdown de eventos
- **THEN** el backend puede reconstruirlo desde Markdown sin tratar PostgreSQL como fuente de verdad

#### Scenario: Conservar un registro completado tras un fallo posterior

- **WHEN** un registro se completa y otra operación del mismo turno falla después
- **THEN** el documento Markdown del evento registrado y su código permanecen consultables
- **AND** el evento no se revierte ni se reescribe
- **AND** el fallo de la operación posterior no se presenta como un fallo del registro
