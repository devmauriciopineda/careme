# patient-profile Specification

## Purpose

Materializar y mantener los datos personales vigentes de la persona en un perfil separado de los hechos clínicos, permitiendo una entrevista inicial opcional y actualizaciones confirmadas durante las consultas.

## Requirements

### Requirement: Mantener los datos personales vigentes del perfil

El sistema MUST mantener para la única persona de la historia un perfil con nombre y apellidos, fecha de nacimiento, sexo, ocupación, grupo sanguíneo, factor Rh y estatura. Cada dato MUST conservar únicamente su valor vigente confirmado. La edad MUST derivarse de la fecha de nacimiento cuando exista y MUST NOT mantenerse como un dato independiente. El perfil MUST permitir que cualquiera de esos datos esté ausente cuando la persona no lo conozca, no lo aporte o no lo confirme.

#### Scenario: Consultar un perfil parcialmente completado

- **WHEN** el perfil contiene algunos datos confirmados y otros no aportados
- **THEN** el sistema conserva los datos confirmados como valores vigentes
- **AND** mantiene ausentes los datos no aportados
- **AND** no presenta una edad almacenada como dato independiente

#### Scenario: Derivar la edad de la fecha de nacimiento

- **WHEN** existe una fecha de nacimiento confirmada en el perfil
- **THEN** el sistema puede mostrar la edad calculada para la fecha de consulta
- **AND** no guarda la edad como un valor propio del perfil

#### Scenario: Mantener un dato desconocido como ausente

- **WHEN** la persona declara que desconoce un dato personal
- **THEN** el sistema mantiene ese dato ausente
- **AND** no lo completa con una suposición ni con conocimiento general

### Requirement: Ofrecer una entrevista inicial saltable y retomable

Cuando la persona interactúe por primera vez y el perfil esté vacío o incompleto, el sistema MUST poder proponer una entrevista para completar los datos ausentes. La persona MUST poder responderla, posponerla u omitirla. Omitir o posponer la entrevista MUST NOT impedir que continúe la conversación ni MUST exigir que complete todos los datos. Cuando la persona la retome, el sistema MUST preguntar únicamente por los datos que sigan ausentes.

#### Scenario: Proponer la entrevista en la primera interacción

- **WHEN** la persona inicia su primera interacción y el perfil está vacío
- **THEN** el sistema propone una entrevista para completar sus datos personales
- **AND** permite continuar la conversación sin completar la entrevista

#### Scenario: Omitir la entrevista

- **WHEN** la persona decide omitir o posponer la entrevista
- **THEN** el sistema continúa atendiendo la conversación
- **AND** no marca como confirmados los datos que la persona no aportó

#### Scenario: Retomar una entrevista pospuesta

- **WHEN** la persona retoma una entrevista y el perfil todavía tiene datos ausentes
- **THEN** el sistema pregunta por los datos que siguen ausentes
- **AND** no vuelve a exigir los datos ya confirmados

### Requirement: Actualizar el perfil al cerrar la consulta

El sistema MUST reunir los datos personales claros y confirmados durante una consulta y MUST actualizar el perfil al cerrarla. Si la persona comunica un nuevo valor válido para un dato existente, el sistema MUST sustituir el valor vigente por el último valor confirmado y MUST NOT conservar un historial propio del perfil. Una consulta sin datos del perfil MUST dejar el perfil sin cambios. Los hechos clínicos y las mediciones MUST seguir sus propios cauces y MUST NOT convertirse en datos del perfil por haber sido mencionados.

#### Scenario: Actualizar datos confirmados al cerrar

- **WHEN** la consulta se cierra después de que la persona haya confirmado uno o más datos del perfil
- **THEN** el sistema actualiza el perfil con esos datos
- **AND** conserva únicamente el último valor confirmado de cada dato
- **AND** deja ausentes los datos que no fueron aportados o confirmados

#### Scenario: Sustituir un valor corregido

- **WHEN** el perfil contiene un valor vigente y la persona confirma un nuevo valor para ese mismo dato durante la consulta
- **THEN** el sistema conserva el nuevo valor al cerrar la consulta
- **AND** descarta el valor anterior del perfil
- **AND** no crea un historial propio de ese dato

#### Scenario: Cerrar sin datos del perfil

- **WHEN** la consulta se cierra sin que la persona aporte datos del perfil
- **THEN** el sistema conserva el perfil exactamente como estaba

#### Scenario: Mantener separados los hechos clínicos

- **WHEN** la persona menciona un diagnóstico, medicamento, síntoma, medición u otro hecho clínico
- **THEN** el sistema lo atiende por el cauce clínico que corresponda
- **AND** no lo incorpora al perfil por el solo hecho de haberlo mencionado

### Requirement: Aclarar datos ambiguos y evitar actualizaciones parciales

Cuando un dato personal sea ambiguo, contradictorio o no pueda confirmarse, el sistema MUST pedir una aclaración comprensible en español y MUST NOT incorporarlo como confirmado mientras no exista confirmación. Si ya había un valor vigente, MUST conservarlo hasta que la persona confirme el reemplazo. Si la actualización del perfil no puede completarse, el sistema MUST conservar el perfil exactamente como estaba, informar de un fallo recuperable en español y permitir reintentarla.

#### Scenario: Pedir aclaración ante un dato ambiguo

- **WHEN** la persona comunica un dato personal ambiguo o contradictorio
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no incorpora el dato como confirmado mientras no la reciba
- **AND** conserva el valor vigente anterior si existía

#### Scenario: Actualizar datos válidos aunque otro necesite aclaración

- **WHEN** la persona aporta varios datos y uno de ellos necesita aclaración
- **THEN** el sistema puede actualizar al cierre los datos claros y confirmados
- **AND** mantiene sin cambios el dato que necesita aclaración

#### Scenario: Recuperarse de un fallo de actualización

- **WHEN** la actualización del perfil no puede completarse al cerrar la consulta
- **THEN** el sistema informa de un fallo recuperable en español sin detalles internos
- **AND** conserva el perfil exactamente como estaba
- **AND** permite reintentar la actualización
