# Plan: Alinear roadmap, MVP, use cases y README con el repositorio

Ajustar la documentación (solo documentos) para que el roadmap del asistente de historia clínica personal y el alcance del MVP dejen de describir una arquitectura inexistente y reflejen la del repositorio: monorepo `frontend/` (Next.js 16) + `backend/` (Spring Boot 3.5) + PostgreSQL 17 con Flyway, con el seguimiento de peso y circunferencia abdominal ya implementado. La historia que deben contar los documentos queda: **Markdown en `data/events/` como fuente de verdad, índice full-text derivado en PostgreSQL, backend Spring Boot como dueño de la orquestación del LLM**, con el chat del asistente como punto de entrada y el seguimiento corporal como vista anexa.

## Decisiones que deben quedar escritas en los documentos

- `data/events/` en la raíz del repositorio, ignorado por git y montado como volumen en el contenedor del backend.
- Índice reconciliado por partida doble: reconstrucción al arrancar el backend **y** endpoint de reindexado explícito.
- Full-text de PostgreSQL con configuración `simple` (`tsvector` + índice GIN).
- Prompts en `backend/src/main/resources/prompts/`, respuesta JSON sin streaming en el MVP.
- Proveedor compatible con OpenAI, consumido desde el backend; la clave en variable de entorno del backend y nunca expuesta al frontend.
- Modelo de datos: UUID + `code` legible (`evt_NNN`) + `date` / `date_precision` / `date_text`.
- Rutas `/` = chat y `/measurements` = anexo; peso/cintura y UC-001..003 intactos; conversación sin persistir en el MVP, pero registrada en el roadmap.

## Conflictos detectados (documento → repositorio)

| Documento propone | Repositorio tiene |
| --- | --- |
| MVP §6: filesystem Markdown **excluyendo** BD relacional; SQLite FTS5 (§7) | PostgreSQL 17 + Flyway + JPA ya en uso; el índice puede ser PostgreSQL |
| MVP §14: excluye PostgreSQL en "Infraestructura" | PostgreSQL es el motor de datos del backend |
| MVP §13: estructura Python (`app/agent`, `main.py`, `markdown_store.py`) | `backend/src/main/java/com/careme/backend/{controller,service,repository,entity,dto,config,exception}` + `frontend/src/{app,components,features,lib,services}` |
| MVP §7 índice con `patient_id` | Paciente único, sin entidad `Patient` |
| Roadmap Fase 1.2: `data/patients/p_001/events/` | Sin multi-paciente |
| Roadmap Fase 1.3: `POST /patients/{id}/events`, `GET /events/{id}` | Convención real `/api/v1/...` (`MeasurementController` → `/api/v1/measurements`) |
| Roadmap Fase 1.4: UI para seleccionar paciente, crear y listar eventos | Frontend Next.js existente; el chat no existe todavía |
| MVP §11: UI solo conversacional | `/` es hoy el dashboard de mediciones (`frontend/src/app/page.tsx`) |
| MVP §4/§5: ids `evt_001` sin UUID | `measurements.id` es UUID (`gen_random_uuid()`) |
| MVP §12 diagrama: app monolítica con Markdown + SQLite | Dos servicios (Next.js + Spring Boot) + PostgreSQL; `docker-compose.yml` |
| MVP §19: "evolucionar hacia PostgreSQL" | PostgreSQL ya está presente desde el MVP |
| README raíz: "no consume APIs externas"; "mediciones de solo lectura" | Habrá proveedor LLM; UC-002 y UC-003 ya añadieron POST e importación |

## Steps

1. **Fase 1 — Roadmap** (`docs/roadmap/roadmap_asistente_historia_clinica.md`): §3.1 fijar Markdown en `data/events/` con índice derivado y alinear el front matter de ejemplo con los campos finales; §3.3 situar las herramientas semánticas en el backend Spring Boot y el *function calling* compatible con OpenAI; §3.4 dejar el retrieval híbrido como evolución futura y el MVP en full-text con configuración `simple`; Fase 0.4 añadir la regla "todo evento escrito queda indexado", la convención `evt_NNN.md` y los tres campos temporales; Fase 1.2 pasar a `data/events/` como volumen y aclarar que PostgreSQL es índice derivado; Fase 1.3 adoptar `/api/v1/clinical-events` y separar `/api/v1/measurements`; Fase 1.4 sustituir "seleccionar paciente" por la vista anexa en `/measurements`; Fase 2 fijar Spring Boot, variable de entorno, prompts en `src/main/resources/prompts/` y JSON sin streaming; Fase 3.1 y 3.2 pasar de SQLite a `tsvector` + GIN con configuración `simple`, alinear los campos del índice sin `patient_id` y describir la doble reconciliación; añadir Fase 3.6 "Persistencia del historial de conversación" como capacidad diferida; §5 redibujar el diagrama del MVP y revisar "etapa intermedia"; §6 P0 cambiar "SQLite FTS" por "índice full-text en PostgreSQL".

