# Graph Report - careme  (2026-09-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1807 nodes · 3274 edges · 131 communities (118 shown, 13 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 278 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `83a7611c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MeasurementImportControllerTest
- Measurement
- measurements/lib/schema.ts
- measurements/actions.ts
- metrics.ts
- .query
- 3. Principios de arquitectura
- dependencies
- compilerOptions
- Careme Frontend README
- devDependencies
- org.springframework.http.ResponseEntity
- components.json
- Spring Boot Backend
- Backend Project Standards
- Scenarios
- mvnw
- Careme
- Careme Backend README
- MeasurementImportService
- application.yml
- .configurationForApiMapping
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
- Scenarios
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
- ADDED Requirements
- 3. Alcance funcional
- 4. Modelo de datos mínimo
- 8. Herramientas del LLM
- 9. Responsabilidades del LLM y del backend
- 7. Búsqueda
- Requirements
- ADDED Requirements
- org.slf4j.Logger
- ClinicalEventMarkdownStore
- ClinicalEventIntent
- Decisions
- ClinicalEvent
- openspec-explore/SKILL.md
- opsx-explore.prompt.md
- scripts
- 2026-09-13-uc-004/proposal.md
- Kind
- Requirement: Confirmar el resultado y mantener atomicidad ante fallos
- package.json
- 2026-09-13-uc-004/tasks.md
- .search
- V2__create_clinical_event_index.sql
- @testing-library/jest-dom
- @tailwindcss/postcss
- @testing-library/react
- @types/react
- MeasurementCsvParser
- MeasurementRepository
- MeasurementImportService
- ChatWorkspace.test.tsx
- MeasurementImportServiceTest
- Requirement: Return explicit chat outcomes
- MeasurementImportControllerTest.java
- Requirement: Return explicit chat outcomes
- Requirement: Produce provider-neutral clinical intents
- Requirement: Produce provider-neutral clinical intents
- Decisions
- Requirement: Represent clarification, success, duplicate, and failure states
- .register
- Requirement: Represent clarification, success, duplicate, and failure states
- Plan: Cerrar contratos del asistente clínico
- 2026-09-13-assistant-chat-llm-integration/proposal.md
- Requirement: Register clinical events through the conversation boundary
- 2026-09-13-assistant-chat-llm-integration/tasks.md
- LlmIntegrationException
- org.junit.jupiter.api.Test
- org.springframework.stereotype.Service
- Decisions
- 2026-09-13-chat-server-side-transport/design.md
- Requirement: Represent clarification, success, duplicate, and failure states
- 2026-09-13-chat-server-side-transport/proposal.md
- 2026-09-13-fix-clinical-event-storage/proposal.md
- 2026-09-13-fix-clinical-event-storage/tasks.md
- Requirement: Allow the declared origin to reach the operations a browser client uses
- 2026-09-13-chat-server-side-transport/tasks.md
- Requirement: Produce provider-neutral clinical intents
- ClinicalEventQueryRepository
- Requirement: Return explicit chat outcomes
- Decisions
- Requirement: Represent clarification, success, duplicate, and failure states
- doc-sync.prompt.md
- uc-007/tasks.md
- uc-007/proposal.md

