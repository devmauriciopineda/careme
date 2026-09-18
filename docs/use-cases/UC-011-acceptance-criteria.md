# UC-011 — Inspeccionar los eventos clínicos registrados — Acceptance Criteria

## Feature

**ID:** UC-011  
**Use case:** Inspeccionar los eventos clínicos registrados  
**Source:** [UC-011.md](./UC-011.md)

La persona puede revisar directamente los eventos de su historia clínica,
consultar el detalle y filtrarlos sin modificar la información registrada.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario incluye una referencia al flujo, excepción o regla de origen.

## Scenarios

### Scenario: Mostrar los eventos registrados en orden cronológico

**Ref:** Flujo principal

Given el sistema está disponible y la persona tiene acceso a su historia clínica  
And la historia contiene varios eventos registrados con fecha  
When la persona entra en el espacio para revisar los eventos  
Then el sistema muestra los eventos desde el más reciente hasta el más antiguo  
And cada evento permite reconocer su tipo y su fecha  
And la historia clínica no se modifica

### Scenario: Consultar el detalle de un evento

**Ref:** Flujo principal, pasos 5 y 6

Given la lista de eventos registrados está disponible  
When la persona selecciona un evento  
Then el sistema muestra el contenido de ese evento  
And conserva el grado de precisión temporal con que fue registrado  
And la historia clínica no se modifica

### Scenario: Informar que no existen eventos registrados

**Ref:** A1 — La historia clínica no contiene ningún evento

Given la persona tiene acceso a su historia clínica  
And la historia no contiene ningún evento registrado  
When la persona entra en el espacio para revisar los eventos  
Then el sistema informa claramente que todavía no hay eventos registrados  
And no presenta la situación como un fallo  
And la historia clínica permanece sin cambios

### Scenario: Filtrar eventos por tipo de hecho

**Ref:** A2 — La persona filtra los eventos por tipo de hecho

Given la historia contiene eventos de varios tipos  
When la persona selecciona un tipo de hecho como filtro  
Then el sistema muestra únicamente los eventos de ese tipo  
And los muestra desde el más reciente hasta el más antiguo  
And la historia clínica no se modifica

### Scenario: Filtrar eventos por periodo

**Ref:** A3 — La persona filtra los eventos por un periodo

Given la historia contiene eventos correspondientes a varios periodos  
When la persona selecciona un periodo como filtro  
Then el sistema muestra únicamente los eventos cuya fecha corresponde al periodo indicado  
And los muestra desde el más reciente hasta el más antiguo  
And la historia clínica no se modifica

### Scenario: Mostrar como aproximada una fecha relativa resuelta

**Ref:** A4 — El evento conserva una fecha relativa al registrarse

Given la historia contiene un evento cuya fecha se expresó como “hace dos semanas” al guardarlo  
And se conoce la fecha en que se guardó el registro  
When la persona revisa la lista o el detalle del evento  
Then el sistema muestra la fecha estimada que corresponde a esa expresión tomando como referencia la fecha de guardado  
And identifica la fecha como aproximada  
And no muestra la expresión “hace dos semanas”  
And la historia clínica no se modifica

### Scenario: Mostrar un evento sin fecha conocida al final

**Ref:** A5 — El evento no tiene fecha conocida

Given la historia contiene eventos fechados y un evento cuya fecha es desconocida  
When la persona revisa los eventos  
Then el sistema muestra que la fecha del evento es desconocida o no está disponible  
And coloca el evento después de los eventos con fecha  
And la historia clínica no se modifica

### Scenario: Conservar orden y filtros al volver del detalle

**Ref:** A6 — La persona vuelve de un detalle a la lista

Given la persona está viendo una lista de eventos con un filtro activo  
When vuelve a la lista después de consultar un detalle  
Then el sistema conserva el filtro activo  
And conserva el orden del más reciente al más antiguo  
And la historia clínica no se modifica

### Scenario: Informar un fallo al obtener los eventos

**Ref:** E1 — El sistema no puede obtener los eventos

Given la persona solicita revisar los eventos registrados  
When el sistema no puede obtener la información  
Then informa del fallo en español sin mostrar datos incompletos como definitivos  
And no modifica la historia clínica  
And permite volver a intentarlo

### Scenario: Informar que un evento no está disponible

**Ref:** E2 — El evento solicitado ya no puede consultarse

Given la persona tiene una lista de eventos disponible  
When selecciona un evento que ya no puede consultarse  
Then el sistema informa de que el evento no está disponible  
And mantiene la historia clínica sin cambios  
And permite volver a revisar la lista

## Coverage notes

- **Happy path:** Cubierto por `Mostrar los eventos registrados en orden cronológico` y `Consultar el detalle de un evento`.
- **Alternate flows:** Cubiertos por `Informar que no existen eventos registrados` (`A1`), `Filtrar eventos por tipo de hecho` (`A2`), `Filtrar eventos por periodo` (`A3`), `Mostrar como aproximada una fecha relativa resuelta` (`A4`), `Mostrar un evento sin fecha conocida al final` (`A5`) y `Conservar orden y filtros al volver del detalle` (`A6`).
- **Exceptions:** Cubiertas por `Informar un fallo al obtener los eventos` (`E1`) e `Informar que un evento no está disponible` (`E2`).
- **Edge cases:** No aplican como categoría independiente; las variaciones explícitamente definidas por el caso de uso están cubiertas por el flujo principal, `A1`–`A6` y `E1`–`E2`.

## Read-only boundary scenarios

Estos escenarios verifican que la inspección es exclusivamente de consulta.

### Scenario: No modificar la historia al filtrar

**Ref:** Regla de negocio — los filtros no modifican la historia

Given la historia contiene eventos registrados  
When la persona aplica o retira un filtro  
Then el sistema cambia únicamente los eventos que muestra  
And no crea, corrige, completa, edita ni elimina ningún evento

### Scenario: No modificar la historia al consultar un detalle

**Ref:** Regla de negocio — la inspección es exclusivamente de consulta

Given la historia contiene un evento registrado  
When la persona consulta su detalle  
Then el sistema muestra el contenido registrado  
And no modifica el evento ni ningún otro evento de la historia
