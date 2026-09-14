## Why

Careme ya registra hechos médicos por chat (UC-004) y los indexa en PostgreSQL,
pero el usuario todavía no puede recuperarlos: el ciclo fundamental del producto
—contar un hecho y después preguntar por él— queda abierto y la historia clínica
no aporta valor por sí sola. Este cambio cierra ese ciclo respondiendo preguntas
en lenguaje natural con una base documental verificable.

## What Changes

- Reconocer en el chat el mensaje que pregunta por la propia historia clínica y
  distinguirlo de un registro (UC-004), de la conversación general (UC-010) y de
  las preguntas sobre peso o circunferencia abdominal.
- Recuperar los hechos que respaldan la pregunta con búsqueda léxica sobre el
  índice derivado existente (`clinical_event_index`) y filtros de metadatos por
  tipo y fecha, cubriendo la recuperación interna de UC-005 y UC-006.
- Redactar una respuesta en español usando **únicamente** los hechos recuperados,
  conservando su contenido y su precisión temporal (una fecha aproximada nunca se
  presenta como exacta).
- Devolver con la respuesta las **referencias** a los hechos que la sustentan,
  para que el usuario pueda verificarla.
- Añadir los resultados de turno `answered` y `no_records` y declarar de forma
  explícita y en español cuando no existen registros, sin rellenar el vacío con
  conocimiento general ni suposiciones.
- Permitir preguntas de seguimiento apoyadas en los turnos recientes de la
  conversación activa, sin conservar el historial más allá de ella.
- Pedir aclaración ante preguntas ambiguas y, ante un fallo, no inventar una
  respuesta, conservar la pregunta y ofrecer reintentar.
- Mantener la consulta **sin efectos**: la historia clínica no se modifica en
  ningún caso.

## Capabilities

### New Capabilities

- `clinical-history-query`: recuperar hechos clínicos de la única historia y
  responder preguntas en lenguaje natural apoyándose solo en ellos, con
  referencias verificables, conservación de la precisión temporal, declaración de
  ausencia, aclaración ante ambigüedad y sin modificar la historia.

### Modified Capabilities

- `assistant-conversation`: el contrato de conversación acotada incorpora los
  resultados `answered` y `no_records`, el contexto efímero de turnos recientes
  para preguntas de seguimiento y la garantía de idempotencia y no persistencia
  también para los turnos de consulta.
- `llm-clinical-intent-adapter`: el adaptador debe clasificar además una
  intención de consulta y componer una respuesta fundada restringida a los hechos
  recuperados, validando que no se afirme nada sin respaldo.
- `assistant-chat-interface`: la interfaz debe mostrar la respuesta junto con los
  hechos en que se apoya y representar de forma distinta el estado de ausencia de
  registros.

## Impact

- **Backend**: nuevos componentes de recuperación sobre `clinical_event_index`
  (consulta léxica + filtros por tipo y fecha), un servicio de consulta de la
  historia y la coordinación de los nuevos resultados en `ChatOrchestrator`.
  `ClinicalIntentInterpreter` y su implementación OpenAI amplían su contrato con
  la intención de consulta y la composición fundada.
- **Contrato de chat**: se añaden `answered` y `no_records` a
  `ChatMessageResponse.Status`; la respuesta de consulta reutiliza `events` para
  transportar las referencias a los hechos de apoyo. Los estados existentes no
  cambian de significado.
- **Frontend**: `features/chat` amplía su esquema de validación y `ChatWorkspace`
  representa la respuesta con sus hechos de apoyo y el estado sin registros.
- **Datos**: no se añaden tablas ni migraciones obligatorias; se apoya en el
  índice `clinical_event_index` y su `search_vector` ya existentes. El índice
  sigue siendo derivado y reconstruible desde Markdown.
- **Prompt**: nueva versión del prompt de interpretación que añade la
  clasificación de consulta sin alterar la de registro.
- **Fuera de alcance**: búsqueda semántica o vectorial (Fase 6 del roadmap),
  resúmenes longitudinales, líneas de tiempo, edición o borrado de hechos,
  inspección técnica de eventos (UC-011), detalle de gestión de información
  ausente (UC-008) y conversación general (UC-010).
