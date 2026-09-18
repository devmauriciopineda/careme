# clinical-event-inspection Specification

## Purpose

Permite a la persona revisar directamente los eventos clínicos de su historia,
consultar sus detalles y aplicar filtros sin modificar la información registrada.

## Requirements

### Requirement: Listar los eventos clínicos registrados

El sistema MUST ofrecer una consulta de solo lectura que muestre los eventos de
la historia clínica de la persona. Cada elemento MUST permitir reconocer el
tipo, el contenido resumido, el código del evento, la fecha de ocurrencia y la
fecha de registro cuando estén disponibles. La lista MUST ordenarse por fecha de
registro, desde la más reciente hasta la más antigua, de forma predeterminada.
El sistema MUST permitir ordenar alternativamente por fecha de ocurrencia,
también desde la más reciente hasta la más antigua. Los eventos sin fecha de
ocurrencia MUST aparecer después de los eventos fechados cuando se use ese
criterio.

#### Scenario: Mostrar eventos por fecha de registro de forma predeterminada

- **WHEN** la persona abre la consulta de eventos y existen varios eventos registrados
- **THEN** el sistema muestra los eventos desde la fecha de registro más reciente hasta la más antigua
- **AND** cada evento muestra su tipo, código, fecha de ocurrencia y fecha de registro disponibles
- **AND** la historia clínica no se modifica

#### Scenario: Ordenar eventos por fecha de ocurrencia

- **WHEN** la persona selecciona la fecha de ocurrencia como criterio de orden
- **THEN** el sistema muestra los eventos desde la ocurrencia más reciente hasta la más antigua
- **AND** cada evento conserva visible su fecha de ocurrencia y su fecha de registro
- **AND** la historia clínica no se modifica

#### Scenario: Resolver empates de ordenación de forma determinista

- **WHEN** dos eventos tienen la misma fecha para el criterio de orden seleccionado
- **THEN** el sistema los desempata por el otro criterio temporal disponible
- **AND** si ambos criterios empatan, los ordena por código de evento
- **AND** la historia clínica no se modifica

#### Scenario: Colocar eventos sin fecha de ocurrencia al final

- **WHEN** la persona ordena por fecha de ocurrencia y existen eventos fechados y un evento sin fecha conocida
- **THEN** el sistema muestra primero los eventos con fecha de ocurrencia ordenados cronológicamente
- **AND** muestra el evento sin fecha de ocurrencia después de los eventos fechados
- **AND** indica que la fecha de ocurrencia del evento no está disponible

#### Scenario: Informar una historia sin eventos

- **WHEN** la persona abre la consulta y no hay eventos registrados
- **THEN** el sistema informa en español que todavía no hay eventos registrados
- **AND** presenta la situación como un estado vacío y no como un fallo
- **AND** no modifica la historia clínica

### Requirement: Filtrar eventos por tipo y periodo

El sistema MUST permitir filtrar la lista por tipo de evento y por periodo. Los
resultados MUST conservar el criterio de orden seleccionado, usando por defecto
la fecha de registro descendente, y los filtros MUST afectar únicamente a la
información mostrada.

#### Scenario: Filtrar por tipo de evento

- **WHEN** la persona selecciona un tipo de evento como filtro
- **THEN** el sistema muestra únicamente los eventos de ese tipo
- **AND** mantiene el criterio de orden seleccionado
- **AND** no crea, corrige, completa, edita ni elimina eventos

#### Scenario: Filtrar por periodo

- **WHEN** la persona selecciona un periodo como filtro
- **THEN** el sistema muestra únicamente los eventos cuya fecha corresponde al periodo
- **AND** mantiene el criterio de orden seleccionado
- **AND** no modifica ningún evento de la historia

#### Scenario: Retirar un filtro

- **WHEN** la persona retira un filtro activo
- **THEN** el sistema vuelve a mostrar los eventos que corresponden a los filtros restantes
- **AND** conserva el criterio de orden seleccionado, o la fecha de registro descendente si no se eligió otro
- **AND** la historia clínica permanece sin cambios

### Requirement: Consultar el detalle de un evento

El sistema MUST permitir seleccionar un evento de la lista y mostrar su contenido
registrado, tipo, código y datos temporales. El detalle MUST conservar el grado
de precisión temporal registrado y MUST ser exclusivamente de lectura.

#### Scenario: Mostrar el detalle de un evento

- **WHEN** la persona selecciona un evento disponible
- **THEN** el sistema muestra el contenido del evento y sus datos identificativos
- **AND** muestra la fecha de ocurrencia con el grado de precisión registrado
- **AND** muestra la fecha de registro
- **AND** la historia clínica no se modifica

#### Scenario: Volver a la lista desde el detalle

- **WHEN** la persona vuelve a la lista después de consultar un detalle
- **THEN** el sistema muestra de nuevo la lista con el mismo orden
- **AND** conserva el criterio de orden seleccionado
- **AND** conserva los filtros que estaban activos
- **AND** la historia clínica permanece sin cambios

#### Scenario: Evento seleccionado no disponible

- **WHEN** la persona selecciona un evento que ya no puede consultarse
- **THEN** el sistema informa en español que el evento no está disponible
- **AND** no muestra un detalle incompleto como si fuera definitivo
- **AND** permite volver a revisar la lista sin modificar la historia

### Requirement: Presentar fechas relativas como fechas estimadas

Cuando un evento se registró con una expresión temporal relativa, el sistema
MUST mostrar una fecha estimada calculada tomando como referencia la fecha en que
se guardó el registro. La fecha MUST identificarse como aproximada y la consulta
MUST NOT mostrar la expresión relativa original.

#### Scenario: Estimar la fecha de una expresión relativa

- **WHEN** la persona consulta un evento registrado con una expresión como "hace dos semanas"
- **THEN** el sistema muestra la fecha estimada calculada desde la fecha de guardado del registro
- **AND** identifica la fecha como aproximada
- **AND** no muestra la expresión relativa original
- **AND** muestra la fecha de registro del evento

#### Scenario: Conservar una fecha aproximada no relativa

- **WHEN** la persona consulta un evento cuya fecha quedó registrada como aproximada
- **THEN** el sistema muestra la fecha disponible con indicación de aproximación
- **AND** no presenta la fecha como exacta
- **AND** no añade una precisión que no conste en el registro

### Requirement: Mantener la consulta de eventos como operación de solo lectura

La inspección, el detalle, los filtros, la ordenación y la navegación MUST NOT crear, completar,
corregir, editar ni eliminar eventos. Un fallo de consulta MUST distinguirse del
estado vacío y MUST permitir reintentar sin alterar la historia.

#### Scenario: Reintentar una consulta fallida

- **WHEN** el sistema no puede obtener los eventos solicitados
- **THEN** informa en español de un fallo recuperable sin mostrar detalles internos
- **AND** no presenta datos incompletos como si fueran definitivos
- **AND** permite volver a intentarlo
- **AND** la historia clínica permanece exactamente como estaba

#### Scenario: Inspeccionar sin modificar la historia

- **WHEN** la persona lista eventos, aplica filtros, consulta un detalle o vuelve a la lista
- **THEN** el sistema solo cambia la información que muestra
- **AND** no crea, completa, corrige, edita ni elimina ningún evento
