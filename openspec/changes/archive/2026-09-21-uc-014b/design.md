## Context

See `proposal.md` — Why. What shapes the approach:

- El seguimiento de mediciones ya existe y es la fuente de verdad numérica: tabla `measurements` con
  unicidad `(metric, date)`, valores por componente en `measurement_values`, catálogo de métricas en
  código (`Metric`) y conjunto admitido en `admitted_metrics` (`uc-014`, `design.md` D1–D3).
  **No hay que migrar ni cambiar el esquema:** `UC-014b` solo lee.
- El conjunto de operaciones del turno es un enum cerrado, `AgentOperation`, con tres miembros
  —`CONSULT_HISTORY`, `RECORD_NOTE`, `RECORD_MEASUREMENT`—, y `AgentToolContract` deriva de él la
  declaración que recibe el proveedor. Es el único sitio donde se define el conjunto, así que ampliarlo
  es una decisión local.
- La consulta de la historia ya distingue un ámbito `measurements` en `ClinicalEventIntent.Query.Scope`
  y hoy se limita a **redirigir** (`AgentOperationExecutor.MEASUREMENT_REDIRECT`): es el cauce que
  `uc-014` dejó preparado para este cambio (`uc-014`, `design.md` D10).
- La respuesta de la consulta de la historia es un valor explícito, `ClinicalAnswerResult`
  (`ANSWERED` / `NO_RECORDS` / `FAILURE`), y `ChatMessageResponse` lleva `events`, `absenceReason`,
  `suggestedActions` y las operaciones del turno. El contrato de chat ya es el vehículo de todas las
  respuestas fundadas.
- El repositorio de mediciones (`MetricMeasurementRepository`) ofrece lectura por métricas y por
  métrica y día, y escritura de todo o nada. La consulta necesita acotar por periodo, que hoy no está.
- Las mediciones guardan una **fecha exacta** y su valor **ya convertido** a la unidad de referencia,
  así que la fidelidad que exige la spec se resuelve presentando lo almacenado, sin conversiones ni
  redondeos en la lectura.

## Goals / Non-Goals

**Goals:**

- Leer el seguimiento de mediciones desde la conversación y responder con los valores, las unidades y
  las fechas registrados, sin alterar nada.
- Sostener la separación de cauces: la historia clínica nunca presenta un valor de medición como hecho
  suyo, y el cauce de las mediciones nunca responde hechos clínicos.
- Mantener la garantía estructural de que el proveedor no alcanza la persistencia ni escribe: consultar
  mediciones es de solo lectura.
- Que los valores, las unidades y las fechas lleguen a la persona **tal como se almacenaron**, sin que
  el modelo pueda redondearlos, convertirlos ni inventarlos.

**Non-Goals:**

- El análisis de evolución dedicado y la comparación de valores (Fase 7.3): `UC-014b` puede describir
  con los datos recuperados cómo han cambiado, pero no define una forma específica de presentarlo.
- Convertir la respuesta a la unidad en la que la persona pregunte: se responde en la unidad de
  referencia.
- Ampliar, renombrar o gestionar el catálogo de métricas.
- La vista `/measurements` y sus gráficas (cambio hermano `metric-tracking-view`).
- Modificar o borrar mediciones, y consultar su procedencia (`UC-023`).

## Decisions

### D1 — Una operación propia para consultar el seguimiento

- Se añade `CONSULT_MEASUREMENTS` a `AgentOperation`, con sus argumentos: la pregunta de la persona, la
  métrica o métricas determinadas, el periodo cuando la pregunta lo concrete y la señal de que la
  pregunta no concretó métrica.
- **Por qué:** el conjunto de operaciones es la frontera que garantiza que el asistente no hace nada
  más que lo previsto; una operación nueva y explícita es la forma de que el proveedor pueda pedir la
  lectura sin que se le abra ninguna otra puerta, y `AgentToolContract` la declara sin cambios porque
  deriva del enum.
- **Alternativa descartada:** reutilizar `CONSULT_HISTORY` y responder desde su ámbito `measurements`.
  Mezcla dos fuentes de verdad distintas en una misma operación y obliga a que el compositor de la
  historia sepa de mediciones; mantiene vivo el riesgo de que un valor de medición acabe presentado
  como hecho clínico.

### D2 — El ámbito `measurements` de la historia deja de redirigir y se delega

