## Context

El backend ya registra hechos clínicos como documentos Markdown en `data/events/`
y mantiene un índice derivado en PostgreSQL (`clinical_event_index`) con un
`search_vector tsvector` generado e indexado con GIN. Ese índice se escribe desde
`ClinicalEventIndexWriter` y se reconstruye desde `ClinicalEventIndexRebuilder`,
que es la única operación que hoy lo consume. Ver `proposal.md` para la motivación.

El chat ya está encauzado: `ChatController` → `ChatOrchestrator` →
`ClinicalIntentInterpreter` (implementación OpenAI) → intención
`EVENTS | CLARIFICATION | CONVERSATION` → registro o respuesta fija. El estado
efímero vive en `ConversationStateStore` (Map en memoria con TTL y límite de
conversaciones) y hoy solo guarda la aclaración pendiente y los resultados por
`messageId` para idempotencia.

Restricciones que condicionan el diseño:

- El backend es el dueño de la orquestación; el proveedor nunca es fuente de
  verdad ni escribe en la persistencia.
- El índice es derivado: la consulta no puede introducir hechos ni depender del
  estado del índice como fuente de verdad.
- `docs/roadmap/use-cases.md` define UC-005 y UC-006 como capacidades internas de
  recuperación sin superficie propia; esta es su implementación.
- UC-007 excluye explícitamente la búsqueda semántica (Fase 6 del roadmap).

## Goals / Non-Goals

**Goals:**

- Recuperar hechos del índice existente por texto y metadatos (tipo y fecha), de
  forma determinista y sin salir de los hechos registrados.
- Componer una respuesta en español restringida al conjunto recuperado y devolver
  con ella las referencias verificables y la precisión temporal registrada.
- Enrutar la turno de chat entre registro, consulta, aclaración, conversación
  general y remisión de peso/circunferencia.
- Soportar preguntas de seguimiento con un búfer acotado de turnos recientes,
  sin persistir historial.

**Non-Goals:**

- Búsqueda semántica o vectorial, ranking aprendido o re-ranking.
- Endpoint público de búsqueda o de inspección de eventos (UC-011).
- Exponer el índice como fuente de verdad o escribirlo desde la consulta.
- Responder preguntas generales con conocimiento del modelo (UC-010) o detallar
  la gestión de información ausente (UC-008).
- Persistir historial de conversación entre sesiones o dispositivos.

## Decisions

### Recuperación léxica sobre el índice existente, con filtros de metadatos

Se consulta `clinical_event_index` con `JdbcTemplate` en un componente de lectura
nuevo (`ClinicalEventQueryRepository`) simétrico a `ClinicalEventIndexWriter`:
`search_vector @@ <tsquery>` con `ts_rank` y desempate por `event_date DESC NULLS
LAST`, más filtros opcionales por `type` y rango de `event_date`, con `LIMIT`
acotado por configuración (`careme.chat.query.max-results`). La consulta es
estrictamente de lectura.

El `tsquery` se construye con los términos que el propio modelo extrae de la
pregunta, no con la pregunta cruda: el `search_vector` usa la configuración
`simple` (sin stemming ni stopwords en español), así que los términos del modelo
en las palabras del usuario son lo que mejor coincide con el contenido indexado.

Alternativas descartadas:
- **Buscar con la pregunta completa** (`plainto_tsquery` sobre el mensaje): con
  `simple` produce coincidencias de ruido por palabras funcionales y no explota
  la intención ya clasificada.
- **Embeddings + `pgvector`** (Fase 6): resolvería la variación terminológica,
  pero UC-007 excluye la recuperación por significado y añade dependencia e
  infraestructura que el MVP no necesita.
- **Listar todos los eventos y filtrar en memoria**: evita SQL específico, pero
  escala mal y desperdicia el índice GIN ya existente.

Si la búsqueda léxica no devuelve resultados pero el modelo aportó un filtro de
tipo o de fecha, se ejecuta una segunda consulta solo por metadatos antes de
declarar la ausencia.

