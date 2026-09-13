## Purpose

Permite registrar hechos médicos propios expresados en lenguaje natural como
parte de la historia clínica personal, conservando fielmente su significado y
la precisión temporal aportada por la persona.

## ADDED Requirements

### Requirement: Registrar hechos clínicos propios

El sistema MUST identificar los hechos médicos propios que la persona expresa y
registrar cada uno con un tipo admitido: diagnóstico, medicación, medición o
nota. Cada evento MUST conservar el contenido expresado, pertenecer a la única
historia clínica de la persona y quedar disponible inmediatamente después de
registrarse.

#### Scenario: Registrar un hecho médico con información suficiente
- **WHEN** la persona envía un mensaje que contiene un hecho médico propio registrable
- **THEN** el sistema registra un evento con tipo, contenido y fecha con la precisión disponible
- **AND** el evento queda disponible para consultas posteriores

#### Scenario: Registrar varios hechos del mismo mensaje
- **WHEN** un mensaje contiene varios hechos médicos propios distinguibles
- **THEN** el sistema registra un evento independiente por cada hecho

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
registrado en la conversación en curso. En ese caso MUST conservar el evento
original, no crear otro evento y comunicar que el hecho ya estaba registrado.

#### Scenario: Repetir un hecho registrado
- **WHEN** la persona vuelve a contar en la misma conversación un hecho ya registrado
- **THEN** el sistema no crea un evento nuevo
- **AND** confirma el hecho existente conservando su contenido y precisión temporal

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