2. **Fase 2 — Alcance del MVP** (`docs/roadmap/mvp_alcance_asistente_historia_clinica.md`), *paralela con 1*: §2 y §3.1 añadir el indexado junto al Markdown; §4.1, §4.3 y §5 formalizar `code`, `date_precision` y `date_text`; §6 eliminar la frase que descarta las bases de datos relacionales y explicitar ubicación, volumen y quién escribe los ficheros; §7 reemplazar SQLite FTS5, retirar `patient_id` y describir la reconciliación; §8 y §9 ubicar las herramientas y la inyección de la fecha actual en el backend, sin streaming; §11 fijar `/` y `/measurements` y diferir la vista de depuración (UC-011); §12 y §13 redibujar la arquitectura y sustituir la estructura Python por la real; §14 dejar de excluir PostgreSQL y añadir como exclusión explícita la unificación con `measurements`; §15, §16, §18, §19 y §20 actualizar Markdown/SQLite, el DoD, el principio de fuente de verdad y el resumen ejecutivo.

3. **Fase 3 — Use cases** (`docs/roadmap/use-cases.md`), *paralela con 1 y 2*: renumerar conservando el orden actual — UC-01→UC-004 (Registrar un evento clínico), UC-02→UC-005 (Consultar un evento clínico), UC-03→UC-006 (Buscar información en la historia clínica), UC-04→UC-007 (Consultar la historia clínica mediante lenguaje natural), UC-05→UC-008 (Gestionar información clínica ausente), UC-06→UC-009 (Registrar información temporalmente imprecisa), UC-07→UC-010 (Consultar información que no pertenece a la historia clínica), UC-08→UC-011 (Inspeccionar los eventos clínicos registrados) — y añadir la nota de cabecera que explica que UC-001..UC-003 corresponden al MVP de seguimiento corporal ya implementado y que los documentos formales se redactarán antes de implementar cada UC.

4. **Fase 4 — README** (`README.md`), *paralela con 1, 2 y 3*: corregir lo ya falso ("Measurements are read-only from the application…", la ausencia de endpoints de escritura, la inserción manual con `psql`), matizar "consumes **no external or third-party APIs**" anticipando el proveedor LLM sin afirmar que exista, y añadir una nota breve de dirección del producto que apunte a `docs/roadmap/`. Sin reescribirlo para describir un asistente inexistente.

5. **Fase 5 — Verificación**, *depende de 1 a 4*: pasada de términos obsoletos, coherencia de la ubicación/volumen/variables entre los cuatro documentos y contraste final contra los hechos del repositorio.

## Relevant files

- `docs/roadmap/roadmap_asistente_historia_clinica.md` — §3, §4 (Fases 0-3), §5 y §6.
- `docs/roadmap/mvp_alcance_asistente_historia_clinica.md` — §2, §3.1, §4 a §9, §11 a §14, §15, §16, §18, §19 y §20.
- `docs/roadmap/use-cases.md` — encabezados UC-01..UC-08 y nota de cabecera.
- `README.md` — "Project description", "Use of APIs or external services", "Basic usage" y "Additional notes".
- Solo lectura, como fuente de verdad de lo que existe: `docker-compose.yml`, `backend/src/main/resources/db/migration/V1__create_measurements_table.sql`, `backend/src/main/java/com/careme/backend/controller/MeasurementController.java`, `frontend/src/app/page.tsx`, `docs/standards/java-springboot-standards.md`, `docs/standards/next-standards.md`, `docs/use-cases/UC-001.md`..`UC-003.md`.

## Verification

1. Buscar en `docs/roadmap/` los términos `SQLite`, `FTS5`, `patient_id`, `data/patients`, `main.py`, `markdown_store.py`, `search_index.py`, `.py` y `microservicios`, y confirmar que solo sobreviven en exclusiones o fases futuras justificadas.
2. Confirmar que §14 del MVP ya no excluye PostgreSQL, que §18 y §19 son coherentes con "Markdown = verdad, PostgreSQL = índice reconstruible" y que el README no contradice a ninguno.
3. Comprobar que `data/events/`, el volumen en `docker-compose.yml` y las variables de entorno del proveedor LLM se citan de forma consistente entre roadmap, MVP y README.
4. Comprobar que las referencias internas (§ y enlaces entre documentos) siguen resolviendo tras las ediciones.
5. Verificar el mapeo `UC-004..UC-011`: contiguo, sin duplicados, en el mismo orden que las descripciones y sin referencias cruzadas a la numeración antigua.
6. Contraste contra el repositorio: `/api/v1/measurements` en `MeasurementController`, la ruta anexa frente a `frontend/src/app/page.tsx`, y que la estructura citada coincida con los estándares de backend y frontend.
7. Lectura completa de los cuatro documentos editados.

## Decisions

- Alcance limitado a documentación: no se implementa código, no se crean `docs/use-cases/UC-004..UC-011.md`, no se tocan `docs/use-cases/UC-001..003.md`, `docs/standards/`, `backend/README.md` ni `frontend/README.md`.
- No se modifican esquema, migraciones, `docker-compose.yml` ni endpoints existentes: el plan describe lo que habrá que cambiar después, no lo cambia.
- Los documentos describen el estado objetivo y no afirman que el asistente ya exista en el repositorio.
- Fuera de alcance: crear los documentos formales de los use cases, implementar cualquier parte del asistente y reorganizar las rutas del frontend.
