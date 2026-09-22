## 0. Contrato de lectura del seguimiento

- [x] 0.1 Añadir el endpoint read-only del catálogo admitido con código, etiqueta, unidad de referencia y componentes; verificar que incluye las métricas iniciales, presión arterial y una métrica futura sin cambiar el registro ni la conversación.
- [x] 0.2 Añadir el endpoint read-only de mediciones por métrica, con fechas ordenadas, valores por componente y unidad de referencia; verificar presión arterial como una única medición compuesta.
- [x] 0.3 Añadir pruebas de contrato e integración para fallo aislado por métrica y compatibilidad de `POST /api/v1/measurements`, importación y consulta conversacional.

## 1. Modelo de datos de la vista

- [x] 1.1 Revisar el contrato de mediciones y construir el modelo de presentación por métrica, incluyendo unidad, fechas, valores y estructura compuesta de presión arterial; verificar con pruebas unitarias que las métricas iniciales, una métrica nueva y presión arterial conservan sus datos.
- [x] 1.2 Derivar la lista de métricas disponibles filtrando las que no tienen mediciones y aplicar la selección por defecto de peso y circunferencia abdominal solo cuando existan datos; verificar los casos de catálogo vacío y métrica sin datos.
- [x] 1.3 Representar estados independientes de carga, éxito, ausencia y error para cada métrica, con reintento aislado; verificar que un fallo no elimina las métricas cargadas correctamente.

## 2. Selector y visualización

- [x] 2.1 Actualizar la pantalla de seguimiento para mostrar un selector accesible de métricas disponibles y sincronizarlo con un único estado de selección; verificar que seleccionar u ocultar una métrica actualiza todas las representaciones.
- [x] 2.2 Actualizar las gráficas para mostrar una gráfica individual por métrica seleccionada, su unidad de referencia y dos series para presión arterial; verificar la representación con pruebas de componente.
- [x] 2.3 Actualizar la tabla de detalle para mantener una sola tabla con las columnas de las métricas seleccionadas, fechas ordenadas y valores de presión arterial como métrica compuesta; verificar que no aparecen columnas de métricas ocultas.
- [x] 2.4 Mantener sincronizadas las vistas de tendencia y detalle por fecha con la selección vigente; verificar el cambio entre vistas sin perder selección ni valores.

## 3. Estados de usuario y accesibilidad

- [x] 3.1 Añadir los estados vacíos para seguimiento sin mediciones y para métricas individuales sin datos, sin ofrecer opciones que no tengan mediciones; verificar los escenarios correspondientes de la delta spec.
- [x] 3.2 Añadir mensajes en español y acción de reintento para el fallo de una métrica, conservando las demás disponibles; verificar el escenario de fallo parcial.
- [x] 3.3 Verificar que la información de las gráficas también está disponible mediante la tabla y que el selector y sus controles se pueden usar con teclado; ejecutar las pruebas de accesibilidad disponibles.

## 4. Documentación y verificación integrada

- [x] 4.1 Mantener UC-001 y sus criterios de aceptación alineados con la capability de la vista, incluyendo reglas, flujos y errores; verificar la correspondencia de `A1–A4`, `E1` y `UC-001-R1…R6`.
- [x] 4.2 Añadir o actualizar pruebas de integración de la página para métricas iniciales, métrica nueva, presión arterial, métrica sin datos y fallo aislado; ejecutar la suite frontend enfocada en mediciones.
- [x] 4.3 Ejecutar typecheck, lint y pruebas frontend relevantes; verificar que el build de frontend termina correctamente y que no se modifican los contratos de registro ni consulta conversacional.
