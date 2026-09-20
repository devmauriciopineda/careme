## Context

Ver [`proposal.md`](./proposal.md) para la motivación. El estado actual que condiciona el enfoque:

- La conversación vive en memoria y el turno registra dentro de su propio camino
  (`assistant-conversation`, `assistant-agent-turn`). El contrato devuelve un resultado observable por
  mensaje, hoy con `registered` entre sus valores.
- Los eventos son documentos Markdown en `data/events/` (`evt_NNN.md`) con front matter versionado, y
  PostgreSQL es un índice derivado y reconstruible (`clinical_event_index`). El modelo ya declara los
  campos `source` y `created_at` (ver `docs/data-model.md` §2.2).
- El modelo objetivo de la Fase 4 ya está documentado en `docs/data-model.md` §6.1: la consulta se
  representa en Markdown en `data/encounters/` con código `enc_NNN`, PostgreSQL la indexa, admite los dos
  momentos de registro y puede conservar un resumen propio marcado como información derivada.

## Goals / Non-Goals

**Goals:**

- Que lo que la persona cuenta sobreviva a la conversación: recogido en notas mientras habla, registrado
  como hechos con procedencia al cerrar.
- Que la consulta sea la unidad con identidad y ciclo de vida, sin introducir historial de conversación
  persistido.
- Que el cierre sea determinista, idempotente y gobernable ante interrupciones y fallos.

**Non-Goals:**

- Persistir el historial de la conversación: sigue diferido. La consulta y sus notas no son historial de
  conversación.
- Exponer o consultar la procedencia de un hecho (UC-023), reunir hechos en condiciones (UC-020),
  relacionarlos (UC-022) o componer el perfil extendido (UC-024).
- Los campos de procedencia restantes de la Fase 4.9 —documento de origen, extracción automática,
  confirmación, nivel de confianza—: aquí solo quién, cuándo y de qué consulta.
- Cambiar el canal de cada mensaje, el retrieval o la inspección más allá de lo que exige el cambio de
  momento del registro.

## Decisions

**D1 — El registro ocurre solo al cerrar la consulta.** El modelo objetivo admitía los dos momentos; se
elige el cierre porque es el único que hace que la consulta —y no el mensaje— sea la unidad de
procedencia, y porque la persona puede completar y corregir lo que cuenta mientras conversa.
*Alternativa descartada:* registrar a medida que avanza, que produciría varios hechos por consulta en
momentos distintos y repartiría la procedencia sin necesidad.

**D2 — La consulta se persiste desde que se abre.** El documento de la consulta se crea al abrirla y se
actualiza con las notas de cada turno; en el cierre se registran los hechos y se finaliza. Es lo que
permite que una interrupción no pierda lo narrado (`clinical-consultation`, «Conservar las notas ante una
interrupción»).
*Alternativas descartadas:* persistir solo al cerrar (una caída del proceso perdería toda la consulta);
mantener las notas solo en memoria (haría ingobernables los flujos de interrupción y de cierre
reintentable).

**D3 — Las notas viven en el documento de la consulta, no como eventos en borrador.** Una nota no es un
hecho: si se materializara como evento, aparecería en las consultas a la historia y en la inspección, y
contradiría «Mantener las notas fuera de la historia clínica».
*Alternativa descartada:* eventos con estado borrador, que añadirían un estado al modelo de eventos y
obligarían a filtrarlo en todas las lecturas.

**D4 — La interrupción no usa temporizador.** Una consulta se cierra cuando la persona lo pide, cuando
inicia otra conversación o cuando termina el proceso. No se introduce cierre por inactividad en este
cambio.
*Alternativa descartada:* cerrar por inactividad, que exigiría decidir y documentar un umbral y avisar a
la persona, y cambiaría el comportamiento observable de forma que las specs no recogen.

**D5 — El cierre es una operación explícita e idempotente del contrato de chat.** No hay forma de saber
que la persona terminó si no lo dice, y el canal conversacional no es determinista para detectarlo.
*Alternativas descartadas:* inferir el cierre (imposible con la información disponible); un mensaje
conversacional del tipo «he terminado» (dependería del proveedor y no sería determinista ni
reintentable de forma segura).

**D6 — El catálogo de resultados del turno pierde `registered` y gana `noted`.** Un cliente debe poder
distinguir «quedó recogido» de «quedó registrado»; el registro aparece únicamente en el resultado del
cierre.
*Alternativa descartada:* conservar `registered` con significado diferido, que haría que el mismo valor
significara dos cosas distintas según el momento.

**D7 — La procedencia reutiliza lo que ya existe y añade una referencia a la consulta.** `source` y
`created_at` ya están en el modelo de evento; lo nuevo es la referencia a la consulta, que se declara en
el front matter del evento (fuente de verdad) además de en el índice derivado.
*Alternativa descartada:* guardar la referencia solo en PostgreSQL, que siendo derivado la perdería al
reconstruirse.

