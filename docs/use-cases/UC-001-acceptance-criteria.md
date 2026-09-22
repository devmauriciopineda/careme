# UC-001 — Ver el seguimiento corporal — Acceptance Criteria

## Feature

**ID:** UC-001  
**Use case:** Ver el seguimiento corporal  
**Source:** [UC-001.md](./UC-001.md)

La persona consulta la evolución de las métricas corporales que tienen datos,
puede elegir cuáles mostrar y ve sus tendencias y valores por fecha.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`.

## Scenarios

### Scenario: Mostrar el seguimiento de las métricas seleccionadas

**Ref:** Flujo principal / UC-001-R1, UC-001-R2, UC-001-R3, UC-001-R4, UC-001-R5, UC-001-R6, UC-001-R7

Given hay mediciones registradas de peso y circunferencia abdominal  
And el catálogo contiene las métricas admitidas y sus unidades de referencia  
When la persona abre el seguimiento corporal  
Then el sistema ofrece el peso y la circunferencia abdominal para seleccionar  
And los muestra seleccionados por defecto  
And muestra una gráfica individual para cada métrica seleccionada  
And muestra una única tabla con una columna para cada métrica seleccionada  
And presenta las mediciones ordenadas de la más antigua a la más reciente  
And muestra la unidad de referencia de cada métrica

### Scenario: Mostrar una métrica nueva del catálogo

**Ref:** Flujo principal / UC-001-R2, UC-001-R4, UC-001-R6, UC-001-R7 / UC-001-R8

Given existe una métrica nueva en el catálogo  
And hay mediciones registradas de esa métrica  
When la persona abre el seguimiento corporal  
Then la métrica aparece entre las disponibles para seleccionar  
And puede seleccionarla junto con otras métricas  
And se muestra en su propia gráfica  
And aparece en la columna correspondiente de la tabla  
And se muestra en su unidad de referencia

### Scenario: Ocultar y volver a mostrar métricas

**Ref:** A3 / UC-001-R4, UC-001-R5, UC-001-R6, UC-001-R7

Given hay varias métricas disponibles  
When la persona cambia las métricas seleccionadas  
Then el sistema muestra únicamente las gráficas de las métricas seleccionadas  
And la tabla contiene únicamente las columnas de esas métricas  
And las fechas de la tabla siguen correspondiendo a las mediciones mostradas

### Scenario: Mostrar la presión arterial con dos series

**Ref:** A4 / UC-001-R8

Given hay mediciones registradas de presión arterial  
When la persona selecciona la presión arterial  
Then el sistema muestra una única gráfica para la presión arterial  
And la gráfica contiene una serie para la presión sistólica y otra para la diastólica  
And la tabla presenta la presión arterial como una única métrica con sus dos valores

### Scenario: Informar de que no hay mediciones

**Ref:** A1

Given no hay ninguna medición registrada  
When la persona abre el seguimiento corporal  
Then el sistema informa de que todavía no hay datos  
And muestra la pantalla vacía  
And no ofrece métricas para seleccionar

### Scenario: Excluir una métrica sin datos

**Ref:** Regla de negocio: solo se ofrecen métricas con mediciones registradas

Given una métrica del catálogo no tiene mediciones registradas  
And existen mediciones de al menos otra métrica  
When la persona abre el seguimiento corporal  
Then la métrica sin datos no aparece entre las disponibles para seleccionar  
And las métricas con datos siguen disponibles

### Scenario: Cambiar entre tendencia y detalle por fecha

**Ref:** A2

Given hay métricas seleccionadas y mediciones registradas  
When la persona cambia entre la tendencia y el detalle por fecha  
Then ambas vistas muestran información de las mismas métricas seleccionadas  
And la tabla muestra los valores correspondientes a esas métricas

### Scenario: Aislar un fallo de una métrica

**Ref:** E1

Given hay mediciones disponibles para varias métricas  
When el sistema no puede obtener los datos de una métrica  
Then informa del fallo de esa métrica en el idioma de la persona  
And ofrece reintentar esa métrica  
And mantiene disponibles las demás métricas  
And no presenta el fallo como ausencia de datos de todas las métricas

## Coverage notes

- **Happy path:** cubierto por `Mostrar el seguimiento de las métricas seleccionadas`.
- **Alternate flows:** `A1`, `A2`, `A3` y `A4` tienen un escenario cada uno.
- **Exceptions:** `E1` tiene un escenario. No aplica ninguna otra excepción.
- **Edge cases:** la métrica sin datos queda cubierta por `Excluir una métrica sin datos`; la ampliación del catálogo queda cubierta por `Mostrar una métrica nueva del catálogo`.
- **Automatización:** los escenarios describen comportamiento observable y pueden convertirse en pruebas sin añadir decisiones de negocio.
