## Why

`uc-014` ya registra las mediciones al cerrar la consulta —con su métrica, su valor, su unidad de
referencia y su fecha exacta—, pero nada las lee desde la conversación: una pregunta por el peso, la
circunferencia abdominal, la presión arterial o el colesterol se atiende hoy con un
**redireccionamiento** a «su propio espacio» (`design.md` D10 de `uc-014`), no con los valores
registrados. El seguimiento solo se ve en `/measurements`, así que lo que la persona cuenta por
lenguaje natural no puede recuperarlo por lenguaje natural. `UC-014b` cierra ese hueco: es la lectura de
lo que `uc-014` escribe, y su objeto es justamente el contrato de esa consulta.

## What Changes

- El asistente **responde las preguntas sobre mediciones** con los valores y las fechas tal como
  quedaron registrados: por una métrica concreta, por varias a la vez o por un periodo, en la unidad de
  referencia de cada métrica y sin alterar ningún registro.
- La respuesta **conserva el valor y la fecha registrados** —no los redondea, no los completa y no los
  altera más allá del cambio de unidad— y **permite identificar las mediciones en que se apoya**.
- La **presión arterial se presenta como una sola medición** con sus dos valores, sistólica y
  diastólica, no como dos mediciones distintas.
- Cuando no consta ninguna medición de la métrica preguntada, el sistema **declara explícitamente que
  esa medición no consta**; la declaración se refiere a lo registrado y no afirma ni niega que la
  medición haya ocurrido.
- Cuando la persona **no concreta qué métrica pregunta**, el sistema **pide que la concrete** antes de
  responder y no supone cuál es; cuando pregunta por una **métrica que el sistema todavía no admite**,
  declara que no forma parte de su seguimiento y no inventa valores.
- La respuesta se da siempre en la **unidad de referencia de la métrica**, indicándola de forma
  explícita, con independencia de la unidad en la que la persona haya preguntado.
- **BREAKING**: el cauce de la historia clínica deja de redirigir las preguntas sobre mediciones. Una
  pregunta por peso, circunferencia abdominal, presión arterial o colesterol se atiende por el **cauce de
  las mediciones**, que responde con el seguimiento; la historia clínica MUST NOT presentar valores de
  medición como hechos suyos.
- **BREAKING**: el conjunto de operaciones ofrecido al proveedor y las operaciones del turno crecen con
  **consultar el seguimiento de mediciones**, junto a consultar la historia, recoger hechos y recoger
  mediciones. Ninguna operación nueva escribe: la consulta de mediciones es de solo lectura.
- El sistema **declina de forma explícita** una petición de interpretación o recomendación sobre los
  valores y ofrece los valores registrados, sin sustituirla por una interpretación inventada.
- Ante un fallo al recuperar las mediciones, el sistema **informa de un fallo recuperable** y ofrece
  reintentar, sin presentarlo como una ausencia de mediciones.

## Capabilities

### New Capabilities

- `clinical-measurement-query`: la consulta conversacional del seguimiento de mediciones —determinar
  qué métrica o métricas y qué periodo abarca la pregunta, recuperar las mediciones correspondientes,
  responder con sus valores, unidades y fechas tal como quedaron registrados, identificar las mediciones
  de apoyo, presentar la métrica compuesta como una sola medición, declarar explícitamente la ausencia,
  pedir que se concrete la métrica, declarar una métrica no admitida y declinar interpretaciones—.

### Modified Capabilities

- `clinical-history-query`: una pregunta sobre peso, circunferencia abdominal, presión arterial o
  colesterol deja de redirigirse a «su propio espacio» y se atiende por el cauce de las mediciones; la
  historia clínica sigue sin presentar valores de medición como hechos clínicos.
- `assistant-agent-turn`: el conjunto de operaciones del turno incorpora consultar el seguimiento de
  mediciones, además de consultar la historia clínica y recoger notas; la consulta de mediciones es de
  solo lectura y no escribe en el seguimiento.
- `llm-clinical-intent-adapter`: el conjunto de operaciones ofrecido al proveedor incorpora una
  operación de consulta del seguimiento de mediciones con su resultado estructurado —métrica, valores,
  unidad de referencia y fecha—, y la respuesta se compone únicamente con las mediciones recuperadas.
- `assistant-conversation`: el turno puede responder apoyándose en mediciones recuperadas, así que el
  resultado `answered` y la ausencia de mediciones se distinguen del cauce de la historia clínica y del
  resultado de una recogida.
- `assistant-chat-interface`: la interfaz muestra las mediciones que sustentan una respuesta y la
  ausencia de mediciones de forma distinguible de una respuesta fundada en hechos clínicos, de una
  recogida y de un error.

## Impact

- `backend/`: camino de lectura del seguimiento de mediciones —consulta por métrica, por varias métricas
  y por periodo—, servicio de consulta con su validación determinista, operación de turno y contrato
  estructurado de la consulta de mediciones, composición de la respuesta desde las mediciones
  recuperadas, y ajuste del cauce de la historia clínica para dejar de redirigir las preguntas de
  medición. Sin cambios de esquema ni migraciones: se lee el modelo de métrica que `uc-014` ya persiste.
- `frontend/`: la superficie del chat presenta la respuesta apoyada en mediciones y su ausencia de forma
  distinguible del cauce de la historia clínica.
- Contrato documentado en `backend/README.md`, con el contrato de consulta de mediciones del turno y su
  resultado; arquitectura en `docs/architecture.md`.
- Pruebas de backend (servicio de consulta, contrato del turno y del proveedor, ausencia y fallo), de
  frontend y de extremo a extremo sobre el chat.
- `docs/use-cases/UC-014b.md` y `UC-014b-acceptance-criteria.md` son la fuente funcional de este
  cambio; `docs/roadmap/use-cases.md` pasa `UC-014b` de documentado a implementado al cerrar el cambio.
- Fuera de alcance: el análisis de evolución dedicado y la comparación de valores (Fase 7.3), la
  conversión de la respuesta a la unidad en la que la persona pregunte, ampliar o gestionar el catálogo
  de métricas, la procedencia completa de una medición (`UC-023`), modificar o borrar mediciones, y la
  vista de seguimiento y sus gráficas (`UC-001` y su revisión, cambio hermano `metric-tracking-view`).