- El executor sustituye `MEASUREMENT_REDIRECT` por una delegación al servicio de consulta de
  mediciones. `ClinicalEventIntent.Query.Scope.MEASUREMENTS` se conserva como **guarda de enrutado**: si
  el proveedor manda una pregunta de medición por la operación de la historia, se atiende por el cauce
  correcto y **nunca** se responde con hechos clínicos; la historia no devuelve valores de medición.
- **Por qué:** el enrutado lo decide el proveedor y puede equivocarse; la garantía de que la historia no
  presenta valores de medición no puede depender de que acierte. Delegar mantiene la separación aunque
  el enrutado falle, y evita un rebote inútil que la persona leería como un fallo.
- **Alternativa descartada:** retirar `Scope.MEASUREMENTS` y rechazar la consulta. Deja la pregunta sin
  cauce cuando el enrutado falla y convierte un acierto a medias en un fallo visible.

### D3 — La consulta de mediciones es un servicio de solo lectura

- Un `MeasurementQueryService` recibe la métrica o métricas y el periodo, recupera del
  `MetricMeasurementRepository` y devuelve un resultado explícito, análogo a `ClinicalAnswerResult`:
  respondida con mediciones, ausencia, o fallo.
- El servicio no tiene ninguna ruta de escritura: no toca el *upsert*, no crea notas y no abre ninguna
  transacción de escritura. La regla de «consultar nunca altera el seguimiento» se hace estructural.
- **Por qué:** la spec separa la ausencia de mediciones del fallo al recuperarlas, y esa distinción no
  puede depender de que la recuperación devuelva una lista vacía o lance una excepción; un resultado
  explícito la hace verificable.

### D4 — Lectura por métricas acotada por periodo, sin tocar el esquema

- Se añade al repositorio una lectura por métricas y rango de fechas (`findByMetrics` con el periodo
  aplicado), y la consulta se apoya en ella. No hay migración: el modelo de `uc-014` ya guarda lo que
  hace falta.
- **Por qué:** recuperar todo el histórico de una métrica y filtrar en memoria es correcto pero crece
  con el tiempo; acotar en la consulta deja el coste en función del periodo preguntado.
- **Alternativa descartada:** un índice de texto sobre las mediciones. Las mediciones no se buscan por
  texto —se seleccionan por métrica y fecha— y un índice añadiría un segundo camino derivado que
  reconstruir, justo lo que `uc-014` D1 evitó.

### D5 — La unidad de referencia y la etiqueta salen del catálogo

- La respuesta toma la etiqueta en español de la métrica (`Metric.label()`) y su unidad de referencia
  (`Metric.referenceUnit()`) del catálogo; no se guarda ni se duplica la unidad en ningún sitio nuevo.
- **Por qué:** el catálogo ya es la única autoridad sobre la unidad de referencia; leerla de él evita
  que la respuesta y el registro puedan divergir, y hace que ampliar el catálogo no cambie la consulta.
- El valor se presenta tal como está almacenado —ya en la unidad de referencia— sin ninguna
  conversión ni redondeo en la lectura.

### D6 — La aplicación resuelve la métrica y el periodo; el proveedor solo propone

- El proveedor propone qué métrica o métricas y qué periodo abarca la pregunta; la **aplicación**
  valida contra el catálogo admitido y responde de forma determinista a los tres casos límite:
  - la pregunta no concreta métrica: **se pregunta cuál** y no se supone ninguna;
  - la métrica no está en el catálogo o no está admitida: se declara que no forma parte del seguimiento
    y no se inventan valores;
  - la métrica es compuesta: se resuelve como una sola medición con sus componentes.
- **Por qué:** es la misma frontera que el registro de `uc-014` —decidir es del proveedor, validar y
  responder es de la aplicación—, y mantiene la consulta verificable sin depender del criterio del
  modelo.

### D7 — Los valores, las unidades y las fechas los formatea la aplicación

- La aplicación compone un conjunto **determinista** de hechos de medición —etiqueta de la métrica,
  valores con su unidad de referencia, fecha exacta y una referencia identificable— y el compositor en
  español solo redacta alrededor de ellos. El compositor **no recibe la libertad de escribir los
  números**: los valores que llegan a la persona son los que la aplicación formateó.
- **Por qué:** la fidelidad es el requisito duro de este caso de uso —no redondear, no completar, no
  alterar salvo el cambio de unidad y decir la unidad explícitamente— y un modelo de lenguaje no puede
  garantizarla. Es la diferencia clave con la consulta de la historia, donde lo que hay que conservar
  es la palabra de la persona y el modelo sí es el compositor.