### El backend decide el enrutamiento; el adaptador solo propone

El adaptador amplía su contrato con una intención de consulta que lleva la
pregunta y los criterios de búsqueda (términos, tipo y rango de fechas), y
mantiene `EVENTS`, `CLARIFICATION` y `CONVERSATION` sin cambios de significado. La
consulta lleva además un `scope`:

| `scope` | Desenlace |
| --- | --- |
| `history` | recuperar en el índice y componer respuesta fundada |
| `measurements` | remitir a su seguimiento propio sin buscar (A6) |

La distinción se apoya en el modelo porque requiere interpretar lenguaje natural,
pero el backend es quien recupera, valida y decide el resultado observable.

Alternativa descartada: **que el modelo decida el resultado y redacte la
respuesta a la vez**. Mezcla razonamiento y hechos sin que el backend pueda
verificar la fundamentación, y contradice la regla de negocio de buscar primero y
redactar después.

### Dos operaciones del proveedor: clasificar y componer

La respuesta debe construirse después de conocer los hechos, así que el turno de
consulta hace dos llamadas con prompts distintos:

1. **Clasificación** (`clinical-intent-v2`): la pregunta y los criterios de
   búsqueda, o el registro, o la aclaración.
2. **Composición** (`clinical-answer-v1`): la pregunta más el conjunto de hechos
   recuperados; devuelve el texto en español y las referencias usadas.

La composición se expone como un puerto separado
(`ClinicalAnswerComposer.compose(question, events)`) para que no dependa del
intérprete y sea sustituible en pruebas. El prompt de composición recibe los
hechos con su contenido, su fecha y su `date_precision`, y prohíbe introducir
fechas, hechos o interpretaciones que no estén en ese conjunto.

Alternativas descartadas:
- **Una sola llamada**: imposible, el modelo no conoce los hechos antes de que el
  backend los recupere.
- **Inyectar toda la historia en el prompt**: elimina la recuperación, escala mal
  y multiplica el riesgo de afirmaciones sin respaldo.
- **Redactar la respuesta de forma determinista (plantillas)**: abarata el turno,
  pero no cubre preguntas que reúnen varios hechos ni el lenguaje del usuario.

### La fundamentación se valida en el backend

Las referencias que devuelve la composición se contrastan contra el conjunto
recuperado. Una referencia fuera de ese conjunto es un fallo controlado: el turno
termina en `failed` y no se presenta la respuesta como fundada. Si la composición
no devuelve referencias, se adjunta el conjunto recuperado completo como apoyo,
porque la respuesta se compuso exclusivamente con él y sigue siendo verificable.

### Nuevos resultados de chat que reutilizan `events` para las referencias

Se añaden `answered` y `no_records` a `ChatMessageResponse.Status` (valores
`answered` y `no_records`). Las referencias viajan en el campo `events` ya
existente: `EventSummary` ya expone `code`, `type`, `date`, `datePrecision` y
`content`, que es exactamente lo que la persona necesita para verificar la
respuesta. No se añade un campo nuevo al contrato.

`no_records` se separa de `failed` porque la ausencia de registros es un
resultado correcto, no un error: el usuario debe poder distinguir "no hay nada
registrado" de "no pude buscar". La remisión de peso/circunferencia (A6) reutiliza
`general_conversation` con un mensaje fijo en español, porque no hay registro, ni
búsqueda, ni respuesta fundada, y no merece un estado propio.

Alternativa descartada: **un campo `sources` separado**. Duplica la forma de
`EventSummary` y obliga a versionar el contrato sin aportar información nueva.

### Búfer acotado de turnos recientes para el seguimiento

`ConversationStateStore.State` incorpora una cola de tamaño fijo con los últimos
turnos (mensaje del usuario y resumen de la respuesta) que se evicta con la misma
TTL y el mismo límite de conversaciones que ya existen. El búfer solo alimenta la
interpretación de la pregunta de seguimiento; la respuesta se sigue construyendo
desde la historia. No se persiste nada.

