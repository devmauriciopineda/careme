---
description: "Verifica la documentación contra la especificación (SDD) y corrige el drift de los cambios recientes (openspec/changes/archive): READMEs, docs/architecture.md, docs/data-model.md y estados de use-cases.md"
argument-hint: "Cambio archivado a verificar (p.ej. 2026-09-13-uc-004). Si se omite, usa el más reciente."
agent: "agent"
---

# Rol

Actúas como mantenedor de la documentación de Careme. Tu trabajo es verificar que la
documentación siga siendo **verdad** respecto a la **especificación** (SDD), usando como
disparador los cambios recientes registrados en OpenSpec (`openspec/changes/archive/`).
Primero **reportas** el drift y, solo tras confirmación, **corriges** con ediciones mínimas.
El procedimiento es **idempotente**: si la documentación ya está correcta, no modificas nada.

# Alcance

**Documentos editables (único alcance):**

- `README.md`
- `backend/README.md`
- `frontend/README.md`
- `docs/architecture.md`
- `docs/data-model.md`
- `docs/roadmap/use-cases.md` — **solo los estados** (ver Paso 4b). Nunca sus descripciones.

**Fuera de alcance (no editar, no reestructurar):**

- `docs/use-cases/**` (los documentos formales de cada UC)
- Resto de `docs/roadmap/**`
- `frontend/AGENTS.md`, `frontend/CLAUDE.md`
- `graphify-out/**` (generado por `graphify`)
- `openspec/**` (solo lectura: es la fuente de verdad de los cambios)

**Idioma por documento:** `README.md`, `backend/README.md`, `frontend/README.md`,
`docs/architecture.md` y `docs/data-model.md` están en inglés; `docs/roadmap/` y
`docs/use-cases/` en español. Respeta el idioma de cada documento: no lo traduzcas ni
lo cambies de idioma por tu cuenta.

Si un cambio archivado afecta a descripciones o alcances de use cases, **menciónalo como aviso y no los edites**.

# Paso 1 — Identificar el cambio disparador

1. **Si hay argumento** (p.ej. `2026-09-13-uc-004` o un sufijo parcial): resuélvelo dentro de
   `openspec/changes/archive/`. Si no hay coincidencia, lista los cambios archivados y pide elegir.
2. **Si no hay argumento:** elige el cambio más reciente por fecha (prefijo `YYYY-MM-DD`).
   Si hay varios con la misma fecha, lístalos y pide cuál o si son todos.
3. Si el cambio sigue en `openspec/changes/<name>/` (sin archivar), avísalo y úsalo igual.

Anuncia qué cambio vas a usar y cómo cambiarlo.

# Paso 2 — Extraer los hechos del cambio

Lee del cambio disparador:

- `proposal.md` — *Why*, *What Changes*, *Capabilities*, *Impact*.
- `design.md` — decisiones y límites.
- `tasks.md` — qué se implementó realmente.
- `specs/<capability>/spec.md` — el contrato exigido.

Contrasta con el spec vigente en `openspec/specs/<capability>/spec.md`.

Extrae solo **hechos observables**: endpoints y rutas, DTOs, entidades y campos, migraciones,
pantallas/rutas de frontend, variables de entorno, dependencias.

# Paso 3 — Fuente de verdad (SDD)

La **especificación es la fuente de verdad**: el contrato vigente en
`openspec/specs/<capability>/spec.md` y los artefactos del cambio archivado (`proposal.md`,
`design.md`, `tasks.md`) definen qué debe documentarse.

- **No hagas inspecciones extensivas del código.** Contrasta la documentación contra los specs.
- Solo si un punto concreto no puede resolverse desde los specs, haz **consultas puntuales** al
  código (una búsqueda o la lectura de un archivo específico). No catastres controladores,
  servicios, entidades ni el árbol del repositorio.
- `graphify query "<pregunta>"` sirve para dudas puntuales de relaciones, no para auditar.

Cita siempre `ruta` del spec (y línea cuando sea útil) como evidencia.

# Paso 4 — Detectar drift por documento

Para cada documento del alcance, comprueba:

- `README.md`: descripción, *Use of APIs or external services*, endpoints, *Basic usage*, estructura.
- `backend/README.md`: endpoints, stack, ejecución, base de datos.
- `frontend/README.md`: rutas, stack, ejecución, autenticación.
- `docs/architecture.md`: secciones 1–9 (executive summary, context and scope, technology
  stack, container view, component view, repository map, patterns, ADR-light, security).
  Revisa la tabla de lenguaje ubicuo y que cada afirmación cite el archivo que la respalda.
  Resuelve los `⚠️ NOT DETECTED` que los specs ya permitan confirmar.
- `docs/data-model.md`: entidades **implementadas** vs **propuestas**, campos, validaciones,
  migraciones y diagrama ER.

Clasifica cada hallazgo como: **DESACTUALIZADO**, **FALTA**, **OBSOLETO** o **MARCADOR RESUELTO**
(un `⚠️ NOT DETECTED` que ya puede confirmarse).

# Paso 4b — Estados en `docs/roadmap/use-cases.md` (solo estados)

Actualiza **únicamente los estados**, nunca las descripciones ni los alcances:

- Las líneas `**Estado:** …` de cada UC.
- El encabezado *Control de avance* (`N de M implementados · …`) y la línea *Implementación actual*.

Determina el estado desde los specs: un UC pasa a ✅ **Implementado** cuando su capability existe en
`openspec/specs/` con las historias exigidas; 🟡 **Cubierto** o ⬜ **Pendiente** según ese mismo
criterio. **No reescribas** el texto descriptivo de cada UC ni añadas ni elimines UCs.

# Paso 5 — Reporte (en el chat)

Presenta una tabla:

| Documento | Sección | Afirma | Realidad (evidencia) | Acción propuesta |
| --- | --- | --- | --- | --- |

Añade un veredicto por documento (OK / con drift) y la propuesta de cambios de estado en
`docs/roadmap/use-cases.md`. Si un cambio afecta descripciones o alcances de use cases,
menciónalo como aviso (**sin editar**). Si no hay drift, dilo y termina sin tocar archivos.

# Paso 6 — Aplicar correcciones con confirmación

- Muestra el diff propuesto por hallazgo y aplica **solo lo confirmado**.
- Edita con las herramientas de edición de archivos, nunca por terminal.
- Cambios **quirúrgicos**: corrige lo falso, no reescribas documentos completos.
- **Idempotencia**: si un hallazgo ya está resuelto, deja el archivo **intacto**; no reformatees,
  no reescribas contenido idéntico ni "mejores" la redacción cuando no hay drift real.
- Al terminar, resume los archivos tocados y qué se corrigió en cada uno.

# Reglas

- Cada afirmación nueva debe citar `ruta` del spec (y línea) que la respalde.
- Lo no deducible se marca `⚠️ NOT DETECTED — requires human confirmation`; nunca lo inventes.
  Los documentos en inglés usan ese literal (y `✅ RESOLVED` para un marcador ya confirmado):
  búscalo así, no en español.
- **Idempotente**: reejecutar el prompt sobre documentación ya sincronizada debe producir **cero
  ediciones** (solo el veredicto OK), sin cambios de formato ni de redacción.
- La especificación manda (SDD): no audites el código; solo consultas puntuales cuando haga falta.
- Mantén el idioma y el tono/estilo de cada documento, según la tabla de idioma del Alcance.
- No edites código ni `openspec/`. En `docs/roadmap/use-cases.md` toca **solo los estados**; en el
  resto de `docs/roadmap/` y en `docs/use-cases/` no edites nada.
