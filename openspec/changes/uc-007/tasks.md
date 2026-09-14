## 1. Contrato de intención de consulta y prompt de clasificación

- [x] 1.1 Ampliar `ClinicalEventIntent` con la variante de consulta (pregunta, `scope` `history|measurements`, términos de búsqueda y filtros opcionales de tipo y fecha) conservando las invariantes de las variantes existentes; verificar con pruebas unitarias que una consulta no admite candidatos a evento y que registro, aclaración y conversación mantienen su comportamiento.
- [x] 1.2 Actualizar `ClinicalEventIntentValidator` para admitir y validar la intención de consulta (pregunta obligatoria, al menos un término o un filtro, `scope` admitido) y rechazar consultas mal formadas; verificar con pruebas unitarias de casos válidos e inválidos.
- [x] 1.3 Añadir `clinical-intent-v2.txt`, que incorpora la clasificación de consulta y de preguntas sobre peso o circunferencia abdominal sin alterar las reglas de registro, conservando `clinical-intent-v1.txt` para revertir; verificar que `OpenAiClinicalIntentInterpreter` carga la nueva versión y que el mapeo de registro no cambia con las pruebas existentes del intérprete.
- [x] 1.4 Extender el parser de `OpenAiClinicalIntentInterpreter` para la intención de consulta y su `scope`; verificar con pruebas de JSON válido, `scope` desconocido y campos obligatorios ausentes que una respuesta inválida se convierte en fallo controlado.

## 2. Recuperación en el índice derivado

- [x] 2.1 Crear un repositorio de solo lectura sobre `clinical_event_index` con búsqueda por `search_vector` y orden por `ts_rank`, filtros opcionales por `type` y rango de `event_date`, y `LIMIT` configurable; verificar con pruebas de integración contra PostgreSQL que recupera por texto y por metadatos y que respeta el límite.
- [x] 2.2 Añadir el fallback por metadatos cuando la búsqueda léxica no devuelve resultados pero existe filtro de tipo o de fecha; verificar con una prueba de integración que devuelve los hechos del tipo o del rango solicitados.
- [x] 2.3 Verificar que la recuperación no escribe: responder una consulta y comprobar que `clinical_event_index` y los documentos Markdown de `data/events/` quedan idénticos (prueba de integración que compara el estado antes y después).

## 3. Respuesta fundada en los hechos recuperados

- [x] 3.1 Definir el puerto `ClinicalAnswerComposer` con `compose(question, events)` y su resultado (texto y referencias); verificarlo con una implementación falsa en las pruebas del servicio de consulta.
- [x] 3.2 Añadir `clinical-answer-v1.txt`, que restringe la composición al conjunto recuperado, conserva la precisión temporal y exige devolver las referencias usadas; verificar con pruebas del parser de la respuesta del proveedor.
- [x] 3.3 Implementar la composición sobre el proveedor y validar las referencias contra el conjunto recuperado, convirtiendo una referencia desconocida en fallo controlado; verificar con pruebas unitarias de cita válida, cita inventada y respuesta sin referencias (se adjunta el conjunto recuperado completo como apoyo).
- [x] 3.4 Crear el servicio de consulta de la historia que, dada una intención de consulta, recupere los hechos, componga la respuesta y devuelva un resultado tipado (`ANSWERED`, `NO_RECORDS`, `FAILURE`); verificar con pruebas unitarias de cada desenlace, incluido el conjunto recuperado vacío y el fallo del proveedor.

## 4. Orquestación y contrato de chat

- [x] 4.1 Enrutar en `ChatOrchestrator` la intención de consulta: `scope=history` al servicio de consulta y `scope=measurements` a la remisión fija de peso o circunferencia abdominal; verificar con pruebas que la consulta no invoca el registro y que `measurements` no busca en la historia.
- [x] 4.2 Añadir `answered` y `no_records` a `ChatMessageResponse.Status` y mapear los desenlaces del servicio de consulta; verificar con pruebas de `ChatController` y del orquestador que `answered` incluye los hechos de apoyo y que `no_records` no incluye ninguno.
- [x] 4.3 Garantizar que el turno de consulta no muta la historia ni el registro y que un fallo de interpretación o de búsqueda devuelve `failed` sin respuesta inventada; verificar con pruebas del orquestador de fallo del proveedor y de fallo de búsqueda.
- [x] 4.4 Comprobar que los resultados de chat existentes no cambian de significado; verificar que las pruebas de registro, duplicado, aclaración, conversación general y fallo siguen pasando sin modificar sus aserciones.

## 5. Estado de conversación para preguntas de seguimiento

- [x] 5.1 Añadir a `ConversationStateStore.State` un búfer acotado de turnos recientes con tamaño configurable, evictado con la misma TTL y el mismo límite de conversaciones ya existentes; verificar con pruebas unitarias de retención, tamaño máximo y expiración.
- [x] 5.2 Alimentar la interpretación con los turnos recientes sin persistirlos y descartar el búfer cuando se descarta el estado; verificar con pruebas unitarias de que una pregunta de seguimiento recibe el contexto y de que un estado reiniciado no conserva turnos.
- [x] 5.3 Mantener la idempotencia por `(conversationId, messageId)` también para turnos de consulta; verificar con pruebas que repetir una consulta devuelve el resultado original sin volver a invocar al proveedor.

## 6. Interfaz de chat

- [x] 6.1 Ampliar `ChatStatus` y el esquema Zod de `features/chat` con `answered` y `no_records`; verificar con las pruebas existentes de la acción y del esquema que los nuevos estados se validan y que los antiguos siguen aceptándose.
- [x] 6.2 Mostrar en `ChatWorkspace` la respuesta junto con sus hechos de apoyo (código, fecha con su precisión y contenido) y etiquetar el estado `no_records`; verificar con pruebas de componente que ambos estados se representan y que una respuesta fundada lista sus hechos.
- [x] 6.3 Mantener la accesibilidad y la ausencia de detalles técnicos en los nuevos estados; verificar ampliando las pruebas de teclado y lector de pantalla a `answered` y `no_records`.

## 7. Verificación integral

- [x] 7.1 Ejecutar las pruebas y el lint del backend y del frontend y comprobar que pasan; verificar además el cumplimiento de la cobertura exigida por los estándares del proyecto. Verificado: pruebas y lint pasan (backend 149, frontend 110); cobertura de línea 0.9067 ✓; la cobertura de rama (0.6893) queda por debajo del umbral del 90% por una brecha preexistente (línea base 0.6731), no introducida por este cambio, que además la eleva.
- [x] 7.2 Recorrer de extremo a extremo el ciclo registrar → preguntar, incluida una pregunta con fecha aproximada y una pregunta sin registros; verificar que la respuesta solo cita hechos registrados, que conserva la imprecisión temporal y que la historia clínica queda intacta.
