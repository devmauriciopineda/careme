## MODIFIED Requirements

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
