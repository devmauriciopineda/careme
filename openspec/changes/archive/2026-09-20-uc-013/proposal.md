## Why

Hoy la conversación es estado en memoria sin identidad duradera y cada mensaje se registra por
separado: lo que la persona cuenta queda repartido en eventos sueltos, sin constancia de dónde salió ni
de quién lo aportó, y el asistente no tiene dónde dejar lo que la persona menciona de pasada. La Fase
4.1 del roadmap introduce la **consulta** como unidad de conversación con identidad y ciclo de vida, y
exige que los hechos se registren **con su procedencia** en lugar de registrarse frase a frase.

## What Changes

- La conversación pasa a ser una **consulta** con identidad propia: se abre al iniciar la conversación,
  la cierra la persona cuando lo decide, no se reabre ni se retoma, y solo hay una en curso.
- Durante la consulta el asistente **toma notas** de los hechos clínicos que la persona menciona —aunque
  no sean el foco de su mensaje—, completa lo que falta, pide aclaración de lo ambiguo y amplía el
  contexto. Una nota no es un hecho registrado ni forma parte de la historia clínica.
- **BREAKING**: el registro deja de ocurrir dentro del turno. Al cerrar la consulta se registran los
  hechos recogidos y solo entonces quedan disponibles para consulta. La admisibilidad se comprueba al
  anotar cada hecho: lo que no alcanza la información mínima se rechaza entonces, sin completarlo con
  suposiciones, de modo que ninguna nota que llegue al cierre es inadmisible.
- Todo hecho registrado desde una consulta **declara su procedencia**: quién lo aportó, cuándo se
  registró y de qué consulta procede. La procedencia se fija en el momento del registro.
- La consulta conserva un **resumen** propio, con un motivo a modo de título generado automáticamente,
  marcado como información derivada y reconstruible desde sus hechos; la persona no lo ve.
- El cierre es definitivo: una interrupción de la conversación o el inicio de otra consulta cierran la
  anterior con lo que tenga, registrando lo admisible y guardando el resumen.
- **BREAKING**: el resultado del turno deja de informar de un registro inmediato y pasa a informar de los
  hechos recogidos para el cierre.
- El conjunto de operaciones disponible por turno durante la consulta se acota a **consultar la historia
  y tomar notas**; registrar deja de ser una operación del turno.
- La superficie del chat permite a la persona **terminar la consulta**.

## Capabilities

### New Capabilities

- `clinical-consultation`: la conversación como consulta con identidad y ciclo de vida —apertura al
  iniciar la conversación, cierre por decisión de la persona, no reapertura, una sola en curso—, las
  notas que recoge mientras está abierta, y el resumen propio que conserva al cerrarse.
- `clinical-fact-provenance`: la procedencia que declara todo hecho registrado desde una consulta —quién
  lo aportó, cuándo se registró y de qué consulta procede—, fijada en el momento del registro.

### Modified Capabilities

- `assistant-conversation`: la conversación mantiene la consulta abierta con sus notas, y el resultado
  del turno deja de informar de un registro para informar de lo recogido.
- `assistant-agent-turn`: el conjunto de operaciones disponibles durante la consulta se acota a consultar
  la historia y tomar notas; el registro ocurre al cierre.
- `clinical-event-registration`: el registro puede originarse al cerrar una consulta, con los hechos
  recogidos a lo largo de la conversación, y su disponibilidad se produce en ese cierre.
- `assistant-chat-interface`: la persona puede terminar la consulta desde el chat y la superficie refleja
  la consulta cerrada.

## Impact

- `backend/`: contrato de chat (resultado del turno y operación de cierre de la consulta), orquestación
  del asistente, registro de eventos, persistencia Markdown de la consulta y del front-matter de
  procedencia, índice derivado y migraciones Flyway.
- `frontend/`: superficie del chat, con la acción de terminar la consulta y sus estados.
- Contrato documentado en `backend/README.md`, arquitectura y modelo de datos en `docs/architecture.md` y
  `docs/data-model.md`, y `docs/use-cases/UC-013.md`, `UC-013b.md`, `UC-012.md` y
  `docs/use-cases/reglas-de-negocio.md`.
- Pruebas de backend (dominio, persistencia, contrato de chat con Testcontainers) y de extremo a extremo.
- El comportamiento documentado de `UC-012` y la disponibilidad inmediata del registro cambian; la
  revisión pendiente de `UC-012` (§11) queda gobernada por este cambio.
