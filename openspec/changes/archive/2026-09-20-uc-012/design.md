## Context

Hoy el turno del chat es una cadena de decisión única. `ChatOrchestrator.process` pide al intérprete una
intención (`ClinicalIntentInterpreter`, cuatro implementaciones entre real y simulada) y, según el
resultado, invoca exactamente una cosa: `ClinicalEventRegistrationService`, `ClinicalHistoryQueryService`,
el compositor conversacional o una pregunta de aclaración. La intención se valida con
`ClinicalEventIntentValidator` antes de cualquier escritura, y el proveedor se elige con
`CAREME_LLM_MODE` (`application.yml`), cuyo valor por defecto es `fake`.

Los prompts vigentes son `clinical-intent-v3.txt`, `clinical-answer-v3.txt` y
`conversation-reply-v1.txt`. El índice derivado se consulta por `ClinicalEventQueryRepository` con
`tsvector` y filtros de metadatos, con un máximo de resultados configurable
(`careme.chat.query.max-results`).

Motivación y alcance: ver `proposal.md`. Requisitos: ver `specs/`.

## Goals / Non-Goals

**Goals:**

- Sustituir «una intención por mensaje» por un **bucle de operaciones acotadas** que el asistente elige y
  el backend ejecuta, conservando la validación determinista y la escritura del lado de la aplicación.
- Representar en el contrato de turno **más de una operación completada** y la operación que no se
  completó, sin multiplicar los estados.
- Hacer del **asistente real el comportamiento por defecto**, conservando un doble de prueba
  determinista que también elija operaciones.

**Non-Goals:**

- La consulta como sesión con identificador y la procedencia de los hechos (UC-013).
- El modelo de mediciones con dimensión de métrica (UC-014) y el catálogo de tipos de la Fase 4.5.
- Recuperación híbrida o semántica (Fase 6), streaming y **actuación multi-etapa o en segundo plano**
  (`mvp_alcance` §14): el turno sigue siendo un mensaje, una respuesta.

## Decisions

### D1 — El bucle de operaciones vive en el backend, y el modelo elige entre funciones declaradas

La frontera con el proveedor pasa de «devuelve una intención JSON» a «pide ejecutar una de las funciones
declaradas», por *function calling* de la API compatible con OpenAI. Las funciones declaradas son
exactamente dos: consultar la historia y registrar un hecho. El backend ejecuta cada función dentro de la
llamada —validando antes de escribir— y devuelve el resultado al modelo, que decide si necesita otra
función o ya puede responder.

Alternativas consideradas:

- **Mantener la intención única y encadenar por reglas en el backend.** Descartada: decidir qué falta
  requiere comprender el mensaje, y las reglas no lo hacen sin volver a interpretarlo.
- **Dejar que el modelo construya consultas o acceda al almacenamiento.** Descartada por el principio de
  `roadmap_asistente_historia_clinica.md` §3.3 —el agente no conoce el almacenamiento— y porque abre el
  límite clínico.
- **Agente genérico con registro dinámico de funciones.** Descartada como sobre-ingeniería: el conjunto es
  cerrado y tiene dos miembros; enumerarlo en el prompt es más simple y auditable.

### D2 — El turno transporta una lista de operaciones en lugar de un estado por combinación

La respuesta incorpora las operaciones completadas, en orden, cada una con su propio resultado y su propia
carga, más la operación que no se completó si la hubo. El estado del turno se deriva: el resultado de la
operación cuando solo hubo una, `answered` cuando hubo varias y una respuesta, `failed` cuando alguna no se
completó.

Alternativa considerada: añadir estados para cada combinación (`answered_and_registered`,
`registered_partially_failed`, …). Descartada por explosión combinatoria y porque obliga al cliente a
conocer cada combinación.

### D3 — La operación de registro valida y escribe dentro de la llamada

La función de registro, al ser invocada, valida y publica de forma atómica, y devuelve al modelo el evento
resultante (tipo, código, fecha con su precisión). El modelo nunca ve rutas ni escribe.

Alternativa considerada: mantener al modelo proponiendo candidatos y al backend decidiendo el momento de
la escritura. Descartada porque impide lo que esta subfase busca: que el asistente use el resultado de una
operación para decidir la siguiente.

### D4 — Un fallo posterior no revierte lo completado

Un registro ya publicado permanece. La respuesta informa de lo registrado y de lo que no pudo completarse.
No hay transacción que abarque el turno.

Alternativas consideradas: envolver el turno en una transacción y revertir todo ante cualquier fallo.
Descartada porque obliga a mantener una transacción abierta a través de llamadas de red al proveedor, y
porque revierte un hecho que ya se había confirmado a la persona. Riesgo aceptado y declarado en la
especificación.

### D5 — El modo simulado pasa a ser un doble de prueba que también elige operaciones

`FakeClinicalIntentInterpreter` deja de responder a «¿qué intención tiene este texto?» y pasa a responder a
«¿qué operación pide este texto?», con un guion determinista. Se conserva porque las pruebas deben correr
sin red (`backend-tests.instructions.md`) y porque hay caminos —fallo del proveedor, operación no ofrecida,
fallo posterior a un registro— que el proveedor real no produce a voluntad.

Alternativa considerada: eliminar el modo simulado. Descartada: dejaría el camino del agente sin pruebas
deterministas y obligaría a cada prueba a depender de la red.

### D6 — El proveedor real es el valor por defecto