- **Alternativa descartada:** dejar que el modelo componga la respuesta a partir de las mediciones
  recuperadas, como se hace con los hechos clínicos. Un redondeo o una conversión no pedida serían
  justo lo que las reglas de negocio prohíben, y no serían detectables desde fuera.
- Se mantiene la misma forma que ya existe para los hechos: una implementación real y una `fake`, para
  poder probar sin red.

### D8 — El resultado del turno distingue el cauce de las mediciones

- La respuesta de chat incorpora las mediciones de apoyo, con su métrica, sus valores, su unidad de
  referencia y su fecha, y un motivo de ausencia propio del seguimiento, distinto de los motivos de
  ausencia de la historia clínica.
- **Por qué:** la spec exige que una ausencia de mediciones no se presente como una ausencia de hechos
  clínicos ni al revés, y que la persona pueda identificar las mediciones en que se apoya la respuesta.
  Un motivo y una lista propios lo hacen verificable desde el contrato, sin reinterpretar el texto en
  español.
- **Alternativa descartada:** reutilizar `events` y los motivos de ausencia existentes. Haría que la
  interfaz tuviera que distinguir por el texto, que es exactamente lo que el contrato evita en los
  demás cauces.

### D9 — La interfaz muestra las mediciones de apoyo, sin vista nueva

- `ChatWorkspace` presenta la respuesta de mediciones con sus valores y fechas y la distingue de una
  respuesta fundada en la historia clínica y de una ausencia de hechos; la ausencia de mediciones se
  presenta con su propio motivo.
- **Por qué:** la superficie de chat ya muestra los hechos de apoyo de una respuesta fundada; mostrar
  las mediciones de apoyo es la misma forma con otro contenido, no una vista nueva, y mantiene el
  alcance de `/measurements` y de `UC-001` fuera de este cambio.

## Risks / Trade-offs

- **[El modelo puede redondear, convertir o inventar valores al redactar]** → D7: la aplicación
  formatea los valores, las unidades y las fechas; el compositor solo redacta sobre ellos y no puede
  alterarlos.
- **[El proveedor puede enrutar una pregunta de medición por la historia clínica]** → D2: el ámbito
  `measurements` sigue detectando el caso y se delega en la consulta de mediciones; la historia nunca
  devuelve valores de medición, acierte o no el enrutado.
- **[Una consulta sin métrica concreta o sobre una métrica no admitida podría responderse con una
  suposición]** → D6: no concretar métrica se resuelve pidiendo que se concrete, y una métrica fuera del
  seguimiento se declara como tal, sin valores y sin sustituirla por otra.
- **[Una respuesta sin acotar el periodo puede ser muy larga para una métrica con muchas mediciones]** →
  el periodo acota la recuperación (D4) y la respuesta se compone con lo recuperado; el formato de la
  evolución llega en la Fase 7.3.
- **[La ausencia de mediciones puede leerse como «la medición no ocurrió»]** → la declaración de
  ausencia se refiere a lo que consta registrado; el texto de ausencia se redacta en esos términos
  (RN-011) y el motivo de ausencia es propio (D8).
- **[Añadir un campo a la respuesta de chat puede afectar a los clientes existentes]** → el campo es
  aditivo y el contrato de chat no tiene consumidores externos ni autenticación; los estados y los
  campos existentes no cambian de significado.

## Migration Plan

1. Desplegar el backend con la operación de consulta, el servicio de lectura, el compositor y el campo
   aditivo de la respuesta. No hay migración de datos ni de esquema: se lee el modelo que `uc-014` ya
   dejó.
2. Desplegar el frontend con la presentación de las mediciones de apoyo. Si el frontend se despliega
   antes, el campo nuevo llega vacío y la interfaz se comporta como hoy.
3. Retorno: revertir el despliegue. Al ser un cambio de solo lectura, no hay estado que restaurar; los
   documentos de `data/` y el seguimiento no se tocan.

## Open Questions

- Hasta qué punto describir la evolución de los valores dentro de la respuesta (UC-014b-R8): la forma
  específica llega en la Fase 7.3 y no cambia lo que este cambio especifica.
- Si el periodo se recibe siempre como rango de fechas o se admiten expresiones relativas resueltas por
  el proveedor: se resuelve igual que en la consulta de la historia y no cambia las specs.