Alternativa descartada: **guardar el historial completo en PostgreSQL**. Cumpliría
A4 con holgura, pero contradice la regla de no conservar el historial entre
sesiones y añade un modelo de datos que UC-007 no pide.

### El índice y los prompts se versionan; los estados son aditivos

La clasificación usa `clinical-intent-v2` y la composición `clinical-answer-v1`;
se conserva `clinical-intent-v1` para poder revertir. La ampliación del enum de
estados es aditiva en la API, pero el frontend valida con un `enum` cerrado, así
que backend y frontend se despliegan juntos (ver Migration Plan).

## Risks / Trade-offs

- **[La configuración `simple` no lematiza español]** → mitigación: los términos de
  búsqueda los propone el modelo en las palabras del usuario y se acepta el
  subconjunto que realmente aparece indexado; la coincidencia por significado
  queda para la Fase 6. Se acepta menor recall a cambio de no añadir dependencias.
- **[Dos llamadas al proveedor por turno de consulta]** → mitigación: el conjunto
  recuperado se limita por configuración, la composición es la única llamada
  adicional y su latencia se mide con la telemetría existente (sin registrar el
  contenido clínico completo).
- **[El modelo puede aumentar la precisión temporal al redactar]** → mitigación: la
  composición recibe `date_precision` y la prohíbe explícitamente; se cubre con
  pruebas de fecha aproximada y desconocida, y el escenario de conservación de
  imprecisión lo verifica.
- **[Una referencia inventada podría colarse si no se valida bien]** → mitigación:
  toda cita se contrasta contra el conjunto recuperado antes de responder y una
  cita desconocida convierte el turno en `failed`.
- **[Deduplicación y búfer compiten por el mismo estado en memoria]** →
  mitigación: el búfer se actualiza dentro del `State` sincronizado que ya
  protege aclaración y resultados idempotentes.
- **[La ampliación del enum rompe clientes que validan con `enum` cerrado]** →
  mitigación: se amplía `ChatStatus` y el esquema Zod en el mismo despliegue y se
  añaden etiquetas para los nuevos estados; el valor desconocido ya no puede
  llegar desde un backend nuevo a un frontend viejo.
- **[El turno de consulta no debe tocar el índice]** → mitigación: el repositorio de
  consulta expone solo lectura y las pruebas verifican que la historia y el índice
  quedan intactos después de responder.

## Migration Plan

1. Publicar el prompt `clinical-intent-v2` y `clinical-answer-v1` conservando
   `clinical-intent-v1` para revertir.
2. Ampliar el contrato de intención (`QUERY` con `scope` y criterios de búsqueda)
   y su validación; mantener los casos de registro sin cambios y cubrirlos con las
   pruebas existentes.
3. Añadir el repositorio de lectura del índice, el servicio de consulta de la
   historia y el puerto de composición fundada.
4. Enrutar en `ChatOrchestrator` los nuevos desenlaces y añadir `answered` y
   `no_records` a `ChatMessageResponse.Status`.
5. Añadir el búfer acotado de turnos recientes a `ConversationStateStore`.
6. Ampliar `ChatStatus`, el esquema Zod de `features/chat` y la representación de
   la respuesta con sus hechos de apoyo en `ChatWorkspace`.
7. Desplegar backend y frontend juntos, porque el frontend valida los estados con
   un `enum` cerrado.

Rollback: revertir el prompt a `clinical-intent-v1` y volver a enrutar el turno
de consulta como conversación general; los estados nuevos dejan de emitirse y no
requieren migración de datos. No hay migración de esquema: se reutiliza
`clinical_event_index`, que sigue siendo reconstruible desde Markdown.

## Open Questions

- La normalización exacta de los términos de búsqueda (acentos, palabras
  funcionales) con la configuración `simple` puede ajustarse tras observar
  preguntas reales, sin cambiar las especificaciones ni el enfoque.
- El número máximo de hechos recuperados por pregunta y el tamaño del búfer de
  turnos recientes son parámetros de configuración; sus valores definitivos se
  fijan al medir con datos reales.