## God Nodes (most connected - your core abstractions)
1. `ClinicalEvent` - 74 edges
2. `ClinicalEventIntent` - 33 edges
3. `MeasurementCsvParserTest` - 29 edges
4. `ClinicalEventMarkdownStore` - 28 edges
5. `Measurement` - 27 edges
6. `MeasurementJpaRepositoryTest` - 22 edges
7. `MeasurementDraft` - 21 edges
8. `Especificación de alcance — MVP del asistente de historia clínica personal` - 21 edges
9. `MeasurementCsvParser` - 20 edges
10. `ClinicalEventRegistrationService` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Response Envelope Pattern` --semantically_similar_to--> `ApiResponse Envelope`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `JaCoCo 90% Gate` --semantically_similar_to--> `JaCoCo Coverage Gate`  [INFERRED] [semantically similar]
  backend/README.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `90% Coverage Threshold` --semantically_similar_to--> `JaCoCo Coverage Gate`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → .github/prompts/plan-backendSpringbootMeasurements.prompt.md
- `Layered Architecture (Standard)` --semantically_similar_to--> `Layered Architecture (Backend)`  [INFERRED] [semantically similar]
  docs/standards/java-springboot-standards.md → backend/README.md
- `Server Components Default` --semantically_similar_to--> `Server Components`  [INFERRED] [semantically similar]
  docs/standards/next-standards.md → .github/prompts/plan-caremeMeasurements.prompt.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Backend Measurement Flow** — _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_controller, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_service, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_repository, _github_prompts_plan_backendspringbootmeasurements_prompt_measurement_file_repository [EXTRACTED 1.00]
- **Frontend Measurement Feature** — _github_prompts_plan_carememeasurements_prompt_measurements_dashboard, _github_prompts_plan_carememeasurements_prompt_metric_trend_chart, _github_prompts_plan_carememeasurements_prompt_measurements_table, _github_prompts_plan_carememeasurements_prompt_measurement_service, _github_prompts_plan_carememeasurements_prompt_metrics_lib [EXTRACTED 1.00]
- **JPA Persistence Stack** — _github_prompts_plan_postgrespersistence_prompt_measurement_entity, _github_prompts_plan_postgrespersistence_prompt_measurement_jpa_dao, _github_prompts_plan_postgrespersistence_prompt_measurement_jpa_repository, _github_prompts_plan_postgrespersistence_prompt_flyway, readme_postgresql [EXTRACTED 1.00]

## Communities (131 total, 13 thin omitted)

### Community 0 - "MeasurementImportControllerTest"
Cohesion: 0.26
Nodes (3): MeasurementImportControllerTest, MockMultipartFile, org.springframework.mock.web.MockMultipartFile

### Community 1 - "Measurement"
Cohesion: 0.07
Nodes (16): Measurement, MeasurementDraft, Measurement, MeasurementEntity, MeasurementJpaDao, Override, MeasurementJpaRepository, MeasurementDraftTest (+8 more)

### Community 2 - "measurements/lib/schema.ts"
Cohesion: 0.12
Nodes (22): todayIsoDate(), importErrorResponseSchema, importPreviewResponseSchema, importPreviewRowSchema, importResultResponseSchema, measurementCreatedResponseSchema, measurementFormSchema, measurementInputSchema (+14 more)

### Community 3 - "measurements/actions.ts"
Cohesion: 0.13
Nodes (21): ConfirmImportResult, confirmMeasurementImport(), ImportErrorInfo, PreviewImportResult, previewMeasurementImport(), RegisterMeasurementResult, toImportError(), uploadedFile() (+13 more)

### Community 4 - "metrics.ts"
Cohesion: 0.10
Nodes (31): metadata, Card(), CardContent(), CardDescription(), CardHeader(), CardTitle(), Separator(), CHART_DESCRIPTIONS (+23 more)

### Community 5 - ".query"
Cohesion: 0.27
Nodes (5): Query, Scope, HISTORY, MEASUREMENTS, ClinicalEventIntentValidatorTest

### Community 6 - "3. Principios de arquitectura"
Cohesion: 0.40
Nodes (5): 3.1. Markdown como representación canónica inicial, 3.2. Los eventos son la fuente primaria, 3.3. Separación entre almacenamiento e inteligencia, 3.4. Retrieval híbrido, 3. Principios de arquitectura

### Community 7 - "dependencies"
Cohesion: 0.09
Nodes (23): class-variance-authority, cn, dependencies, class-variance-authority, cn, lucide-react, next, radix-ui (+15 more)

### Community 8 - "compilerOptions"
Cohesion: 0.06
Nodes (31): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+23 more)

### Community 9 - "Careme Frontend README"
Cohesion: 0.16
Nodes (20): measurementService, MeasurementsDashboard, measurements.mock.ts, MeasurementsTable, MetricTrendChart, metrics.ts, METRICS Registry, Recharts (+12 more)

### Community 10 - "devDependencies"
Cohesion: 0.10
Nodes (21): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, jsdom, tailwindcss, @testing-library/user-event (+13 more)

### Community 11 - "org.springframework.http.ResponseEntity"
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
Cohesion: 0.06
Nodes (33): 10. Fuera de alcance, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones, 7. Postcondiciones (+25 more)

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

### Community 21 - ".configurationForApiMapping"
Cohesion: 0.21
Nodes (9): CorsConfig, CorsConfigTest, InspectableCorsRegistry, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.web.cors.CorsConfiguration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer (+1 more)

### Community 23 - "MeasurementForm.tsx"
Cohesion: 0.12
Nodes (14): Button(), buttonVariants, Input(), Label(), registerMeasurement(), FieldErrors, MeasurementForm(), handleSubmit() (+6 more)

### Community 32 - "strings.ts"
Cohesion: 0.19
Nodes (9): geistMono, geistSans, metadata, MEASUREMENTS, describeImportError(), describeIssue(), FIELD_LABELS, rejection() (+1 more)

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
Nodes (12): 1.1 Measurement, 1.2 MeasurementDraft, 1.3 Modelos de la API, 1.4 Reglas transversales de la importación, 1. Modelo implementado, 2.1 Patient, 2.2 ClinicalEvent, 2. Modelo del asistente de historia clínica (implementado) (+4 more)

### Community 38 - "Scenarios"
Cohesion: 0.05
Nodes (41): UC-001 — Ver el seguimiento corporal, UC-002 — Registrar la medición del día, UC-003 — Cargar mediciones desde un archivo, UC-004 — Registrar un evento clínico, UC-005 — Consultar un evento clínico, UC-006 — Buscar información en la historia clínica, UC-007 — Consultar la historia clínica mediante lenguaje natural, UC-008 — Gestionar información clínica ausente (+33 more)

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

### Community 57 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Declarar explícitamente la ausencia de registros, Requirement: Informar fallos sin inventar una respuesta, Requirement: Interpretar preguntas de seguimiento con la conversación activa, Requirement: Mantener la historia intacta, Requirement: Mostrar los hechos que sustentan la respuesta, Requirement: Pedir aclaración ante una pregunta ambigua (+21 more)

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

### Community 63 - "Requirements"
Cohesion: 0.06
Nodes (34): clinical-event-registration Specification, Purpose, Requirement: Asignar códigos de evento únicos y no reutilizables, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Conservar la precisión temporal, Requirement: Evitar duplicados dentro de la conversación, Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown, Requirement: Rechazar contenido no registrable o ambiguo (+26 more)

### Community 64 - "ADDED Requirements"
Cohesion: 0.10
Nodes (19): ADDED Requirements, Purpose, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Conservar la precisión temporal, Requirement: Evitar duplicados dentro de la conversación, Requirement: Rechazar contenido no registrable o ambiguo, Requirement: Registrar hechos clínicos propios, Scenario: Confirmar un registro exitoso (+11 more)

### Community 65 - "org.slf4j.Logger"
Cohesion: 0.15
Nodes (7): ClinicalEventIndexRebuilder, ClinicalEventIndexStartupReconciler, org.slf4j.Logger, org.springframework.boot.context.event.ApplicationReadyEvent, org.springframework.context.event.EventListener, org.springframework.jdbc.core.JdbcTemplate, org.springframework.stereotype.Component

### Community 66 - "ClinicalEventMarkdownStore"
Cohesion: 0.12
Nodes (3): ClinicalEventMarkdownStore, ClinicalEventMarkdownStoreTest, org.springframework.beans.factory.annotation.Autowired

### Community 67 - "ClinicalEventIntent"
Cohesion: 0.05
Nodes (36): ChatMessageRequest, ChatMessageResponse, EventSummary, Status, ANSWERED, CLARIFICATION_REQUIRED, DUPLICATE, FAILED (+28 more)

### Community 68 - "Decisions"
Cohesion: 0.17
Nodes (11): Context, Contrato estructurado entre chat y registro, Decisions, Duplicación limitada a la conversación actual, Goals / Non-Goals, La aplicación valida antes de persistir, Markdown como fuente primaria y PostgreSQL como índice derivado, Migration Plan (+3 more)

### Community 69 - "ClinicalEvent"
Cohesion: 0.15
Nodes (8): value(), ClinicalEvent, EventSource, PATIENT, ClinicalEventConversationRegistry, ClinicalEventRegistrationResult, com.fasterxml.jackson.annotation.JsonValue, java.util.regex.Pattern

### Community 70 - "openspec-explore/SKILL.md"
Cohesion: 0.18
Nodes (10): Check for context, Ending Discovery, Guardrails, Handling Different Entry Points, OpenSpec Awareness, The Stance, What You Don't Have To Do, What You Might Do (+2 more)

### Community 71 - "opsx-explore.prompt.md"
Cohesion: 0.20
Nodes (9): Check for context, Ending Discovery, Guardrails, OpenSpec Awareness, The Stance, What You Don't Have To Do, What You Might Do, When a change exists (+1 more)

### Community 72 - "scripts"
Cohesion: 0.25
Nodes (8): scripts, build, dev, lint, start, test, test:watch, typecheck

### Community 73 - "2026-09-13-uc-004/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 74 - "Kind"
Cohesion: 0.33
Nodes (6): Kind, CLARIFICATION, CONVERSATION, DUPLICATE, FAILURE, REGISTERED

### Community 75 - "Requirement: Confirmar el resultado y mantener atomicidad ante fallos"
Cohesion: 0.10
Nodes (20): ADDED Requirements, MODIFIED Requirements, Requirement: Asignar códigos de evento únicos y no reutilizables, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown, Requirement: Registrar eventos clínicos mediante la frontera conversacional, Scenario: Configurar la ubicación de la fuente de verdad, Scenario: Confirmar un registro exitoso (+12 more)

### Community 76 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

### Community 77 - "2026-09-13-uc-004/tasks.md"
Cohesion: 0.40
Nodes (4): 1. Modelo y persistencia de eventos, 2. Interpretación y normalización, 3. Registro y coordinación conversacional, 4. Verificación de integración

### Community 78 - ".search"
Cohesion: 0.19
Nodes (5): ClinicalAnswerComposer, ComposedAnswer, Override, ClinicalEventQueryRepositoryTest, ClinicalHistoryQueryServiceTest

### Community 94 - "MeasurementCsvParser"
Cohesion: 0.23
Nodes (6): MeasurementRequest, MeasurementImportException, MeasurementCsvParser, jakarta.validation.ConstraintViolation, jakarta.validation.Validator, org.apache.commons.csv.CSVFormat

### Community 95 - "MeasurementRepository"
Cohesion: 0.16
Nodes (6): MeasurementResponse, MeasurementRepository, Measurement, MeasurementServiceTest, org.junit.jupiter.api.extension.ExtendWith, org.mockito.junit.jupiter.MockitoExtension

### Community 96 - "MeasurementImportService"
Cohesion: 0.18
Nodes (11): ChatController, ClinicalEventIndexController, MeasurementController, ApiResponse, MeasurementImportService, MeasurementService, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping (+3 more)

### Community 97 - "ChatWorkspace.test.tsx"
Cohesion: 0.08
Nodes (25): metadata, sendChatMessage(), SendChatResult, chatServiceMock, RESPONSE, ChatWorkspace(), recordFailure(), submit() (+17 more)

### Community 98 - "MeasurementImportServiceTest"
Cohesion: 0.29
Nodes (3): ParsedCsv, Measurement, MeasurementImportServiceTest

### Community 99 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.11
Nodes (18): assistant-conversation Specification, Purpose, Requirement: Allow the declared origin to reach the operations a browser client uses, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Requirements, Scenario: Accept a preflight for a browser write (+10 more)

### Community 100 - "MeasurementImportControllerTest.java"
Cohesion: 0.27
Nodes (3): ImportPreviewResponse, ImportPreviewRow, ImportResultResponse

### Community 101 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.13
Nodes (14): ADDED Requirements, Purpose, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Scenario: Continue a conversation after clarification, Scenario: Conversation state is lost after restart, Scenario: Reject an invalid chat request (+6 more)

### Community 102 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.14
Nodes (13): llm-clinical-intent-adapter Specification, Purpose, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Requirements, Scenario: Browser sends a chat message, Scenario: Map a clarification (+5 more)

### Community 103 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.15
Nodes (12): ADDED Requirements, Purpose, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Browser sends a chat message, Scenario: Map a clarification, Scenario: Map general conversation (+4 more)

### Community 104 - "Decisions"
Cohesion: 0.17
Nodes (11): Backend owns orchestration, Context, Decisions, Ephemeral state with bounded keys, Frontend client boundary, Goals / Non-Goals, Markdown plus derived PostgreSQL index, Migration Plan (+3 more)

### Community 105 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.15
Nodes (12): assistant-chat-interface Specification, Purpose, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Requirements, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Send a turn without exposing the API address (+4 more)

### Community 107 - ".register"
Cohesion: 0.11
Nodes (13): Candidate, DatePrecision, APPROXIMATE, EXACT, UNKNOWN, ClinicalEventDateNormalizer, NormalizedDate, ClinicalEventIndexWriter (+5 more)

### Community 108 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.20
Nodes (9): ADDED Requirements, Purpose, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Show request progress, Scenario: Submit a message (+1 more)

### Community 109 - "Plan: Cerrar contratos del asistente clínico"
Cohesion: 0.25
Nodes (7): Archivos relevantes, Consideraciones adicionales, Decisiones, Hallazgos, Pasos, Plan: Cerrar contratos del asistente clínico, Verificación

### Community 110 - "2026-09-13-assistant-chat-llm-integration/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 111 - "Requirement: Register clinical events through the conversation boundary"
Cohesion: 0.33
Nodes (5): ADDED Requirements, Requirement: Register clinical events through the conversation boundary, Scenario: Preserve atomic publication, Scenario: Rebuild the derived index, Scenario: Register through the chat flow

### Community 112 - "2026-09-13-assistant-chat-llm-integration/tasks.md"
Cohesion: 0.33
Nodes (5): 1. Backend conversation contract, 2. LLM intent adapter, 3. Clinical event integration, 4. Frontend chat interface, 5. Security and verification

### Community 117 - "org.junit.jupiter.api.Test"
Cohesion: 0.05
Nodes (15): ChatControllerTest, MeasurementControllerTest, ClinicalEventIntentTest, ClinicalEventTest, ConversationStateStoreTest, MeasurementCsvParserTest, OpenAiClinicalAnswerComposerTest, OpenAiClinicalIntentInterpreterTest (+7 more)

### Community 118 - "org.springframework.stereotype.Service"
Cohesion: 0.11
Nodes (16): FakeClinicalAnswerComposer, Builder, Override, Resource, OpenAiClinicalAnswerComposer, Builder, Resource, OpenAiClinicalIntentInterpreter (+8 more)

### Community 121 - "Decisions"
Cohesion: 0.15
Nodes (12): Context, Decisions, Event codes are derived from the persisted documents, Goals / Non-Goals, Migration Plan, Open Questions, Publication is create-only, Risks / Trade-offs (+4 more)

### Community 122 - "2026-09-13-chat-server-side-transport/design.md"
Cohesion: 0.17
Nodes (11): apiConfig keeps the local fallback but loses the public branch, chatService validates the response and normalizes its failures, Context, Decisions, Goals / Non-Goals, Migration Plan, Open Questions, Risks / Trade-offs (+3 more)

### Community 123 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.18
Nodes (10): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Send a turn without exposing the API address, Scenario: Show request progress, Scenario: Submit a message (+2 more)

### Community 125 - "2026-09-13-chat-server-side-transport/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 126 - "2026-09-13-fix-clinical-event-storage/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 127 - "2026-09-13-fix-clinical-event-storage/tasks.md"
Cohesion: 0.29
Nodes (6): 1. Configurable source-of-truth location, 2. Writable data directory in the image, 3. Durable Markdown source of truth, 4. Diagnosability, 5. Verification, 6. Event code allocation and non-destructive publication

### Community 128 - "Requirement: Allow the declared origin to reach the operations a browser client uses"
Cohesion: 0.40
Nodes (4): ADDED Requirements, Requirement: Allow the declared origin to reach the operations a browser client uses, Scenario: Accept a preflight for a browser write, Scenario: Keep unused verbs closed

### Community 129 - "2026-09-13-chat-server-side-transport/tasks.md"
Cohesion: 0.40
Nodes (4): 1. API reachability for a browser client, 2. Server-side chat transport, 3. Interface, 4. Verification

### Community 130 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.12
Nodes (16): ADDED Requirements, MODIFIED Requirements, Requirement: Compose grounded answers from retrieved events, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Compose from retrieved events, Scenario: Map a clarification, Scenario: Map a query (+8 more)

### Community 131 - "ClinicalEventQueryRepository"
Cohesion: 0.19
Nodes (9): ClinicalEventType, DIAGNOSIS, MEASUREMENT, MEDICATION, NOTE, ClinicalEventQueryRepository, java.sql.ResultSet, org.springframework.jdbc.core.RowMapper (+1 more)

### Community 132 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Scenario: Answer a follow-up question from recent turns, Scenario: Continue a conversation after clarification, Scenario: Conversation state is lost after restart, Scenario: Discard the ephemeral buffer after an interruption, Scenario: Return a clarification outcome (+6 more)

### Community 133 - "Decisions"
Cohesion: 0.14
Nodes (13): Búfer acotado de turnos recientes para el seguimiento, Context, Decisions, Dos operaciones del proveedor: clasificar y componer, El backend decide el enrutamiento; el adaptador solo propone, El índice y los prompts se versionan; los estados son aditivos, Goals / Non-Goals, La fundamentación se valida en el backend (+5 more)

### Community 134 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.15
Nodes (12): ADDED Requirements, MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Show the events that support an answer, Scenario: Continue after clarification, Scenario: Display the supporting events, Scenario: Keep the answer verifiable without technical detail, Scenario: Retry a failed request (+4 more)

### Community 136 - "doc-sync.prompt.md"
Cohesion: 0.18
Nodes (10): Alcance, Paso 1 — Identificar el cambio disparador, Paso 2 — Extraer los hechos del cambio, Paso 3 — Fuente de verdad (SDD), Paso 4 — Detectar drift por documento, Paso 4b — Estados en `docs/roadmap/use-cases.md` (solo estados), Paso 5 — Reporte (en el chat), Paso 6 — Aplicar correcciones con confirmación (+2 more)

### Community 138 - "uc-007/tasks.md"
Cohesion: 0.25
Nodes (7): 1. Contrato de intención de consulta y prompt de clasificación, 2. Recuperación en el índice derivado, 3. Respuesta fundada en los hechos recuperados, 4. Orquestación y contrato de chat, 5. Estado de conversación para preguntas de seguimiento, 6. Interfaz de chat, 7. Verificación integral

### Community 139 - "uc-007/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

## Knowledge Gaps
- **721 isolated node(s):** `ImportPreviewRow`, `FieldErrors`, `Status`, `ConfirmImportResult`, `ImportErrorInfo` (+716 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ClinicalEvent` connect `ClinicalEvent` to `org.slf4j.Logger`, `ClinicalEventMarkdownStore`, `ClinicalEventIntent`, `ClinicalEventQueryRepository`, `.register`, `.search`, `org.junit.jupiter.api.Test`, `org.springframework.stereotype.Service`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `ClinicalEventMarkdownStore` connect `ClinicalEventMarkdownStore` to `org.slf4j.Logger`, `ClinicalEventIntent`, `.register`, `org.springframework.stereotype.Service`?**
  _High betweenness centrality (0.008) - this node is a cross-community bridge._
- **Why does `ClinicalEventIntent` connect `ClinicalEventIntent` to `org.slf4j.Logger`, `.query`, `ClinicalEvent`, `.register`, `org.junit.jupiter.api.Test`, `org.springframework.stereotype.Service`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ClinicalEvent` (e.g. with `.acceptsAnApproximateEventWithoutInventedDate()` and `.acceptsAnExactPatientEvent()`) actually correct?**
  _`ClinicalEvent` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ImportPreviewRow`, `FieldErrors`, `Status` to the rest of the system?**
  _721 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Measurement` be split into smaller, more focused modules?**
  _Cohesion score 0.06564364876385337 - nodes in this community are weakly interconnected._
- **Should `measurements/lib/schema.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.12169312169312169 - nodes in this community are weakly interconnected._