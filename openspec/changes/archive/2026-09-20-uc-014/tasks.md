## 1. Modelo de métrica y migración

- [x] 1.1 Modelar la medición con dimensión de métrica —métrica, valores, fecha y procedencia—, con soporte de métrica simple y compuesta, y verificar con pruebas unitarias que una presión arterial se representa como una sola medición con sus dos valores (`clinical-measurement-registration`, «Registrar al cerrar la consulta las mediciones mencionadas»; `clinical-metric-catalog`, «Tratar una métrica compuesta como una sola medición»).
- [x] 1.2 Añadir la migración Flyway de `measurements`, `measurement_values` y `admitted_metrics`, con la unicidad por métrica y día y la siembra de las métricas admitidas, y verificar con Testcontainers que el arranque aplica las migraciones sobre una base vacía.
- [x] 1.3 Migrar las filas de la tabla antigua a una medición por `(weight, día)` y otra por `(waist, día)` y retirar la tabla antigua, y verificar con una prueba de integración que el seguimiento conserva los valores previos de peso y circunferencia de cada día.
- [x] 1.4 Persistir el conjunto admitido y el alta de una métrica confirmada por la persona, y verificar con una prueba que admitir una métrica no altera el registro ni la lectura de las métricas que ya existían (`clinical-metric-catalog`, «Ampliar el catálogo solo con la confirmación de la persona»).

## 2. Catálogo de métricas y unidades

- [x] 2.1 Implementar el registro de métricas con unidad de referencia, componentes obligatorios y rango admisible para peso, circunferencia abdominal, presión arterial y colesterol, y verificar con pruebas unitarias que cada métrica se guarda en su unidad de referencia (`clinical-metric-catalog`, «Guardar toda medición en la unidad de referencia de su métrica»).
- [x] 2.2 Implementar la conversión determinista entre las unidades de una misma métrica, y verificar con pruebas unitarias que la equivalencia es única y que el valor guardado no se altera más allá del cambio de unidad (`clinical-metric-catalog`, «Convertir un valor expresado en otra unidad de la misma métrica»).
- [x] 2.3 Implementar la resolución de la unidad —deducción declarada por el catálogo y petición de aclaración cuando no es inequívoca—, y verificar con pruebas unitarias que no se supone ninguna unidad y que no se pide aclaración cuando es inequívoca (`clinical-metric-catalog`, «Resolver la unidad de una medición solo cuando es inequívoca»).
- [x] 2.4 Rechazar un valor fuera del rango de su métrica, un valor compuesto incompleto y una métrica no admitida sin sustituirla por otra, y verificar con pruebas unitarias que un tipo de hecho no admitido no se registra (`clinical-metric-catalog`, «Admitir un catálogo extensible de métricas»; `clinical-event-registration`, «Registrar hechos clínicos propios»).

## 3. Recogida de la medición en la consulta

- [x] 3.1 Extender la nota de la consulta con la variante de medición y persistirla en el front matter de `enc_NNN.md`, y verificar con una prueba de ida y vuelta que la nota conserva métrica, valores, unidad declarada y fecha con su precisión (`clinical-consultation`, «Recoger y registrar las mediciones de la consulta»).
- [x] 3.2 Añadir al conjunto de operaciones del proveedor la recogida de una medición y al contrato estructurado el candidato de medición, y verificar con pruebas unitarias que una operación fuera del conjunto no se ejecuta y que el proveedor no alcanza la persistencia (`llm-clinical-intent-adapter`, «Produce provider-neutral clinical intents»).
- [x] 3.3 Enrutar por el cauce de las mediciones un mensaje que contiene una y no ofrecerla como candidato de hecho clínico, y verificar con una prueba que la medición no se convierte en un evento de la historia clínica (`clinical-measurement-registration`, «Atender por el cauce de las mediciones un mensaje que contiene una»).
- [x] 3.4 Verificar con una prueba de contrato que el seguimiento y la historia clínica no cambian durante un turno que recoge una medición (`assistant-agent-turn`, «No incorporar una medición durante el turno»).

## 4. Cierre de la consulta

