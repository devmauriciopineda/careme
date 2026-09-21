## 1. Operación de consulta y servicio de lectura

- [x] 1.1 Añadir la operación de consultar el seguimiento de mediciones al conjunto de operaciones del turno, con sus argumentos —la pregunta, la métrica o métricas, el periodo y la señal de que no se concretó métrica—, y verificar con una prueba unitaria que se declara al proveedor junto a las existentes y que no abre ninguna ruta de escritura (`llm-clinical-intent-adapter`, «Produce provider-neutral clinical intents»; `assistant-agent-turn`, «Acotar las operaciones disponibles»).
- [x] 1.2 Añadir al repositorio de mediciones la lectura por métricas acotada por rango de fechas, y verificar con una prueba de integración que devuelve solo las mediciones de esas métricas dentro del periodo (`clinical-measurement-query`, «Recuperar las mediciones sin alterar el seguimiento»).
- [x] 1.3 Implementar el servicio de consulta de mediciones de solo lectura con su resultado explícito —respondida, ausencia o fallo—, y verificar con pruebas unitarias que no escribe en el seguimiento y que distingue la ausencia del fallo (`clinical-measurement-query`, «Informar de un fallo al recuperar sin presentarlo como ausencia»).
- [x] 1.4 Verificar con una prueba de integración que una consulta de mediciones deja el seguimiento exactamente como estaba (`clinical-measurement-query`, «No alterar ningún registro al consultar»; `assistant-agent-turn`, «No escribir en el seguimiento al consultar mediciones»).

## 2. Resolución de la métrica, el periodo y los casos límite

- [x] 2.1 Resolver la métrica o métricas y el periodo de la pregunta contra el catálogo admitido, y verificar con pruebas unitarias que una métrica compuesta se resuelve como una sola medición y que el periodo acota la recuperación (`clinical-measurement-query`, «Determinar la métrica y el periodo de la pregunta»; «Presentar una métrica compuesta como una sola medición»).
- [x] 2.2 Pedir que se concrete la métrica cuando la pregunta no la concreta, sin suponer ninguna, y verificar con una prueba unitaria que no se responde con una suposición (`clinical-measurement-query`, «Pedir que se concrete la métrica preguntada»).
- [x] 2.3 Declarar que una métrica no forma parte del seguimiento cuando no está en el catálogo o no está admitida, sin inventar valores ni sustituirla por otra, y verificar con pruebas unitarias que ambos casos se declaran igual (`clinical-measurement-query`, «Declarar una métrica que no forma parte del seguimiento»).
- [x] 2.4 Declarar explícitamente la ausencia cuando no consta ninguna medición de la métrica preguntada, con un texto que no afirme ni niegue que la medición ocurriera, y verificar con una prueba unitaria el texto y el motivo de ausencia (`clinical-measurement-query`, «Declarar explícitamente la ausencia de mediciones»).
- [x] 2.5 Declinar una interpretación o recomendación sobre los valores ofreciendo los registrados, y verificar con una prueba unitaria que la declinación no se sustituye por una interpretación inventada (`clinical-measurement-query`, «Declinar interpretaciones y recomendaciones sobre las mediciones»).

## 3. Composición de la respuesta y contrato del turno

- [x] 3.1 Formatear de forma determinista los hechos de medición —etiqueta de la métrica, valores con su unidad de referencia, fecha exacta y referencia identificable—, y verificar con pruebas unitarias que el valor, la unidad y la fecha presentados son los almacenados, sin redondeo ni conversión (`clinical-measurement-query`, «Responder con los valores, las unidades y las fechas registrados»; «Permitir identificar las mediciones en que se apoya la respuesta»).
- [x] 3.2 Componer en español la respuesta a partir de esos hechos, con su implementación real y su implementación `fake`, y verificar con pruebas unitarias que el compositor no altera los valores formateados (`llm-clinical-intent-adapter`, «Compose grounded answers from retrieved measurements»).
- [x] 3.3 Incorporar a la respuesta de chat las mediciones de apoyo y un motivo de ausencia propio del seguimiento, y verificar con MockMvc que el contrato es aditivo y que una ausencia de mediciones se distingue de una ausencia de hechos clínicos (`assistant-conversation`, «Report the outcome of the turn and the operations it went through»).
- [x] 3.4 Verificar con una prueba de contrato que un fallo al recuperar las mediciones se devuelve como fallo reintentable, no como ausencia, y que el turno no se presenta como completo (`assistant-conversation`, «Keep a measurement retrieval failure out of the absence outcome»).

## 4. Cauce de la historia clínica

- [x] 4.1 Sustituir el redireccionamiento del ámbito de mediciones de la consulta de la historia por la delegación en la consulta de mediciones, y verificar con una prueba unitaria que una pregunta por peso, circunferencia abdominal, presión arterial o colesterol no se responde con hechos clínicos (`clinical-history-query`, «Reconocer las preguntas sobre la propia historia clínica»; «Redactar la respuesta solo con los hechos recuperados»).
- [x] 4.2 Verificar con una prueba que la consulta de la historia sigue respondiendo los hechos clínicos como antes y que no devuelve ningún valor de medición (`clinical-history-query`, «Redactar la respuesta solo con los hechos recuperados»; `assistant-agent-turn`, «Decidir y ejecutar las operaciones del mensaje»).

## 5. Interfaz de chat

- [x] 5.1 Extender el esquema y los tipos del chat con las mediciones de apoyo y el motivo de ausencia propio, y verificar con `pnpm typecheck` que los tipos son válidos (`assistant-chat-interface`, «Show the measurements that support an answer»).
- [x] 5.2 Presentar en la superficie del chat la respuesta de mediciones con sus valores, unidades y fechas, distinguiéndola de una respuesta fundada en la historia clínica y de una ausencia de hechos, y verificar con pruebas de componente que una métrica compuesta se muestra como una sola medición (`assistant-chat-interface`, «Show the measurements that support an answer»; «Represent clarification, success, duplicate, and failure states»).
- [x] 5.3 Verificar con un recorrido de Playwright sobre el chat que una pregunta por una métrica registrada muestra sus valores y fechas y que una métrica sin mediciones declara la ausencia (`assistant-chat-interface`, «Show an absence of measurements»).

## 6. Documentación y verificación

- [x] 6.1 Actualizar `backend/README.md` con el contrato de la consulta de mediciones del turno y su resultado aditivo, y verificar que documenta la unidad de referencia, el periodo y la distinción entre ausencia y fallo (`backend/README.md` es la fuente de verdad del contrato).
- [x] 6.2 Actualizar `docs/architecture.md` y `docs/roadmap/use-cases.md` —`UC-014b` pasa de documentado a implementado—, y verificar que ambos coinciden con las specs del cambio.
- [x] 6.3 Ejecutar `.\mvnw.cmd verify` en `backend/` y `pnpm lint`, `pnpm typecheck` y `pnpm test` en `frontend/`, y verificar que el gate de cobertura JaCoCo y todas las pruebas pasan.
