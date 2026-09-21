## ADDED Requirements

### Requirement: Recoger y registrar las mediciones de la consulta

Mientras la consulta está en curso, el sistema MUST recoger en notas las mediciones que la persona
menciona, además de los hechos clínicos, con la métrica, el valor, la unidad y la fecha disponibles. Una
nota de medición MUST NOT ser una medición registrada ni MUST formar parte del seguimiento hasta que la
consulta se cierre. Al cerrarse la consulta, el sistema MUST registrar las mediciones admisibles de su
propio cauce, además de los hechos admisibles, y una consulta MUST poder cerrarse con hechos y mediciones
a la vez, cada uno por su cauce.

#### Scenario: Recoger una medición mencionada

- **WHEN** la persona menciona una medición mientras la consulta está en curso
- **THEN** el sistema la recoge en una nota de la consulta con su métrica, su valor, su unidad y su fecha disponibles
- **AND** no la incorpora todavía al seguimiento

#### Scenario: Registrar las mediciones al cerrar

- **WHEN** la consulta se cierra habiendo recogido mediciones
- **THEN** el sistema registra al cerrarlas las mediciones admisibles de su propio cauce
- **AND** la consulta queda cerrada con ellas

#### Scenario: Cerrar una consulta con hechos y mediciones

- **WHEN** la consulta se cierra habiendo recogido hechos clínicos y mediciones
- **THEN** el sistema registra los hechos admisibles como hechos clínicos
- **AND** registra las mediciones admisibles en el seguimiento de mediciones
- **AND** no mezcla un cauce con el otro
