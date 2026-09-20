## Purpose

Sostener la conversación como una consulta con identidad y ciclo de vida —abierta al iniciar la
conversación, cerrada cuando la persona lo decide— que recoge en notas los hechos que la persona
menciona y conserva al cerrarse un resumen propio de lo tratado.

## ADDED Requirements

### Requirement: Abrir una consulta con la conversación

El sistema MUST asociar cada conversación a una consulta con identidad propia, abierta al iniciar la
conversación y en curso mientras esa conversación esté activa. El sistema MUST NOT dejar una conversación
sin consulta ni una consulta sin conversación.

#### Scenario: Abrir la consulta al iniciar la conversación

- **WHEN** la persona inicia una conversación con el asistente
- **THEN** el sistema asocia a esa conversación una consulta con identidad propia
- **AND** la deja en curso

#### Scenario: Distinguir dos conversaciones por su consulta

- **WHEN** la persona mantiene dos conversaciones distintas en momentos distintos
- **THEN** cada una tiene su propia consulta con identidad propia
- **AND** los hechos de una no se atribuyen a la otra

### Requirement: Mantener una sola consulta en curso

El sistema MUST mantener, como máximo, una consulta en curso. Cuando la persona inicie una conversación
nueva existiendo otra consulta en curso, el sistema MUST cerrar la anterior con lo que tenga antes de
dejar en curso la nueva.

#### Scenario: Cerrar la consulta anterior al iniciar otra

- **WHEN** existe una consulta en curso y la persona inicia una conversación nueva
- **THEN** el sistema cierra la consulta anterior registrando lo admisible y guardando su resumen
- **AND** deja en curso únicamente la consulta de la conversación nueva

#### Scenario: No mantener dos consultas abiertas a la vez

- **WHEN** la persona inicia una conversación nueva
- **THEN** el sistema no deja dos consultas en curso
- **AND** no exige a la persona elegir entre consultas

### Requirement: Recoger en notas los hechos que la persona menciona

Mientras la consulta está en curso, el sistema MUST recoger en notas todo hecho clínico que la persona
mencione, sea o no el foco de su mensaje, con la información disponible y la precisión temporal aportada.
Una nota MUST NOT ser un hecho registrado ni MUST formar parte de la historia clínica: MUST NOT aparecer
en las consultas a la historia mientras la consulta esté abierta. Una nota MUST conservarse con la
consulta, de modo que una interrupción de la conversación no pierda lo que la persona contó.

#### Scenario: Recoger un hecho que no era el foco del mensaje

- **WHEN** la persona menciona un hecho clínico dentro de una conversación sobre otro asunto
- **THEN** el sistema lo recoge en una nota con la información disponible
- **AND** lo tiene en cuenta al cerrar la consulta

#### Scenario: Recoger varios hechos distintos de la misma conversación

- **WHEN** la persona menciona varios hechos clínicos distintos a lo largo de la conversación
- **THEN** el sistema los recoge por separado, sin fusionarlos
- **AND** no da por hecho uno por haber contado otro

#### Scenario: Mantener las notas fuera de la historia clínica

- **WHEN** la persona consulta su historia clínica mientras la consulta sigue en curso
- **THEN** las notas recogidas en esa consulta no aparecen como hechos registrados
- **AND** la historia clínica permanece como estaba antes de la consulta

#### Scenario: Conservar las notas ante una interrupción

- **WHEN** la conversación se interrumpe dejando hechos recogidos sin registrar
- **THEN** las notas recogidas siguen asociadas a la consulta
- **AND** el cierre puede registrarlas

### Requirement: Completar la información de lo recogido sin inventarla

El sistema MUST procurar completar la información de cada hecho recogido: MUST preguntar en español lo que
falta y MUST pedir aclaración de lo ambiguo, pudiendo servirse de una consulta a la historia clínica para
ampliar el contexto. El sistema MUST NOT completar con suposiciones lo que la persona no aporta.

#### Scenario: Preguntar lo que falta

- **WHEN** un hecho recogido no aporta la información necesaria para registrarse
- **THEN** el sistema pregunta en español la información que falta
- **AND** no la sustituye por una suposición

#### Scenario: Ampliar el contexto con lo ya registrado

- **WHEN** para completar un hecho recogido hace falta información que ya consta en la historia clínica
- **THEN** el sistema consulta la historia para completarlo
- **AND** no pide a la persona la información que ya consta

