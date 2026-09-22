# clinical-measurement-view Specification

## Purpose

Permitir que la persona consulte visualmente la evolución de cualquier métrica registrada, seleccione las métricas que desea comparar y conserve una lectura tabular verificable por fecha.

## Requirements

### Requirement: Exponer el contrato de lectura del seguimiento

El backend MUST ofrecer un catálogo de las métricas admitidas mediante
`GET /api/v1/measurements/catalog`. Cada entrada MUST incluir un código estable,
una etiqueta, su unidad de referencia y la definición ordenada de sus
componentes.

El backend MUST ofrecer las mediciones de una métrica mediante
`GET /api/v1/measurements/tracking/{metricCode}`. La respuesta MUST incluir la
métrica solicitada y sus mediciones ordenadas por fecha ascendente. Cada
medición MUST incluir identificador, fecha ISO `yyyy-MM-dd` y los valores por
componente en la unidad de referencia del catálogo.

La presión arterial MUST conservarse como una única métrica con los componentes
`systolic` y `diastolic`; no se puede exponer como dos métricas independientes.
Estos endpoints son de solo lectura y MUST NOT modificar el contrato de registro,
importación ni consulta conversacional.

#### Scenario: Leer el catálogo y una métrica

- **WHEN** la vista solicita el catálogo y los datos de una métrica admitida
- **THEN** recibe la etiqueta, unidad de referencia y componentes del catálogo
- **AND** recibe sus mediciones con fecha y valores en la unidad de referencia
- **AND** las mediciones están ordenadas de la más antigua a la más reciente

#### Scenario: Leer una métrica compuesta

- **WHEN** la vista solicita presión arterial
- **THEN** recibe una única métrica con sus componentes `systolic` y `diastolic`
- **AND** cada fecha conserva ambos valores en mmHg

#### Scenario: Fallo aislado de lectura

- **WHEN** falla la lectura de una métrica concreta
- **THEN** la solicitud de esa métrica falla de forma recuperable
- **AND** las solicitudes de las demás métricas no quedan invalidadas
- **AND** la vista puede reintentar únicamente la métrica afectada

### Requirement: Ofrecer las métricas registradas para el seguimiento

La vista MUST obtener las métricas del catálogo y MUST ofrecer para seleccionar únicamente las métricas que tengan al menos una medición registrada. La vista MUST admitir métricas nuevas del catálogo sin limitarse a peso y circunferencia abdominal. Cuando tengan mediciones, peso y circunferencia abdominal MUST aparecer seleccionados por defecto.

#### Scenario: Ofrecer las métricas con datos

- **WHEN** la persona abre el seguimiento y existen mediciones de varias métricas del catálogo
- **THEN** el sistema ofrece esas métricas para seleccionar
- **AND** no ofrece las métricas del catálogo que no tienen mediciones
- **AND** selecciona por defecto peso y circunferencia abdominal cuando tienen mediciones

#### Scenario: Incorporar una métrica nueva del catálogo

- **WHEN** el catálogo contiene una métrica nueva y existen mediciones registradas de ella
- **THEN** la métrica aparece entre las disponibles para seleccionar
- **AND** se muestra con su unidad de referencia
- **AND** no cambia la forma en que se muestran las métricas ya existentes

#### Scenario: No hay mediciones

- **WHEN** la persona abre el seguimiento y no existe ninguna medición registrada
- **THEN** el sistema informa de que todavía no hay datos
- **AND** no ofrece métricas para seleccionar
- **AND** muestra el estado vacío del seguimiento

### Requirement: Mostrar gráficas y detalle de las métricas seleccionadas

Por cada métrica seleccionada, la vista MUST mostrar una gráfica individual con su unidad de referencia. La vista MUST mostrar una única tabla con las fechas ordenadas de la más antigua a la más reciente y una columna por cada métrica seleccionada. Cambiar la selección MUST actualizar las gráficas y las columnas de la tabla sin incluir métricas ocultas.

#### Scenario: Mostrar el seguimiento por defecto

- **WHEN** la persona abre el seguimiento con peso y circunferencia abdominal seleccionados
- **THEN** el sistema muestra una gráfica individual para cada métrica
- **AND** muestra una única tabla con las columnas de ambas métricas
- **AND** ordena las mediciones de la más antigua a la más reciente
- **AND** indica la unidad de referencia de cada métrica

#### Scenario: Cambiar las métricas visibles

- **WHEN** la persona selecciona una métrica disponible y oculta otra
- **THEN** el sistema muestra la gráfica de la métrica seleccionada
- **AND** oculta la gráfica de la métrica que dejó de estar seleccionada
- **AND** actualiza la tabla para incluir únicamente las columnas de las métricas seleccionadas

#### Scenario: Cambiar entre tendencia y detalle por fecha

- **WHEN** la persona cambia entre la vista de tendencia y el detalle por fecha
- **THEN** ambas vistas muestran las mismas métricas seleccionadas
- **AND** el detalle por fecha conserva los valores correspondientes a esas métricas

#### Scenario: Mostrar la presión arterial

- **WHEN** la persona selecciona la presión arterial
- **THEN** el sistema muestra una única gráfica para esa métrica
- **AND** la gráfica contiene una serie para la presión sistólica y otra para la diastólica
- **AND** la tabla presenta la presión arterial como una única métrica con sus dos valores

### Requirement: Aislar fallos de recuperación por métrica

Cuando no sea posible obtener los datos de una métrica, el sistema MUST informar del fallo de esa métrica en español y MUST ofrecer reintentarla. El fallo MUST NOT ocultar ni invalidar las demás métricas disponibles, y MUST NOT presentarse como ausencia de datos de todo el seguimiento.

#### Scenario: Fallo de una métrica con otras disponibles

- **WHEN** el sistema no puede obtener los datos de una métrica y existen otras métricas disponibles
- **THEN** informa del fallo de la métrica afectada
- **AND** ofrece reintentar la métrica afectada
- **AND** mantiene disponibles las demás métricas
- **AND** no declara que todo el seguimiento carece de datos
