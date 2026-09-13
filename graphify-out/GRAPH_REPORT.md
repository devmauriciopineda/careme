# Graph Report - careme  (2026-09-12)

## Corpus Check
- 105 files · ~52,448 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 935 nodes · 1728 edges · 63 communities (55 shown, 8 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 146 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `84223a1f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MeasurementCsvParser
- Measurement
- schema.ts
- actions.ts
- metrics.ts
- org.junit.jupiter.api.Test
- MeasurementServiceTest
- dependencies
- compilerOptions
- Careme Frontend README
- devDependencies
- ErrorResponse
- components.json
- Spring Boot Backend
- Backend Project Standards
- Scenarios
- mvnw
- Careme
- Careme Backend README
- MeasurementImportService
- application.yml
- CorsConfig.java
- CaremeBackendApplication
- MeasurementForm.tsx
- V1__create_measurements_table.sql
- eslint.config.mjs
- next.config.ts
- postcss.config.mjs
- com.careme:backend
- strings.ts
- Estructura obligatoria del `architecture.md`
- Especificación de alcance — MVP del asistente de historia clínica personal
- measurementService.test.ts
- architecture.md — Careme
- Modelo de datos — Careme
- Use Cases — MVP
- chart.tsx
- MeasurementImport.tsx
- Frontend Project Standards
- Subfases
- Subfases
- Subfases
- 2. Alcance funcional proyectado
- Subfases
- Subfases
- Subfases
- Subfases
- Subfases
- 15. Casos de prueba conversacionales
- Subfases
- Plan: Alinear roadmap, MVP, use cases y README con el repositorio
- 14. Exclusiones explícitas del MVP
- roadmap_asistente_historia_clinica.md
- 10. Reglas de comportamiento
- 3. Principios de arquitectura
- 3. Alcance funcional
- 4. Modelo de datos mínimo
- 8. Herramientas del LLM
- 9. Responsabilidades del LLM y del backend
- 7. Búsqueda

## God Nodes (most connected - your core abstractions)
1. `MeasurementCsvParserTest` - 29 edges
2. `Measurement` - 27 edges
3. `MeasurementJpaRepositoryTest` - 22 edges
4. `MeasurementDraft` - 21 edges
5. `Especificación de alcance — MVP del asistente de historia clínica personal` - 21 edges
6. `MeasurementCsvParser` - 20 edges
7. `MeasurementEntity` - 18 edges
8. `MeasurementRepository` - 18 edges
9. `MeasurementImportControllerTest` - 18 edges
10. `MeasurementImportService` - 17 edges

## Surprising Connections (you probably didn't know these)
- `Response Envelope Pattern` --semantically_similar_to--> `ApiResponse Envelope`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `Layered Architecture (Standard)` --semantically_similar_to--> `Layered Architecture (Backend)`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → backend/README.md
- `JaCoCo 90% Gate` --semantically_similar_to--> `JaCoCo Coverage Gate`  [INFERRED] [semantically similar]
  backend/README.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `90% Coverage Threshold` --semantically_similar_to--> `JaCoCo Coverage Gate`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `Server Components Default` --semantically_similar_to--> `Server Components`  [INFERRED] [semantically similar]
  docs/standards/next-standards.md → .github/prompts/plan-caremeMeasurements.prompt.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Backend Measurement Flow** — _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_controller, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_service, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_repository, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_file_repository [EXTRACTED 1.00]
- **Frontend Measurement Feature** — _github_prompts_plan_carememeasurements_prompt_measurements_dashboard, _github_prompts_plan_carememeasurements_prompt_metric_trend_chart, _github_prompts_plan_carememeasurements_prompt_measurements_table, _github_prompts_plan_carememeasurements_prompt_measurement_service, _github_prompts_plan_carememeasurements_prompt_metrics_lib [EXTRACTED 1.00]
- **JPA Persistence Stack** — _github_prompts_plan_postgrespersistence_prompt_measurement_entity, _github_prompts_plan_postgrespersistence_prompt_measurement_jpa_dao, _github_prompts_plan_postgrespersistence_prompt_measurement_jpa_repository, _github_prompts_plan_postgrespersistence_prompt_flyway, readme_postgresql [EXTRACTED 1.00]

## Communities (63 total, 8 thin omitted)

### Community 0 - "MeasurementCsvParser"
Cohesion: 0.06
Nodes (33): MeasurementController, ApiResponse, ImportPreviewResponse, ImportPreviewRow, ImportResultResponse, MeasurementRequest, MeasurementResponse, MeasurementImportException (+25 more)

### Community 1 - "Measurement"
Cohesion: 0.07
Nodes (20): Measurement, Measurement, MeasurementEntity, MeasurementJpaDao, MeasurementJpaRepository, CaremeBackendApplicationTests, PostgresIntegrationTest, MeasurementJpaRepositoryTest (+12 more)

### Community 2 - "schema.ts"
Cohesion: 0.12
Nodes (22): todayIsoDate(), importErrorResponseSchema, importPreviewResponseSchema, importPreviewRowSchema, importResultResponseSchema, measurementCreatedResponseSchema, measurementFormSchema, measurementInputSchema (+14 more)

### Community 3 - "actions.ts"
Cohesion: 0.13
Nodes (21): ConfirmImportResult, confirmMeasurementImport(), ImportErrorInfo, PreviewImportResult, previewMeasurementImport(), RegisterMeasurementResult, toImportError(), uploadedFile() (+13 more)

### Community 4 - "metrics.ts"
Cohesion: 0.12
Nodes (30): Card(), CardContent(), CardDescription(), CardHeader(), CardTitle(), Separator(), CHART_DESCRIPTIONS, MeasurementsDashboard() (+22 more)

### Community 5 - "org.junit.jupiter.api.Test"
Cohesion: 0.08
Nodes (6): MeasurementDraft, MeasurementControllerTest, MeasurementDraftTest, MeasurementTest, MeasurementCsvParserTest, org.junit.jupiter.api.Test

### Community 7 - "dependencies"
Cohesion: 0.06
Nodes (35): class-variance-authority, cn, dependencies, class-variance-authority, cn, lucide-react, next, radix-ui (+27 more)

### Community 8 - "compilerOptions"
Cohesion: 0.06
Nodes (31): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+23 more)

### Community 9 - "Careme Frontend README"
Cohesion: 0.16
Nodes (20): measurementService, MeasurementsDashboard, measurements.mock.ts, MeasurementsTable, MetricTrendChart, metrics.ts, METRICS Registry, Recharts (+12 more)

### Community 10 - "devDependencies"
Cohesion: 0.07
Nodes (29): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, jsdom, tailwindcss, @tailwindcss/postcss (+21 more)

### Community 11 - "ErrorResponse"
Cohesion: 0.21
Nodes (12): ErrorDetail, ErrorResponse, ApiExceptionHandler, ApiExceptionHandlerTest, org.springframework.http.converter.HttpMessageNotReadableException, org.springframework.http.ResponseEntity, org.springframework.validation.FieldError, org.springframework.web.bind.annotation.ExceptionHandler (+4 more)

### Community 12 - "components.json"
Cohesion: 0.09
Nodes (21): aliases, components, hooks, lib, ui, utils, iconLibrary, menuAccent (+13 more)

### Community 13 - "Spring Boot Backend"
Cohesion: 0.23
Nodes (12): API_BASE_URL, Multi-stage Dockerfile, Spring Boot Backend, Backend Service, Frontend Service, Postgres Service, Containerized Stack, Next.js Agent Rules (+4 more)

### Community 14 - "Backend Project Standards"
Cohesion: 0.20
Nodes (11): JaCoCo Coverage Gate, JaCoCo 90% Gate, Layered Architecture (Backend), Backend Project Standards, Bean Validation, 90% Coverage Threshold, Domain-Driven Design, Layered Architecture (Standard) (+3 more)

### Community 15 - "Scenarios"
Cohesion: 0.07
Nodes (27): 10. Fuera de alcance, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones, 7. Postcondiciones (+19 more)

### Community 16 - "mvnw"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 17 - "Careme"
Cohesion: 0.31
Nodes (9): graphify, CSV Measurement Import, MeasurementImport.tsx, Register Daily Measurement (UC-002), UC-001 Ver el seguimiento corporal, UC-002 Registrar la medición del día, UC-003 Cargar mediciones desde un archivo, Body Tracking Application (+1 more)

### Community 18 - "Careme Backend README"
Cohesion: 0.19
Nodes (19): ApiExceptionHandler, ApiResponse Envelope, ErrorResponse, GET /api/v1/measurements, Measurement, MeasurementController, MeasurementFileRepository, MeasurementRepository (+11 more)

### Community 19 - "MeasurementImportService"
Cohesion: 0.40
Nodes (5): All-or-nothing Transaction, Apache Commons CSV, MeasurementCsvParser, MeasurementDraft, MeasurementImportService

### Community 20 - "application.yml"
Cohesion: 0.20
Nodes (11): Flyway Migrations, PostgreSQL Persistence, Testcontainers, UNIQUE(date) Constraint, UUID Primary Key, V1__create_measurements_table.sql, PostgresIntegrationTest, Dev Profile Configuration (+3 more)

### Community 21 - "CorsConfig.java"
Cohesion: 0.43
Nodes (5): CorsConfig, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.WebMvcConfigurer, WebMvcConfigurer

### Community 23 - "MeasurementForm.tsx"
Cohesion: 0.12
Nodes (14): Button(), buttonVariants, Input(), Label(), registerMeasurement(), FieldErrors, MeasurementForm(), handleSubmit() (+6 more)

### Community 32 - "strings.ts"
Cohesion: 0.16
Nodes (10): geistMono, geistSans, metadata, metadata, MEASUREMENTS, describeImportError(), describeIssue(), FIELD_LABELS (+2 more)

### Community 33 - "Estructura obligatoria del `architecture.md`"
Cohesion: 0.12
Nodes (15): 1. Resumen ejecutivo, 2. Contexto y alcance, 3. Stack tecnológico, 4. Vista de contenedores (C4 nivel 2), 5. Vista de componentes (C4 nivel 3), 6. Mapa del repositorio, 7. Patrones arquitectónicos, 8. Decisiones arquitectónicas (ADR-light) (+7 more)

### Community 34 - "Especificación de alcance — MVP del asistente de historia clínica personal"
Cohesion: 0.14
Nodes (13): 11. Interfaz de usuario, 12. Arquitectura, 13. Estructura de proyecto, 16. Definition of Done, 17. Criterio técnico de éxito, 18. Principios de diseño, 19. Evolución posterior, 1. Propósito (+5 more)

### Community 35 - "measurementService.test.ts"
Cohesion: 0.14
Nodes (8): ImportRejectedError, measurementService, CREATED_RESPONSE, IMPORT_PREVIEW_RESPONSE, IMPORT_RESULT_RESPONSE, INPUT, SUCCESSFUL_RESPONSE, UNKNOWN_IMPORT_ERROR

### Community 36 - "architecture.md — Careme"
Cohesion: 0.15
Nodes (12): 1. Resumen ejecutivo, 2. Contexto y alcance, 3. Stack tecnológico, 4. Vista de contenedores (C4 nivel 2), 5.1 Backend (contenedor más complejo), 5.2 Frontend, 5. Vista de componentes (C4 nivel 3), 6. Mapa del repositorio (+4 more)

### Community 37 - "Modelo de datos — Careme"
Cohesion: 0.15
Nodes (12): 1.1 Measurement, 1.2 MeasurementDraft, 1.3 Modelos de la API, 1.4 Reglas transversales de la importación, 1. Modelo implementado, 2.1 Patient, 2.2 ClinicalEvent, 2. Modelo propuesto — asistente de historia clínica (+4 more)

### Community 38 - "Use Cases — MVP"
Cohesion: 0.15
Nodes (12): UC-001 — Ver el seguimiento corporal, UC-002 — Registrar la medición del día, UC-003 — Cargar mediciones desde un archivo, UC-004 — Registrar un evento clínico, UC-005 — Consultar un evento clínico, UC-006 — Buscar información en la historia clínica, UC-007 — Consultar la historia clínica mediante lenguaje natural, UC-008 — Gestionar información clínica ausente (+4 more)

### Community 39 - "chart.tsx"
Cohesion: 0.21
Nodes (11): ChartConfig, ChartContainer(), ChartContext, ChartContextProps, ChartLegendContent(), ChartTooltipContent(), getPayloadConfigFromPayload(), INITIAL_DIMENSION (+3 more)

### Community 40 - "MeasurementImport.tsx"
Cohesion: 0.38
Nodes (8): Table(), TableBody(), TableCaption(), TableCell(), TableHead(), TableHeader(), TableRow(), MeasurementsTableProps

### Community 41 - "Frontend Project Standards"
Cohesion: 0.18
Nodes (11): Careme MVP (frontend-only), Server Components, Frontend Project Standards, Playwright, pnpm Workspaces, Server Components Default, Storybook, TanStack Query (+3 more)

### Community 42 - "Subfases"
Cohesion: 0.18
Nodes (11): 3.1. Búsqueda textual, 3.2. Índice, 3.3. Tool de búsqueda, 3.4. Retrieval controlado, 3.5. Respuestas basadas en evidencia, 3.6. Persistencia del historial de conversación (diferida), Criterio de éxito del MVP, Fase 3 — Consulta mediante lenguaje natural (+3 more)

### Community 43 - "Subfases"
Cohesion: 0.20
Nodes (10): 8.1. Identidad y acceso, 8.2. Protección de datos, 8.3. Auditoría, 8.4. Versionado, 8.5. Seguridad del agente, 8.6. Privacidad y regulación, Fase 8 — Seguridad, auditoría y producción, Objetivo (+2 more)

### Community 44 - "Subfases"
Cohesion: 0.22
Nodes (9): 0.1. Definir entidades mínimas, 0.2. Definir tipos de evento, 0.3. Definir metadatos, 0.4. Definir formato Markdown, 4. Roadmap, Fase 0 — Definición del modelo mínimo, Objetivo, Resultado (+1 more)

### Community 45 - "2. Alcance funcional proyectado"
Cohesion: 0.22
Nodes (9): 1. Objeto del aplicativo, 2.1. Registro de información clínica, 2.2. Consulta de la historia, 2.3. Síntesis mediante IA, 2.4. Evidencia y trazabilidad, 2.5. Seguridad y operación, 2. Alcance funcional proyectado, Principio arquitectónico fundamental (+1 more)

### Community 46 - "Subfases"
Cohesion: 0.22
Nodes (9): 2.1. Integración del modelo, 2.2. Function calling / tools, 2.3. Extracción de información, 2.4. Control de incertidumbre, 2.5. Validación, Fase 2 — Integración del LLM como interfaz, Objetivo, Resultado (+1 more)

### Community 47 - "Subfases"
Cohesion: 0.22
Nodes (9): 4.1. Nuevos tipos de evento, 4.2. Condiciones longitudinales, 4.3. Estados, 4.4. Relaciones, 4.5. Provenance, Fase 4 — Modelo clínico enriquecido, Objetivo, Resultado (+1 more)

### Community 48 - "Subfases"
Cohesion: 0.22
Nodes (9): 5.1. Entidad Document, 5.2. Object storage, 5.3. Extracción, 5.4. Representación Markdown, 5.5. Relación evidencia-hecho, Fase 5 — Documentos y evidencia, Objetivo, Resultado (+1 more)

### Community 49 - "Subfases"
Cohesion: 0.22
Nodes (9): 6.1. Embeddings, 6.2. Vector search, 6.3. Retrieval híbrido, 6.4. Ranking, 6.5. Evaluación, Fase 6 — Retrieval semántico y RAG, Objetivo, Resultado (+1 more)

### Community 50 - "Subfases"
Cohesion: 0.22
Nodes (9): 7.1. Timeline, 7.2. Resúmenes por condición, 7.3. Comparación de mediciones, 7.4. Correlación temporal, 7.5. Síntesis con evidencia, Fase 7 — Inteligencia longitudinal, Objetivo, Resultado (+1 more)

### Community 51 - "15. Casos de prueba conversacionales"
Cohesion: 0.25
Nodes (8): 15. Casos de prueba conversacionales, Caso 1 — Crear diagnóstico, Caso 2 — Crear medición, Caso 3 — Consultar mediciones, Caso 4 — Consulta temporal, Caso 5 — Información inexistente, Caso 6 — Fecha aproximada, Caso 7 — Pregunta general

### Community 52 - "Subfases"
Cohesion: 0.25
Nodes (8): 1.1. Backend mínimo, 1.2. Persistencia, 1.3. API, 1.4. Interfaz mínima, Fase 1 — MVP técnico de almacenamiento, Objetivo, Resultado, Subfases

### Community 53 - "Plan: Alinear roadmap, MVP, use cases y README con el repositorio"
Cohesion: 0.25
Nodes (7): Conflictos detectados (documento → repositorio), Decisiones que deben quedar escritas en los documentos, Decisions, Plan: Alinear roadmap, MVP, use cases y README con el repositorio, Relevant files, Steps, Verification

### Community 54 - "14. Exclusiones explícitas del MVP"
Cohesion: 0.29
Nodes (7): 14. Exclusiones explícitas del MVP, Datos clínicos, Documentos y fuentes externas, IA avanzada, Infraestructura, Integración con el seguimiento corporal, Producto

### Community 55 - "roadmap_asistente_historia_clinica.md"
Cohesion: 0.29
Nodes (6): 5. Arquitectura evolutiva, 6. Priorización, 7. Criterio general de evolución, Arquitectura avanzada, Etapa intermedia, MVP

### Community 56 - "10. Reglas de comportamiento"
Cohesion: 0.33
Nodes (6): 10.1 No inventar, 10.2 No guardar inferencias como hechos, 10.3 Retrieval antes de responder, 10.4 No encontrar información, 10.5 Información reciente, 10. Reglas de comportamiento

### Community 57 - "3. Principios de arquitectura"
Cohesion: 0.40
Nodes (5): 3.1. Markdown como representación canónica inicial, 3.2. Los eventos son la fuente primaria, 3.3. Separación entre almacenamiento e inteligencia, 3.4. Retrieval híbrido, 3. Principios de arquitectura

### Community 58 - "3. Alcance funcional"
Cohesion: 0.50
Nodes (4): 3.1 Registro de información, 3.2 Consulta de información, 3.3 Preguntas que no requieren recuperación, 3. Alcance funcional

### Community 59 - "4. Modelo de datos mínimo"
Cohesion: 0.50
Nodes (4): 4.1 ClinicalEvent, 4.2 Tipos de evento iniciales, 4.3 Ejemplo de evento, 4. Modelo de datos mínimo

### Community 60 - "8. Herramientas del LLM"
Cohesion: 0.50
Nodes (4): 8.1 create_event, 8.2 search_events, 8.3 get_event y list_events, 8. Herramientas del LLM

### Community 61 - "9. Responsabilidades del LLM y del backend"
Cohesion: 0.67
Nodes (3): 9. Responsabilidades del LLM y del backend, Backend, LLM

## Knowledge Gaps
- **347 isolated node(s):** `com.careme:backend`, `measurements`, `$schema`, `style`, `rsc` (+342 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Careme Backend README` connect `Careme Backend README` to `Careme Frontend README`, `application.yml`, `Backend Project Standards`, `Careme`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Why does `Measurement` connect `Measurement` to `MeasurementCsvParser`, `org.junit.jupiter.api.Test`, `MeasurementServiceTest`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Why does `MeasurementDraft` connect `org.junit.jupiter.api.Test` to `MeasurementCsvParser`, `Measurement`?**
  _High betweenness centrality (0.005) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `Measurement` (e.g. with `.acceptsACompleteMeasurement()` and `.rejectsAMissingDate()`) actually correct?**
  _`Measurement` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 6 inferred relationships involving `MeasurementDraft` (e.g. with `.keepsTheGivenValues()` and `.rejectsAMissingDate()`) actually correct?**
  _`MeasurementDraft` has 6 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.careme:backend`, `measurements`, `$schema` to the rest of the system?**
  _347 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MeasurementCsvParser` be split into smaller, more focused modules?**
  _Cohesion score 0.0593505039193729 - nodes in this community are weakly interconnected._