**D8 — El resumen y el motivo se guardan en el documento de la consulta y se marcan como derivados.** El
motivo se genera en el cierre a partir de las notas y de los hechos registrados; no lo escribe la persona
ni se muestra.
*Alternativas descartadas:* el resumen como documento independiente (una pieza más sin necesidad);
asignar el motivo al abrir la consulta (sería más pobre que generarlo con lo ya recogido).

**D9 — La detección de duplicados se extiende a las notas de la consulta.** Se reutiliza la detección
existente sobre los eventos registrados y se aplica además a lo ya recogido en la consulta en curso, para
que mencionar el mismo hecho varias veces registre un solo evento. El cierre **depura además las notas por
huella** antes de registrar, de modo que ni el registro ni el resumen pueden contener el mismo hecho dos
veces. La detección es **dentro de la misma conversación**: el mismo hecho mencionado en otra conversación
es un hecho nuevo, con su propia procedencia, y no se compara con lo registrado antes.

**D10 — La superficie de chat gana la acción de terminar la consulta y no muestra sus interioridades.**
El identificador, el resumen y el motivo quedan fuera de la interfaz; el resultado del cierre se muestra
como un mensaje más, distinguiendo un cierre que registró hechos, uno sin hechos y uno fallido.

**D11 — El cierre es atómico por hecho y la consulta solo se da por cerrada si se completa.** Cada hecho
conserva la atomicidad ya existente (`clinical-event-registration`), y un fallo deja la consulta abierta y
reintentable en lugar de cerrarla a medias.

**D12 — La admisibilidad de un hecho se comprueba al anotarlo, no al cerrar.** Recoger una nota y
registrarla ejecutan la misma validación determinista, y `EncounterNote` no admite una nota sin tipo, sin
contenido, sin precisión o con fecha exacta y sin fecha. Por eso ninguna nota que llegue al cierre puede
ser inadmisible: el cierre registra todo lo recogido y no hay rama de descarte. Lo que no alcanza la
información mínima se rechaza al anotarlo, sin completarlo con suposiciones, y la persona recibe entonces
la aclaración correspondiente.

## Risks / Trade-offs

- **[La persona ya no ve el hecho registrado al contarlo]** → la respuesta del turno declara que quedó
  recogido para el cierre, y el cierre confirma lo registrado; documentarlo en `backend/README.md` y en la
  interfaz para que no se lea como una pérdida.
- **[Una consulta puede quedarse abierta durante una sesión larga]** (D4) → el inicio de otra conversación
  y el fin del proceso la cierran; el comportamiento es aceptado y queda registrado en las specs.
- **[Cambio incompatible del contrato de chat]** → se actualizan contrato, interfaz y pruebas en el mismo
  cambio; no hay clientes externos en el MVP.
- **[Regresión sobre UC-012]** → los escenarios y las pruebas que describen el registro dentro del turno
  se reescriben; la revisión pendiente de `UC-012` (§11) queda cubierta por este cambio.
- **[Las notas se convierten en un almacén no deseado]** → al cerrar, las notas se descartan: solo
  sobreviven los hechos registrados y el resumen, y no existe consulta de notas.
- **[Una escritura del documento de la consulta en cada turno]** → como máximo una publicación atómica por
  turno, con el mismo patrón que la publicación de eventos.
- **[Eventos anteriores al cambio, sin consulta]** → la procedencia sin consulta es válida y no se le
  atribuye un origen inexistente.

## Migration Plan

1. Modelo y almacén de la consulta (`data/encounters/`, código `enc_NNN` en paralelo a `evt_NNN`), con su
   migración Flyway para el índice derivado y su reconstrucción.
2. Persistencia de la consulta al abrirse y de sus notas por turno.
3. Recogida de notas en el turno: retirada de `registered` del catálogo del turno, alta de `noted`,
   extensión del dedup y finalización de la información.
4. Cierre de la consulta: validación, registro de los hechos con su procedencia, resumen y motivo.
5. Contrato de chat (operación de cierre) y superficie del chat (acción de terminar y estados).
6. Documentación (`backend/README.md`, `docs/architecture.md`, `docs/data-model.md`) y pruebas.

**Rollback:** revertir el código deja los documentos de consulta en `data/encounters/` inertes y el índice
reconstruible; los eventos registrados antes del cambio conservan su validez y no se reescriben. Antes de
dar por seguro el rollback hay que verificar que un lector anterior tolera la referencia a la consulta en
el front matter de los eventos registrados después del cambio.

## Open Questions

- El formato y la longitud del motivo generado: no afectan a las specs ni al desglose de tareas.
- Exponer el resumen de la consulta y la procedencia de un hecho en una vista: llega con UC-023 y la
  inspección de eventos (UC-011).
- Si la inactividad debe llegar a cerrar una consulta: decisión diferida y revisable sin cambiar el modelo
  ni la representación; hoy la decisión es D4.
