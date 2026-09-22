## Why

La vista de seguimiento corporal sigue limitada a peso y circunferencia abdominal, aunque el catálogo de métricas ya admite presión arterial, colesterol y futuras métricas. La persona necesita consultar esa evolución en un único lugar, elegir qué métricas comparar visualmente y distinguir un fallo de una métrica sin perder las demás.

## What Changes

- Ampliar la vista de seguimiento para incluir cualquier métrica del catálogo que tenga mediciones registradas.
- Ofrecer un selector de métricas; seleccionar por defecto peso y circunferencia abdominal cuando estén disponibles.
- Mostrar una gráfica individual por cada métrica seleccionada y mantener una tabla única con las columnas de esas métricas.
- Representar la presión arterial como una única métrica con dos series: sistólica y diastólica.
- Mantener las vistas de tendencia y detalle por fecha con las mismas métricas seleccionadas.
- Ocultar del selector las métricas del catálogo que no tengan mediciones.
- Aislar los fallos de recuperación por métrica, permitiendo reintentar la métrica afectada y conservar las demás disponibles.
- Actualizar UC-001 y sus criterios de aceptación como documentación funcional consolidada.

## Capabilities

### New Capabilities

- `clinical-measurement-view`: consultar visualmente la evolución de las métricas registradas mediante gráficas seleccionables y una tabla de detalle.

### Modified Capabilities

No se modifican los requisitos de las capabilities existentes. La vista requiere
un contrato de lectura explícito para exponer el catálogo admitido y las
mediciones por métrica; este contrato no cambia el registro legacy ni la
consulta conversacional.

## Impact

- Afecta la página de seguimiento corporal y sus componentes de selección, gráficas, tabla, estados vacíos y errores.
- Añade lecturas read-only en `GET /api/v1/measurements/catalog` y
	`GET /api/v1/measurements/tracking/{metricCode}` para que la vista consuma el
	catálogo y pueda reintentar una métrica de forma aislada.
- Mantiene sin cambios el contrato de registro de `POST /api/v1/measurements`,
	la importación y la conversación del asistente.
- Requiere pruebas de componente y de integración de la vista para métricas iniciales, métricas nuevas, presión arterial, ausencia individual y fallo aislado.
- No introduce autenticación, recomendaciones clínicas, objetivos ni modificación de mediciones.