El valor por defecto de `CAREME_LLM_MODE` pasa a ser el proveedor real en `application.yml`,
`.env.example` y `docker-compose.yml`. El modo simulado sigue seleccionable por configuración para
desarrollo y pruebas, y nunca se elige de forma implícita.

Alternativa considerada: conservar el simulado por defecto y activar el real con un indicador explícito.
Descartada porque deja el comportamiento por defecto en manos de una heurística de desarrollo
(`mvp_alcance` §16) y contradice la subfase.

### D7 — Los prompts se versionan de nuevo

Se añade una versión nueva por prompt: el de operaciones sustituye al de intención, y el de composición se
ajusta para redactar una sola respuesta que cubra varias operaciones. Se conservan las versiones anteriores
como archivos, conforme a la convención del repositorio.

Alternativa considerada: un único prompt que enumere operaciones y composición. Descartada: mezcla la
elección de operaciones con la redacción y dificulta probar cada parte por separado.

### D8 — La respuesta final se compone sobre los resultados de las operaciones

Una operación de consulta devuelve lo que la aplicación ya sabe producir —los hechos recuperados, con su
precisión temporal, más la respuesta fundada y el motivo de ausencia—, y el asistente compone con ese
material **una sola** respuesta que cubre todas las operaciones del turno. El texto de cada operación es
material para el asistente, no la respuesta final: sin esto, un mensaje que necesita dos consultas
produciría dos respuestas pegadas y no una coherente.

La consecuencia es que las reglas de fundamentación que ya existen —no introducir hechos, fechas ni
interpretaciones ausentes del conjunto recibido, y rechazar una respuesta que cite un hecho fuera de él—
pasan a aplicarse a la respuesta final del turno, no solo a la respuesta de una consulta aislada.

Alternativa considerada: que el backend concatene las respuestas de las operaciones. Descartada porque no
produce una respuesta coherente cuando el mensaje necesita varias, que es el caso que este cambio existe
para atender.

## Risks / Trade-offs

- **[La negativa a recomendar depende del prompt]** → El requisito de declinar una petición de
diagnóstico, recomendación o interpretación vive hoy en una instrucción del prompt. Con la formulación
que describía la conducta, el asistente real no declinaba un mensaje mixto; exigir la negativa como
elemento obligatorio de la respuesta, con su frase literal, sí funcionó. Se acepta la dependencia del
prompt por decisión explícita, con la salida conocida si falla: una guarda determinista en la aplicación.
Su fiabilidad se mide sobre el corpus de evaluación (tarea 8.3), que incluye peticiones de recomendación
y de interpretación mezcladas con hechos registrables.

- **[El asistente no elige las operaciones que el mensaje necesita]** → El prompt declara el conjunto
  completo y el caso «ninguna operación puede atenderlo» está especificado; la calidad del agente se mide
  con el proveedor real sobre el corpus de evaluación (tarea 8.3) y en el recorrido de extremo a extremo,
  no con el doble de prueba.
- **[Coste y latencia al necesitar varias llamadas al proveedor en un turno]** → Límite máximo de
  operaciones por turno, configurable (`careme.chat.agent.max-operations`, 4 por defecto), de modo que
  el coste de un turno está acotado por construcción y se ajusta sin cambiar el código. El corpus de
  evaluación mide cuántas operaciones necesita un turno real, para ajustar ese valor con evidencia.
- **[Escritura parcial en un turno de varias operaciones]** → Aceptado y especificado: la respuesta
  declara qué quedó registrado y qué no, y no presenta el turno como completo. Ver D4.
- **[El doble de prueba deja de representar al asistente real]** → Se asume explícitamente: es un doble de
  prueba para el andamiaje, no un evaluador del agente. La verificación del comportamiento agéntico se
  apoya en el proveedor real.
- **[Cambiar el valor por defecto rompe el arranque local de quien no tenga credencial]** → `README.md` y
  `.env.example` documentan la credencial requerida; los perfiles de prueba fijan el modo simulado de
  forma explícita, de modo que el CI no necesita red ni credencial.
- **[El mensaje intenta pedir una operación fuera del conjunto]** → El conjunto se aplica en el backend: una
  función no ofrecida no se ejecuta y el intento se rechaza sin efecto; ver `specs/` del adaptador.

## Migration Plan

No hay cambios de esquema ni migraciones de datos: los eventos siguen siendo documentos Markdown.

1. Introducir la frontera de operaciones y el bucle, manteniendo el contrato HTTP actual.
2. Ampliar el contrato de turno con las operaciones completadas y la que no se completó, y actualizar el
   cliente a la vez.
3. Sustituir los prompts por sus versiones nuevas y adaptar el doble de prueba.
4. Cambiar el valor por defecto del proveedor y actualizar despliegue y documentación.

**Rollback:** revertir el valor por defecto del proveedor a `fake` no requiere ninguna migración, y los
hechos registrados durante el periodo afectado son Markdown y sobreviven. Los pasos 1 a 3 se revierten con
el código; ninguno escribe datos que haya que deshacer.

## Open Questions

- El número máximo de operaciones por turno: la configuración admite 4
  (`careme.chat.agent.max-operations`) y el corpus de evaluación mide cuántas operaciones necesita un
  turno real, de forma que el valor se ajuste con evidencia y no por intuición. Cambiarlo no toca las
  especificaciones ni el desglose de tareas.
