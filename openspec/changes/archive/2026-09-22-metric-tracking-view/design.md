## Context

La motivación y el alcance funcional están en `proposal.md`. UC-001 ya define el seguimiento como una consulta de solo lectura y ahora exige métricas seleccionables, gráficas individuales, una tabla compartida y fallos aislados. El catálogo y el contrato de mediciones existentes ya representan métricas extensibles, unidades de referencia y presión arterial como métrica compuesta.

El contrato legacy actual de `GET /api/v1/measurements` solo representa el
registro diario de peso y circunferencia abdominal. Para cumplir esta capability
sin romper ese contrato, la implementación añadirá dos lecturas específicas:
`GET /api/v1/measurements/catalog` para las métricas admitidas y
`GET /api/v1/measurements/tracking/{metricCode}` para los datos de una métrica.
El segundo endpoint se consulta por métrica para que un error pueda aislarse y
reintentarse sin repetir ni invalidar las demás lecturas.

## Goals / Non-Goals

**Goals:**

- Exponer un contrato read-only de catálogo y mediciones genéricas, conservando el contrato legacy de registro.
- Mantener la vista de seguimiento como una composición de datos de medición y estado de selección.
- Permitir que la vista derive sus opciones del catálogo y de las mediciones realmente disponibles.
- Compartir una misma selección entre gráficas y tabla para evitar que ambas representaciones diverjan.
- Representar métricas compuestas sin convertirlas en métricas independientes.
- Aislar los estados de carga, ausencia y error de cada métrica.
- Conservar la compatibilidad con peso y circunferencia abdominal como selección inicial.

**Non-Goals:**

- Cambiar `POST /api/v1/measurements`, la importación o el contrato conversacional.
- Cambiar el registro, edición, borrado o importación de mediciones.
- Cambiar el contrato conversacional de consulta o registro.
- Crear análisis clínico, recomendaciones, objetivos o comparaciones interpretativas.
- Añadir autenticación, preferencias persistentes o nuevas métricas al catálogo desde esta vista.

## Decisions

### Usar el catálogo y los datos como fuente de opciones

La pantalla solicitará primero el catálogo admitido y después leerá cada métrica
mediante el endpoint específico de tracking. Filtrará las métricas sin
mediciones antes de presentarlas. Esto permite incorporar métricas nuevas sin
modificar una lista fija de la vista y conserva un límite claro entre contrato,
servicio y presentación.

**Alternativa descartada:** codificar únicamente peso y circunferencia abdominal. Mantendría el comportamiento histórico, pero rompería el requisito de catálogo extensible.

### Separar lecturas por métrica

Cada lectura de tracking corresponderá a una métrica del catálogo. El servicio
de frontend modelará `loading`, `success`, `empty` y `error` por métrica, y el
endpoint de esa métrica será el objetivo de la acción de reintento.

**Alternativa descartada:** devolver todas las métricas en una única respuesta
obligatoria. Un fallo parcial convertiría una incidencia de una métrica en un
fallo global o exigiría un formato de errores más complejo que la vista no
necesita.

### Mantener una selección única para gráficas y tabla

La selección de métricas será el estado funcional común que alimente las gráficas individuales y las columnas de la tabla. La tabla seguirá siendo única y cambiará sus columnas junto con la selección.

**Alternativa descartada:** mantener una selección independiente por gráfica y tabla. Permitiría configuraciones divergentes y haría más difícil verificar qué información está viendo la persona.

### Representar la presión arterial como una métrica compuesta

La presión arterial conservará su identidad de métrica y se presentará en una sola gráfica con dos series, sistólica y diastólica. La tabla la tratará como una columna o agrupación de una única métrica con ambos valores.

**Alternativa descartada:** exponer sistólica y diastólica como métricas seleccionables separadas. Contradiría el contrato vigente del catálogo y podría hacer que una medición compuesta pareciera dos mediciones distintas.

### Aislar estados por métrica

La carga y el error se asociarán a cada métrica. Un fallo mostrará un mensaje y una acción de reintento para esa métrica, mientras las demás continúan visibles.

**Alternativa descartada:** fallar toda la pantalla ante un error parcial. Es más simple, pero oculta información válida y no cumple UC-001-E1.

### Mantener la vista de detalle como tabla accesible

Las gráficas no serán la única representación de los valores. La tabla contendrá fechas, unidades y valores de las métricas visibles, y servirá como alternativa accesible a la tendencia visual.

**Alternativa descartada:** depender exclusivamente de las gráficas. No satisface las expectativas de accesibilidad ni permite verificar con precisión todos los valores.

## Risks / Trade-offs

- **[Riesgo]** Métricas con estructuras distintas pueden requerir etiquetas y columnas específicas. **Mitigación:** usar la definición del catálogo y tratar explícitamente las métricas compuestas, empezando por presión arterial.
- **[Riesgo]** Una respuesta parcial puede mezclar datos válidos con estados de error. **Mitigación:** modelar el estado por métrica y probar ausencia, carga exitosa y fallo de forma independiente.
- **[Riesgo]** Añadir un contrato de lectura puede divergir del registro legacy. **Mitigación:** compartir el modelo de dominio/repositorio, mantener los endpoints existentes sin cambios y añadir pruebas de compatibilidad para registro e importación.
- **[Riesgo]** Muchas métricas seleccionadas pueden reducir la legibilidad. **Mitigación:** gráficas individuales, selección explícita y tabla limitada a las métricas visibles.
- **[Riesgo]** La selección por defecto puede incluir una métrica sin datos. **Mitigación:** aplicar el valor por defecto solo después de filtrar las métricas sin mediciones.

## Migration Plan

No se requiere migración de datos ni cambio de contrato de registro. Se añade el
contrato read-only antes de activar la nueva pantalla; la vista conserva la
presentación inicial de peso y circunferencia abdominal cuando existen datos.
El rollback consiste en restaurar la vista anterior y dejar sin uso los nuevos
endpoints, sin modificar las mediciones almacenadas.