#### Scenario: Pedir aclaración de lo ambiguo

- **WHEN** un hecho recogido puede interpretarse de más de una manera
- **THEN** el sistema pide una aclaración comprensible en español
- **AND** no registra nada mientras siga siendo ambiguo

### Requirement: Cerrar la consulta cuando la persona lo decide y no reabrirla

La persona MUST poder dar por terminada la consulta en cualquier momento, con independencia de que la
información esté completa. El sistema MUST cerrar la consulta entonces, registrando los hechos admisibles
y guardando su resumen, MUST NOT retener a la persona ni bloquear su decisión, y MUST NOT reabrir ni
retomar una consulta cerrada.

#### Scenario: Terminar la consulta a petición de la persona

- **WHEN** la persona da por terminada la conversación
- **THEN** el sistema cierra la consulta con lo recogido hasta ese momento
- **AND** no retiene a la persona ni le exige completar la información pendiente

#### Scenario: Terminar la consulta con información incompleta

- **WHEN** la persona da por terminada la conversación quedando información por recoger
- **THEN** el sistema cierra la consulta igualmente
- **AND** registra los hechos recogidos como notas hasta ese momento

#### Scenario: No reabrir una consulta cerrada

- **WHEN** la persona intenta retomar o reabrir una consulta ya cerrada
- **THEN** el sistema explica en español que una consulta cerrada no se retoma
- **AND** le indica que puede contar lo que necesite en una consulta nueva
- **AND** la consulta cerrada permanece cerrada

### Requirement: Cerrar la consulta ante una interrupción

Cuando la conversación se interrumpa sin que la persona dé por terminada la consulta, el sistema MUST
cerrar la consulta con lo recogido hasta ese momento. Una consulta MUST NOT quedar abierta esperando a la
persona fuera de la conversación que la originó.

#### Scenario: Cerrar la consulta interrumpida

- **WHEN** la conversación se interrumpe sin que la persona dé por terminada la consulta
- **THEN** el sistema cierra la consulta con lo recogido
- **AND** registra los hechos admisibles
- **AND** no deja la consulta abierta esperando a la persona

#### Scenario: Consulta que permanece abierta por un cierre que no puede completarse

- **WHEN** el cierre de la consulta no puede completarse
- **THEN** la consulta no se da por cerrada y conserva lo recogido
- **AND** el cierre puede reintentarse
- **AND** es la única situación en la que una consulta permanece abierta fuera de su conversación

### Requirement: Conservar el resumen de la consulta

Al cerrarse, la consulta MUST conservar un resumen propio elaborado a partir de los hechos registrados y
de un motivo a modo de título generado automáticamente. El resumen MUST quedar marcado como información
derivada y MUST NOT presentarse como fuente de verdad: MUST poder reconstruirse desde los hechos de la
consulta. El motivo MUST NOT ser escrito por la persona ni MUST mostrársele. Una consulta que no pudo
guardar su resumen MUST seguir cerrada con sus hechos, sin perder ninguno.

#### Scenario: Guardar el resumen al cerrar

- **WHEN** la consulta se cierra habiendo hechos registrados
- **THEN** la consulta conserva un resumen elaborado a partir de ellos
- **AND** el resumen incluye un motivo a modo de título generado automáticamente
- **AND** el resumen queda marcado como información derivada

#### Scenario: Cerrar una consulta sin hechos que registrar

- **WHEN** la consulta se cierra sin hechos clínicos recogidos
- **THEN** el sistema no registra ningún hecho
- **AND** la consulta se cierra con su resumen
- **AND** la historia clínica permanece exactamente como estaba

#### Scenario: Cerrar aunque el resumen no pueda guardarse

- **WHEN** el resumen de la consulta no puede guardarse
- **THEN** la consulta queda cerrada con sus hechos y sin resumen
- **AND** el resumen puede reconstruirse después a partir de los hechos de la consulta
- **AND** no se pierde ningún hecho

#### Scenario: No mostrar el motivo a la persona

- **WHEN** la persona conversa con el asistente dentro de una consulta
- **THEN** el motivo de la consulta no se muestra en la conversación
- **AND** la persona no lo escribe

#### Scenario: No confundir la ausencia de hechos con un fallo

- **WHEN** una consulta se cierra sin hechos clínicos recogidos
- **THEN** la confirmación declara que no hubo hechos que registrar
- **AND** no se comunica como un fallo