- [x] 4.1 Registrar el lote de mediciones de la consulta en una sola transacción, con reemplazo por métrica y día, y verificar con pruebas unitarias y de integración que no se crea una segunda medición y que la confirmación informa de que quedó actualizado (`clinical-measurement-registration`, «Mantener una sola medición por métrica y día»).
- [x] 4.2 Exigir fecha exacta al registrar: no convertir una fecha aproximada o desconocida, pedir la fecha exacta y usar el día en que se atiende el mensaje cuando no se indicó ninguna, pidiendo confirmación cuando no pueda darse por seguro, y verificar con pruebas unitarias que no se registra una fecha que la persona no dio (`clinical-measurement-registration`, «Exigir una fecha exacta a toda medición»).
- [x] 4.3 Depurar las mediciones repetidas de la consulta antes de registrar y conservar la información más completa, verificado con una prueba unitaria que dos menciones de la misma medición producen un solo registro (`clinical-measurement-registration`, «Registrar una sola vez una medición mencionada varias veces»).
- [x] 4.4 Declarar la procedencia de cada medición registrada con la consulta de origen, y verificar con una prueba que la procedencia se fija al registrar, no se modifica después y no se expone en la confirmación (`clinical-measurement-registration`, «Declarar la procedencia de toda medición registrada»).
- [x] 4.5 Hacer de todo o nada el lote de mediciones y reintentable el cierre, y verificar con pruebas unitarias que un fallo deja el seguimiento exactamente como estaba, informa de lo registrado y de lo no registrado, no revierte una medición ya registrada y no comunica el fallo como una ausencia (`clinical-measurement-registration`, «Comprobar antes de escribir y no dejar registros a medias»).
- [x] 4.6 Confirmar el cierre combinando las mediciones y los hechos registrados, y verificar con pruebas de contrato que la confirmación distingue lo registrado de lo no registrado y no expone detalles internos (`clinical-measurement-registration`, «Confirmar brevemente lo registrado»).

## 5. Retirada de `measurement` del catálogo de hechos clínicos

- [x] 5.1 Retirar `MEASUREMENT` del catálogo de tipos admitidos y de la validación determinista del registro, y verificar con pruebas unitarias que un hecho con ese tipo se rechaza sin escribir (`clinical-event-registration`, «Registrar hechos clínicos propios»).
- [x] 5.2 Dejar de responder por valores de medición en la consulta de la historia, y verificar con una prueba que una pregunta sobre peso o circunferencia se atiende indicando que ese seguimiento tiene su propio espacio y sin presentar valores como hechos clínicos (`clinical-history-query`, «Redactar la respuesta solo con los hechos recuperados»).
- [x] 5.3 Verificar con una prueba que la reconstrucción del índice derivado tolera un documento legado con tipo `measurement` sin reescribirlo (`clinical-event-registration`, «Registrar hechos clínicos propios»).

## 6. Compatibilidad con las vías de registro existentes

- [x] 6.1 Convertir `/api/v1/measurements` en una vista derivada del modelo conservando el contrato —`GET` compone peso y circunferencia de los días que tienen ambas métricas y `POST` hace *upsert* de esas dos métricas de ese día— y verificar con MockMvc que el contrato no cambia para el cliente (`clinical-metric-catalog`, «Guardar toda medición en la unidad de referencia de su métrica»).
- [x] 6.2 Conservar en la vista legada la validación del contrato anterior y verificar con pruebas de contrato que siguen rechazándose la fecha futura, el valor no positivo, el exceso de decimales y los topes de 500 kg y 400 cm.
- [x] 6.3 Verificar con la carga desde archivo (`UC-003`) que la importación sigue validando el archivo completo antes de escribir y que las mediciones importadas quedan en el modelo de métrica, una por métrica y día.
- [x] 6.4 Verificar con una prueba de integración que una medición de una sola métrica queda íntegra en el modelo y que la vista legada la omite sin perderla, y dejar constancia del hueco acotado de `design.md` D8 en `backend/README.md`.

## 7. Documentación y verificación

- [x] 7.1 Actualizar `backend/README.md` con el contrato de mediciones y el cierre extendido, y verificar que documenta el catálogo de métricas, la conversión de unidades y el reemplazo por métrica y día (`backend/README.md` es la fuente de verdad del contrato).
- [x] 7.2 Actualizar `docs/data-model.md` —§6.2 pasa de objetivo de Fase 4.3 a modelo implementado y la tabla antigua sale de §1.1— y `docs/architecture.md`, y verificar que ambos coinciden con las specs del cambio.
- [x] 7.3 Reconciliar `docs/use-cases/UC-014.md`, `UC-004.md` y `docs/use-cases/reglas-de-negocio.md` (RN-019 matizado por la regla de una medición por métrica y día) con el comportamiento implementado, y verificar que no queda ninguna nota de revisión pendiente sin resolver.
- [x] 7.4 Actualizar los recorridos de Playwright del chat para la medición registrada al cerrar de la consulta y verificar que pasan con el stack real en ejecución.
- [x] 7.5 Ejecutar `.\mvnw.cmd verify` en `backend/` y `pnpm lint`, `pnpm typecheck` y `pnpm test` en `frontend/`, y verificar que el gate de cobertura JaCoCo y todas las pruebas pasan.
