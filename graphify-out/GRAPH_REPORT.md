# Graph Report - careme  (2026-09-18)

## Corpus Check
- 275 files · ~187,167 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2801 nodes · 5012 edges · 208 communities (179 shown, 29 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 443 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `34f26406`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MeasurementImportControllerTest
- Measurement
- measurements/lib/schema.ts
- measurements/actions.ts
- metrics.ts
- Requirements
- ClinicalEvent
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
- MeasurementImport.tsx
- Estructura obligatoria del `architecture.md`
- Especificación de alcance — MVP del asistente de historia clínica personal
- MetricTrendChart.tsx
- architecture.md — Careme
- Data model — Careme
- Scenarios
- chart.tsx
- 04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual
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
- ClinicalEventInspectionService
- ClinicalEventMarkdownStore
- .process
- Decisions
- .interpret
- openspec-explore/SKILL.md
- opsx-explore.prompt.md
- scripts
- 2026-09-13-uc-004/proposal.md
- 07 — Calidad y pruebas
- Requirement: Confirmar el resultado y mantener atomicidad ante fallos
- package.json
- 2026-09-13-uc-004/tasks.md
- .query
- V2__create_clinical_event_index.sql
- Requirement: Declarar explícitamente la ausencia de registros
- @tailwindcss/postcss
- @testing-library/react
- @types/react
- MeasurementCsvParser
- ClinicalEventIndexControllerTest
- ClinicalEventIndexController.java
- ChatWorkspace.test.tsx
- MeasurementImportService
- Requirement: Return explicit chat outcomes
- Decisions
- Requirement: Return explicit chat outcomes
- Requirement: Produce provider-neutral clinical intents
- Requirement: Produce provider-neutral clinical intents
- Decisions
- Requirement: Represent clarification, success, duplicate, and failure states
- Requirement: Produce provider-neutral clinical intents
- .register
- Requirement: Represent clarification, success, duplicate, and failure states
- Plan: Cerrar contratos del asistente clínico
- 2026-09-13-assistant-chat-llm-integration/proposal.md
- Requirement: Register clinical events through the conversation boundary
- 2026-09-13-assistant-chat-llm-integration/tasks.md
- LlmIntegrationException
- Requirement: Return explicit chat outcomes
- Requirement: Return explicit chat outcomes
- 01 — Estructura y arquitectura del repositorio
- MeasurementCsvParserTest
- org.springframework.stereotype.Service
- Decisions
- 02 — Frontend: renderizado y comunicación
- Decisions
- 2026-09-13-chat-server-side-transport/design.md
- Requirement: Represent clarification, success, duplicate, and failure states
- 08 — Despliegue y operación
- 2026-09-13-chat-server-side-transport/proposal.md
- 2026-09-13-fix-clinical-event-storage/proposal.md
- 2026-09-13-fix-clinical-event-storage/tasks.md
- Requirement: Allow the declared origin to reach the operations a browser client uses
- 2026-09-13-chat-server-side-transport/tasks.md
- Candidate
- Requirement: Represent clarification, success, duplicate, and failure states
- 03 — Backend: API, concurrencia y persistencia
- Requirement: Return explicit chat outcomes
- 3. Qué ocurre durante una consulta
- 06 — Desarrollo guiado por especificaciones y OpenSpec
- doc-sync.prompt.md
- README.md
- Requirement: Represent clarification, success, duplicate, and failure states
- 2026-09-13-uc-007/tasks.md
- 2026-09-13-uc-007/proposal.md
- 2026-09-16-uc-008/tasks.md
- Backend (Java / Spring Boot) — Careme
- ChatOrchestrator
- 05 — IA: LLM, prompts y RAG
- Frontend (Next.js / React / TypeScript) — Careme
- Requirement: Compose grounded answers from retrieved events
- org.junit.jupiter.api.Test
- backend-tests.instructions.md
- docs.instructions.md
- frontend-tests.instructions.md
- openspec-sdd.instructions.md
- 2026-09-16-uc-008/proposal.md
- Use Cases — MVP
- UC-008 — Gestionar información clínica ausente
- UC-010 — Consultar información que no pertenece a la historia clínica
- ChatMessageResponse
- UC-007 — Consultar la historia clínica mediante lenguaje natural
- Scenarios
- Scenarios
- UC-008 — Gestionar información clínica ausente — Acceptance Criteria
- OpenAiClinicalConversationComposerTest
- sync-concept-docs.prompt.md
- MeasurementRepository
- MeasurementDraft
- Decisions
- OpenAiClinicalIntentInterpreterTest
- Requirement: Produce provider-neutral clinical intents
- OpenAiClinicalAnswerComposerTest
- Requirement: Represent clarification, success, duplicate, and failure states
- Requirement: Reconocer las preguntas sobre la propia historia clínica
- .compose
- Requirement: Declarar explícitamente la ausencia de registros
- ClinicalConversationIntegrationTest
- 2026-09-17-uc-010/tasks.md
- 2026-09-17-uc-010/proposal.md
- Requirement: Reconocer las preguntas sobre la propia historia clínica
- Requirement: Redactar la respuesta solo con los hechos recuperados
- clinical-history-query Specification
- Requirement: Distinguir la ausencia de registros de un fallo de búsqueda
- Requirement: Mantener la historia intacta
- Requirement: Ofrecer una salida accionable tras declarar la ausencia
- Requirement: Pedir aclaración ante una pregunta ambigua
- Requirement: Recuperar únicamente hechos registrados
- jsdom
- ClinicalEventQueryRepository
- Requirement: Listar los eventos clínicos registrados
- ADDED Requirements
- ClinicalAnswerResult
- ClinicalEventIndexRebuilder
- 8. Búsqueda full-text de PostgreSQL
- UC-011 — Inspeccionar los eventos clínicos registrados
- button.tsx
- 4. Tipos de pruebas y niveles
- ClinicalEventIntent
- Scenarios
- Decisions
- .event
- ClinicalHistoryQueryFlowIntegrationTest
- 7. Cobertura de código
- ClinicalEventInspectionIntegrationTest
- UC-011 — Inspeccionar los eventos clínicos registrados — Acceptance Criteria
- 2026-09-17-raise-jacoco-coverage/proposal.md
- 2026-09-17-uc-011/proposal.md
- 2026-09-17-raise-jacoco-coverage/design.md
- 2026-09-17-uc-011/tasks.md
- 2026-09-17-raise-jacoco-coverage/tasks.md

## God Nodes (most connected - your core abstractions)
1. `ClinicalEvent` - 90 edges
2. `ClinicalEventIntent` - 36 edges
3. `ChatMessageRequest` - 35 edges
4. `ClinicalEventMarkdownStore` - 34 edges
5. `ChatOrchestratorTest` - 31 edges
6. `ClinicalHistoryQueryServiceTest` - 31 edges
7. `MeasurementCsvParserTest` - 29 edges
8. `ChatMessageResponse` - 28 edges
9. `Measurement` - 27 edges
10. `OpenAiClinicalIntentInterpreterTest` - 27 edges

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

## Communities (208 total, 29 thin omitted)

### Community 0 - "MeasurementImportControllerTest"
Cohesion: 0.17
Nodes (7): ImportPreviewResponse, ImportPreviewRow, ImportResultResponse, MeasurementImportException, MeasurementImportControllerTest, MockMultipartFile, org.springframework.mock.web.MockMultipartFile

### Community 1 - "Measurement"
Cohesion: 0.09
Nodes (13): Measurement, Measurement, MeasurementEntity, MeasurementJpaDao, Override, MeasurementJpaRepository, MeasurementTest, MeasurementJpaRepositoryTest (+5 more)

### Community 2 - "measurements/lib/schema.ts"
Cohesion: 0.09
Nodes (25): todayIsoDate(), importErrorResponseSchema, importPreviewResponseSchema, importPreviewRowSchema, importResultResponseSchema, measurementCreatedResponseSchema, measurementFormSchema, measurementInputSchema (+17 more)

### Community 3 - "measurements/actions.ts"
Cohesion: 0.12
Nodes (22): ConfirmImportResult, confirmMeasurementImport(), ImportErrorInfo, PreviewImportResult, previewMeasurementImport(), RegisterMeasurementResult, toImportError(), uploadedFile() (+14 more)

### Community 4 - "metrics.ts"
Cohesion: 0.14
Nodes (27): Separator(), CHART_DESCRIPTIONS, MeasurementsDashboard(), MeasurementsTable(), MetricTrendChart(), buildChartSummary(), buildSeries(), computeDateRange() (+19 more)

### Community 5 - "Requirements"
Cohesion: 0.17
Nodes (12): Requirement: Informar fallos sin inventar una respuesta, Requirement: Interpretar preguntas de seguimiento con la conversación activa, Requirement: Mantener la declaración de ausencia ante la insistencia, Requirement: Mostrar los hechos que sustentan la respuesta, Requirement: Responder la parte respaldada y declarar la parte ausente, Requirements, Scenario: Identificar los hechos de apoyo, Scenario: La persona insiste tras la negativa (+4 more)

### Community 6 - "ClinicalEvent"
Cohesion: 0.11
Nodes (11): ClinicalEvent, EventSource, PATIENT, ClinicalEventIndexWriter, PostgresIntegrationTest, ClinicalHistoryUnsupportedRetrievalIntegrationTest, org.junit.jupiter.api.BeforeEach, org.springframework.boot.test.context.SpringBootTest (+3 more)

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
Nodes (21): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, tailwindcss, @testing-library/jest-dom, @testing-library/user-event (+13 more)

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
Cohesion: 0.13
Nodes (13): metadata, Input(), Label(), registerMeasurement(), FieldErrors, MeasurementForm(), handleSubmit(), Status (+5 more)

### Community 32 - "MeasurementImport.tsx"
Cohesion: 0.15
Nodes (17): geistMono, geistSans, metadata, Table(), TableBody(), TableCaption(), TableCell(), TableHead() (+9 more)

### Community 33 - "Estructura obligatoria del `architecture.md`"
Cohesion: 0.12
Nodes (15): 1. Executive summary, 2. Context and scope, 3. Technology stack, 4. Container view (C4 level 2), 5. Component view (C4 level 3), 6. Repository map, 7. Architectural patterns, 8. Architectural decisions (ADR-light) (+7 more)

### Community 34 - "Especificación de alcance — MVP del asistente de historia clínica personal"
Cohesion: 0.14
Nodes (13): 11. Interfaz de usuario, 12. Arquitectura, 13. Estructura de proyecto, 16. Definition of Done, 17. Criterio técnico de éxito, 18. Principios de diseño, 19. Evolución posterior, 1. Propósito (+5 more)

### Community 35 - "MetricTrendChart.tsx"
Cohesion: 0.11
Nodes (21): metadata, Card(), CardContent(), CardDescription(), CardHeader(), CardTitle(), ClinicalEventsBrowser(), formatDate() (+13 more)

### Community 36 - "architecture.md — Careme"
Cohesion: 0.15
Nodes (12): 1. Executive summary, 2. Context and scope, 3. Technology stack, 4. Container view (C4 level 2), 5.1 Backend (the most complex container), 5.2 Frontend, 5. Component view (C4 level 3), 6. Repository map (+4 more)

### Community 37 - "Data model — Careme"
Cohesion: 0.15
Nodes (12): 1.1 Measurement, 1.2 MeasurementDraft, 1.3 API models, 1.4 Cross-cutting import rules, 1. Implemented model, 2.1 Patient, 2.2 ClinicalEvent, 2. Clinical history assistant model (implemented) (+4 more)

### Community 38 - "Scenarios"
Cohesion: 0.11
Nodes (18): Coverage notes, Execution rules, Feature, Scenario: Conservar la imprecisión temporal en la respuesta, Scenario: Declarar la ausencia de registros que respalden la pregunta, Scenario: Descartar el estado efímero tras una interrupción, Scenario: Informar un fallo al interpretar la pregunta sin inventar, Scenario: Informar un fallo de búsqueda sin inventar una respuesta (+10 more)

### Community 39 - "chart.tsx"
Cohesion: 0.21
Nodes (11): ChartConfig, ChartContainer(), ChartContext, ChartContextProps, ChartLegendContent(), ChartTooltipContent(), getPayloadConfigFromPayload(), INITIAL_DIMENSION (+3 more)

### Community 40 - "04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual"
Cohesion: 0.08
Nodes (24): 04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual, 10. Transacciones y coherencia, 11. Reconstrucción y operación del índice, 1. Qué es la persistencia, 2.1 Elementos fundamentales, 2.2 Tipos y semántica, 2. Qué es una base de datos relacional, 3.1 Características relevantes (+16 more)

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
Cohesion: 0.17
Nodes (11): 3.1. Markdown como representación canónica inicial, 3.2. Los eventos son la fuente primaria, 3.3. Separación entre almacenamiento e inteligencia, 3.4. Retrieval híbrido, 3. Principios de arquitectura, 5. Arquitectura evolutiva, 6. Priorización, 7. Criterio general de evolución (+3 more)

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

### Community 65 - "ClinicalEventInspectionService"
Cohesion: 0.15
Nodes (7): ClinicalEventInspectionResponse, ClinicalEventInspectionService, SortBy, OCCURRENCE_DATE, RECORD_DATE, ClinicalEventInspectionServiceTest, ClinicalEvent

### Community 66 - "ClinicalEventMarkdownStore"
Cohesion: 0.12
Nodes (4): ClinicalEventMarkdownStore, ClinicalEvent, ClinicalEventMarkdownStoreTest, ClinicalEvent

### Community 67 - ".process"
Cohesion: 0.25
Nodes (3): ChatMessageRequest, ChatOrchestratorTest, ClinicalEvent

### Community 68 - "Decisions"
Cohesion: 0.17
Nodes (11): Context, Contrato estructurado entre chat y registro, Decisions, Duplicación limitada a la conversación actual, Goals / Non-Goals, La aplicación valida antes de persistir, Markdown como fuente primaria y PostgreSQL como índice derivado, Migration Plan (+3 more)

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

### Community 74 - "07 — Calidad y pruebas"
Cohesion: 0.10
Nodes (19): 07 — Calidad y pruebas, 1. Qué es calidad, 2.1 Secuencia de aplicación, 2.2 Secuencia real del repositorio, 2. La estrategia de pruebas, 3.1 De requisito a casos, 3.2 Arrange, Act, Assert, 3.3 Dobles de prueba (+11 more)

### Community 75 - "Requirement: Confirmar el resultado y mantener atomicidad ante fallos"
Cohesion: 0.10
Nodes (20): ADDED Requirements, MODIFIED Requirements, Requirement: Asignar códigos de evento únicos y no reutilizables, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown, Requirement: Registrar eventos clínicos mediante la frontera conversacional, Scenario: Configurar la ubicación de la fuente de verdad, Scenario: Confirmar un registro exitoso (+12 more)

### Community 76 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

### Community 77 - "2026-09-13-uc-004/tasks.md"
Cohesion: 0.40
Nodes (4): 1. Modelo y persistencia de eventos, 2. Interpretación y normalización, 3. Registro y coordinación conversacional, 4. Verificación de integración

### Community 78 - ".query"
Cohesion: 0.07
Nodes (17): Query, Scope, HISTORY, MEASUREMENTS, ClinicalEventType, DIAGNOSIS, MEASUREMENT, MEDICATION (+9 more)

### Community 80 - "Requirement: Declarar explícitamente la ausencia de registros"
Cohesion: 0.09
Nodes (21): ADDED Requirements, MODIFIED Requirements, Requirement: Declarar explícitamente la ausencia de registros, Requirement: Distinguir la ausencia de registros de un fallo de búsqueda, Requirement: Mantener la declaración de ausencia ante la insistencia, Requirement: Ofrecer una salida accionable tras declarar la ausencia, Requirement: Responder la parte respaldada y declarar la parte ausente, Scenario: Ausencia acotada al periodo consultado (+13 more)

### Community 94 - "MeasurementCsvParser"
Cohesion: 0.23
Nodes (5): MeasurementCsvParser, ParsedCsv, jakarta.validation.ConstraintViolation, jakarta.validation.Validator, org.apache.commons.csv.CSVFormat

### Community 95 - "ClinicalEventIndexControllerTest"
Cohesion: 0.17
Nodes (5): CaremeBackendApplicationTests, ClinicalEventIndexControllerTest, org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc, org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest, org.springframework.test.web.servlet.MockMvc

### Community 96 - "ClinicalEventIndexController.java"
Cohesion: 0.17
Nodes (10): ChatController, ClinicalEventIndexController, MeasurementController, ApiResponse, ClinicalEventInspectionException, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping (+2 more)

### Community 97 - "ChatWorkspace.test.tsx"
Cohesion: 0.06
Nodes (32): metadata, sendChatMessage(), SendChatResult, chatServiceMock, RESPONSE, absenceReasonLabels, ChatWorkspace(), recordFailure() (+24 more)

### Community 98 - "MeasurementImportService"
Cohesion: 0.28
Nodes (3): MeasurementImportService, Measurement, MeasurementImportServiceTest

### Community 99 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.05
Nodes (40): assistant-conversation Specification, Purpose, Requirement: Allow the declared origin to reach the operations a browser client uses, Requirement: Answer general conversation without consulting the clinical history, Requirement: Decline requests for diagnosis, recommendation or interpretation, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes (+32 more)

### Community 100 - "Decisions"
Cohesion: 0.15
Nodes (12): Context, Decisions, El compositor declara cuánto de la pregunta queda respaldado, El contrato HTTP crece de forma aditiva, El fallo y la ausencia son excluyentes por construcción, El modo `fake` cubre también la respuesta parcial, El motivo y la salida viajan como valores enumerados, no como prosa, Goals / Non-Goals (+4 more)

### Community 101 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.13
Nodes (14): ADDED Requirements, Purpose, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Scenario: Continue a conversation after clarification, Scenario: Conversation state is lost after restart, Scenario: Reject an invalid chat request (+6 more)

### Community 102 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.07
Nodes (28): llm-clinical-intent-adapter Specification, Purpose, Requirement: Compose conversational replies outside the clinical history, Requirement: Compose grounded answers from retrieved events, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Requirements (+20 more)

### Community 103 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.15
Nodes (12): ADDED Requirements, Purpose, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Browser sends a chat message, Scenario: Map a clarification, Scenario: Map general conversation (+4 more)

### Community 104 - "Decisions"
Cohesion: 0.17
Nodes (11): Backend owns orchestration, Context, Decisions, Ephemeral state with bounded keys, Frontend client boundary, Goals / Non-Goals, Markdown plus derived PostgreSQL index, Migration Plan (+3 more)

### Community 105 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.09
Nodes (21): assistant-chat-interface Specification, Purpose, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Requirement: Show the events that support an answer, Requirements, Scenario: Continue after clarification, Scenario: Display the supporting events (+13 more)

### Community 106 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.12
Nodes (16): ADDED Requirements, MODIFIED Requirements, Requirement: Compose grounded answers from retrieved events, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Compose from retrieved events, Scenario: Map a clarification, Scenario: Map a query (+8 more)

### Community 107 - ".register"
Cohesion: 0.11
Nodes (13): DatePrecision, APPROXIMATE, EXACT, UNKNOWN, ClinicalEventConversationRegistry, ClinicalEventDateNormalizer, NormalizedDate, ClinicalEventRegistrationService (+5 more)

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

### Community 114 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Scenario: Answer a follow-up question from recent turns, Scenario: Continue a conversation after clarification, Scenario: Conversation state is lost after restart, Scenario: Discard the ephemeral buffer after an interruption, Scenario: Return a clarification outcome (+6 more)

### Community 115 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.18
Nodes (10): MODIFIED Requirements, Requirement: Return explicit chat outcomes, Scenario: Keep absence out of the failed outcome, Scenario: Return a clarification outcome, Scenario: Return a failure outcome, Scenario: Return a general conversation outcome, Scenario: Return a no-records outcome, Scenario: Return a partial answer outcome (+2 more)

### Community 116 - "01 — Estructura y arquitectura del repositorio"
Cohesion: 0.13
Nodes (14): 01 — Estructura y arquitectura del repositorio, 1. Sistema y arquitectura, 2. Separación de responsabilidades, 3. Zonas del repositorio, 4. Arquitectura por capas del backend, 5. Módulos por funcionalidad del frontend, 6. Dominio, transporte y persistencia, 7. Recorrido de una operación (+6 more)

### Community 118 - "org.springframework.stereotype.Service"
Cohesion: 0.06
Nodes (27): ClinicalAnswerComposer, ComposedAnswer, Coverage, FULL, NONE, PARTIAL, FakeClinicalAnswerComposer, Override (+19 more)

### Community 119 - "Decisions"
Cohesion: 0.14
Nodes (13): Búfer acotado de turnos recientes para el seguimiento, Context, Decisions, Dos operaciones del proveedor: clasificar y componer, El backend decide el enrutamiento; el adaptador solo propone, El índice y los prompts se versionan; los estados son aditivos, Goals / Non-Goals, La fundamentación se valida en el backend (+5 more)

### Community 120 - "02 — Frontend: renderizado y comunicación"
Cohesion: 0.09
Nodes (22): 02 — Frontend: renderizado y comunicación, 1. Qué es el frontend, 2.1 Qué aporta React, 2.2 Qué aporta Next.js, 2.3 Qué aporta TypeScript, 2. El stack y sus responsabilidades, 3.1 `src/app/`: rutas y composición, 3.2 `src/components/ui/`: primitivas visuales (+14 more)

### Community 121 - "Decisions"
Cohesion: 0.15
Nodes (12): Context, Decisions, Event codes are derived from the persisted documents, Goals / Non-Goals, Migration Plan, Open Questions, Publication is create-only, Risks / Trade-offs (+4 more)

### Community 122 - "2026-09-13-chat-server-side-transport/design.md"
Cohesion: 0.17
Nodes (11): apiConfig keeps the local fallback but loses the public branch, chatService validates the response and normalizes its failures, Context, Decisions, Goals / Non-Goals, Migration Plan, Open Questions, Risks / Trade-offs (+3 more)

### Community 123 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.18
Nodes (10): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Send a turn without exposing the API address, Scenario: Show request progress, Scenario: Submit a message (+2 more)

### Community 124 - "08 — Despliegue y operación"
Cohesion: 0.10
Nodes (20): 08 — Despliegue y operación, 10. Criterios de una entrega desplegable, 1. Qué es un despliegue, 2. Del código al artefacto, 3.1 Construcción multietapa, 3.2 Usuario, proceso y puerto, 3. Contenedores e imágenes, 4.1 Dependencias y orden (+12 more)

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

### Community 130 - "Candidate"
Cohesion: 0.25
Nodes (3): Candidate, ClinicalEventIntentValidator, ClinicalEventIntentValidatorTest

### Community 131 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.15
Nodes (12): ADDED Requirements, MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Show the events that support an answer, Scenario: Continue after clarification, Scenario: Display the supporting events, Scenario: Keep the answer verifiable without technical detail, Scenario: Retry a failed request (+4 more)

### Community 132 - "03 — Backend: API, concurrencia y persistencia"
Cohesion: 0.08
Nodes (26): 03 — Backend: API, concurrencia y persistencia, 10. Errores y operaciones compuestas, 11. Dos formas de leer los hechos clínicos, 1. Qué es un backend, 2. Responsabilidades del backend, 3.1 Spring Boot, 3.2 Spring Web MVC, 3.3 Hibernate y JPA (+18 more)

### Community 133 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.07
Nodes (27): ADDED Requirements, MODIFIED Requirements, Requirement: Answer general conversation without consulting the clinical history, Requirement: Decline requests for diagnosis, recommendation or interpretation, Requirement: Return explicit chat outcomes, Requirement: Separate the general part of a mixed message, Scenario: Answer a greeting or a courtesy, Scenario: Answer a question that does not depend on the clinical history (+19 more)

### Community 134 - "3. Qué ocurre durante una consulta"
Cohesion: 0.18
Nodes (11): 3.1 Cómo genera texto un modelo, 3.2.1 Roles de los mensajes, 3.2 El prompt como contrato, 3.3 De lenguaje a estructura: la intención, 3.4 Las fechas: el modelo propone, el código normaliza, 3.5 El adaptador como frontera, 3.6 La recuperación: código, no modelo, 3.7 La composición fundamentada (+3 more)

### Community 135 - "06 — Desarrollo guiado por especificaciones y OpenSpec"
Cohesion: 0.12
Nodes (17): 06 — Desarrollo guiado por especificaciones y OpenSpec, 1. Qué es SDD, 2. Especificación, diseño y tareas, 3. Estructura de OpenSpec, 4.1 Propiedades de un buen escenario, 4.2 Alcance y no objetivos, 4. Cómo se escribe un comportamiento verificable, 5.1 Propuesta (+9 more)

### Community 136 - "doc-sync.prompt.md"
Cohesion: 0.18
Nodes (10): Alcance, Paso 1 — Identificar el cambio disparador, Paso 2 — Extraer los hechos del cambio, Paso 3 — Fuente de verdad (SDD), Paso 4 — Detectar drift por documento, Paso 4b — Estados en `docs/roadmap/use-cases.md` (solo estados), Paso 5 — Reporte (en el chat), Paso 6 — Aplicar correcciones con confirmación (+2 more)

### Community 138 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.20
Nodes (9): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Show a partially supported answer, Scenario: Show an answered turn, Scenario: Show that no records were found, Scenario: Use the interface with keyboard and assistive technology (+1 more)

### Community 139 - "2026-09-13-uc-007/tasks.md"
Cohesion: 0.25
Nodes (7): 1. Contrato de intención de consulta y prompt de clasificación, 2. Recuperación en el índice derivado, 3. Respuesta fundada en los hechos recuperados, 4. Orquestación y contrato de chat, 5. Estado de conversación para preguntas de seguimiento, 6. Interfaz de chat, 7. Verificación integral

### Community 140 - "2026-09-13-uc-007/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 141 - "2026-09-16-uc-008/tasks.md"
Cohesion: 0.22
Nodes (8): 1. Determinar el motivo de la ausencia, 2. Declaración y salida accionable, 3. Exclusión entre ausencia y fallo, 4. Respuesta parcial, 5. Contrato HTTP, 6. Frontend, 7. Verificación integrada, 8. Ampliación: la ausencia cuando la búsqueda sí devuelve hechos

### Community 142 - "Backend (Java / Spring Boot) — Careme"
Cohesion: 0.33
Nodes (5): Backend (Java / Spring Boot) — Careme, Contract, Conventions, Layering (keep it one-directional), LLM integration

### Community 143 - "ChatOrchestrator"
Cohesion: 0.14
Nodes (7): ChatOrchestrator, Status, ClinicalConversationComposer, ClinicalIntentInterpreter, ConversationStateStore, State, Turn

### Community 144 - "05 — IA: LLM, prompts y RAG"
Cohesion: 0.25
Nodes (7): 05 — IA: LLM, prompts y RAG, 1. Qué es un LLM, 2. Por qué se utiliza aquí, 4. Patrones de RAG, 5. Memoria y conversaciones multiturno, 6. Límites y controles para datos clínicos, Índice

### Community 145 - "Frontend (Next.js / React / TypeScript) — Careme"
Cohesion: 0.40
Nodes (4): Conventions, Frontend (Next.js / React / TypeScript) — Careme, Rendering and data, Service layer and validation

### Community 146 - "Requirement: Compose grounded answers from retrieved events"
Cohesion: 0.25
Nodes (7): MODIFIED Requirements, Requirement: Compose grounded answers from retrieved events, Scenario: Compose from retrieved events, Scenario: No retrieved events, Scenario: Preserve temporal precision in the answer, Scenario: Retrieved events do not support the question, Scenario: Retrieved events support only part of the question

### Community 147 - "org.junit.jupiter.api.Test"
Cohesion: 0.11
Nodes (6): EventSummary, ChatControllerTest, ConversationStateStoreTest, FakeClinicalAnswerComposerTest, ClinicalEvent, org.junit.jupiter.api.Test

### Community 152 - "2026-09-16-uc-008/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 153 - "Use Cases — MVP"
Cohesion: 0.17
Nodes (12): UC-001 — Ver el seguimiento corporal, UC-002 — Registrar la medición del día, UC-003 — Cargar mediciones desde un archivo, UC-004 — Registrar un evento clínico, UC-005 — Consultar un evento clínico, UC-006 — Buscar información en la historia clínica, UC-007 — Consultar la historia clínica mediante lenguaje natural, UC-008 — Gestionar información clínica ausente (+4 more)

### Community 154 - "UC-008 — Gestionar información clínica ausente"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con UC-007 y UC-010, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 155 - "UC-010 — Consultar información que no pertenece a la historia clínica"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con UC-007 y UC-008, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 156 - "ChatMessageResponse"
Cohesion: 0.09
Nodes (20): AbsenceReason, EMPTY_HISTORY, NO_EVENTS_IN_PERIOD, NO_EVENTS_OF_TYPE, NO_TERM_MATCH, ChatMessageResponse, Status, ANSWERED (+12 more)

### Community 158 - "UC-007 — Consultar la historia clínica mediante lenguaje natural"
Cohesion: 0.18
Nodes (11): 10. Fuera de alcance, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones, 7. Postcondiciones (+3 more)

### Community 159 - "Scenarios"
Cohesion: 0.20
Nodes (10): Scenario: Acotar la ausencia a un periodo concreto, Scenario: Declarar el estado inicial cuando la historia está vacía, Scenario: Declarar la ausencia ante términos que no coinciden con el registro, Scenario: Declarar la ausencia de registros ante una pregunta no respaldada, Scenario: Declarar que no hay registros de un tipo de hecho, Scenario: Informar un fallo en lugar de declarar una ausencia no comprobada, Scenario: Mantener la declaración ante la insistencia, Scenario: No declarar ausencia para una pregunta de carácter general (+2 more)

### Community 160 - "Scenarios"
Cohesion: 0.11
Nodes (18): Boundary scenarios with UC-007 and UC-008, Coverage notes, Execution rules, Feature, Scenario: Atender como consulta de la historia una pregunta coloquial sobre registros, Scenario: Declinar una petición de recomendación o diagnóstico, Scenario: Distinguir la conversación general de una respuesta fundada, Scenario: Explicar un concepto médico sin aplicarlo al caso de la persona (+10 more)

### Community 161 - "UC-008 — Gestionar información clínica ausente — Acceptance Criteria"
Cohesion: 0.22
Nodes (9): Boundary scenarios with UC-007 and UC-010, Coverage notes, Execution rules, Feature, Scenario: Distinguir la ausencia de registros de un fallo de búsqueda, Scenario: Distinguir la ausencia de registros de una consulta general, Scenario: Distinguir la ausencia de registros de una respuesta fundamentada, Scenario: Ofrecer el registro del hecho que no consta (+1 more)

### Community 162 - "OpenAiClinicalConversationComposerTest"
Cohesion: 0.18
Nodes (3): OpenAiClinicalConversationComposerTest, com.sun.net.httpserver.HttpServer, org.junit.jupiter.api.AfterEach

### Community 163 - "sync-concept-docs.prompt.md"
Cohesion: 0.14
Nodes (13): 1. Identificar el cambio, 2. Extraer conceptos candidatos, 3. Comparar con los capítulos actuales, 4. Preservar la forma de los capítulos, 5. Actualizar el índice general, 6. Aplicar cambios y validar, Alcance, Criterios de idempotencia (+5 more)

### Community 164 - "MeasurementRepository"
Cohesion: 0.11
Nodes (9): MeasurementRequest, MeasurementResponse, MeasurementRepository, MeasurementService, MeasurementControllerTest, Measurement, MeasurementServiceTest, org.junit.jupiter.api.extension.ExtendWith (+1 more)

### Community 166 - "Decisions"
Cohesion: 0.12
Nodes (16): Context, Decisions, El camino conversacional no puede leer la historia por construcción, El clasificador distingue el cauce y conserva la parte general, El contrato HTTP crece con un campo opcional, El fallo de composición es el fallo del turno, El prompt conversacional lleva sus límites, no una instrucción de buscar, El servicio compone la parte general y el resultado de la historia por separado (+8 more)

### Community 168 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.12
Nodes (15): ADDED Requirements, MODIFIED Requirements, Requirement: Compose conversational replies outside the clinical history, Requirement: Produce provider-neutral clinical intents, Scenario: Ask for clarification when the channel is undetermined, Scenario: Compose a reply for a general message, Scenario: Decline inside the composed reply, Scenario: Keep a colloquial question in the clinical history channel (+7 more)

### Community 170 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.15
Nodes (12): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Show a declined request, Scenario: Show a general conversation turn, Scenario: Show a partially supported answer, Scenario: Show a turn that mixes conversation and clinical history (+4 more)

### Community 171 - "Requirement: Reconocer las preguntas sobre la propia historia clínica"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Pedir aclaración ante una pregunta ambigua, Requirement: Reconocer las preguntas sobre la propia historia clínica, Scenario: Cauce por determinar, Scenario: Mensaje que mezcla una parte general con una consulta sobre la historia, Scenario: No puede determinarse si la pregunta depende de la historia, Scenario: Pregunta ambigua, Scenario: Pregunta coloquial sobre hechos registrados (+3 more)

### Community 173 - "Requirement: Declarar explícitamente la ausencia de registros"
Cohesion: 0.22
Nodes (9): Requirement: Declarar explícitamente la ausencia de registros, Scenario: Ausencia acotada al periodo consultado, Scenario: Ausencia acotada al tipo de hecho consultado, Scenario: Ausencia porque ningún hecho responde a la pregunta, Scenario: Historia clínica sin ningún hecho registrado, Scenario: La ausencia no afirma que el hecho no ocurriera, Scenario: La búsqueda devuelve hechos que no responden a la pregunta, Scenario: La búsqueda no devuelve ningún hecho (+1 more)

### Community 175 - "2026-09-17-uc-010/tasks.md"
Cohesion: 0.25
Nodes (7): 1. Composición conversacional, 2. Rama conversacional del orquestador, 3. Clasificación del cauce, 4. Parte general de un mensaje mixto, 5. Contrato HTTP, 6. Frontend, 7. Verificación integrada

### Community 176 - "2026-09-17-uc-010/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 177 - "Requirement: Reconocer las preguntas sobre la propia historia clínica"
Cohesion: 0.29
Nodes (7): Requirement: Reconocer las preguntas sobre la propia historia clínica, Scenario: Mensaje que mezcla una parte general con una consulta sobre la historia, Scenario: No puede determinarse si la pregunta depende de la historia, Scenario: Pregunta coloquial sobre hechos registrados, Scenario: Pregunta que no depende de la historia, Scenario: Pregunta sobre la propia historia, Scenario: Pregunta sobre peso o circunferencia abdominal

### Community 178 - "Requirement: Redactar la respuesta solo con los hechos recuperados"
Cohesion: 0.33
Nodes (6): Requirement: Redactar la respuesta solo con los hechos recuperados, Scenario: Conservar la imprecisión temporal, Scenario: Preguntar por un hecho concreto y su fecha, Scenario: Preguntar por valores o mediciones registradas, Scenario: Responder una pregunta con hechos registrados, Scenario: Responder una pregunta que requiere varios hechos

### Community 180 - "Requirement: Distinguir la ausencia de registros de un fallo de búsqueda"
Cohesion: 0.67
Nodes (3): Requirement: Distinguir la ausencia de registros de un fallo de búsqueda, Scenario: La ausencia no se comunica como fallo, Scenario: La búsqueda falla y no puede comprobarse la ausencia

### Community 181 - "Requirement: Mantener la historia intacta"
Cohesion: 0.67
Nodes (3): Requirement: Mantener la historia intacta, Scenario: Interrupción antes de completar la respuesta, Scenario: La consulta no modifica la historia

### Community 182 - "Requirement: Ofrecer una salida accionable tras declarar la ausencia"
Cohesion: 0.67
Nodes (3): Requirement: Ofrecer una salida accionable tras declarar la ausencia, Scenario: Ofrecer continuar tras la ausencia, Scenario: Registrar únicamente cuando la persona lo pide

### Community 183 - "Requirement: Pedir aclaración ante una pregunta ambigua"
Cohesion: 0.67
Nodes (3): Requirement: Pedir aclaración ante una pregunta ambigua, Scenario: Cauce por determinar, Scenario: Pregunta ambigua

### Community 184 - "Requirement: Recuperar únicamente hechos registrados"
Cohesion: 0.67
Nodes (3): Requirement: Recuperar únicamente hechos registrados, Scenario: No construir hechos no registrados, Scenario: Recuperar los hechos relevantes

### Community 186 - "ClinicalEventQueryRepository"
Cohesion: 0.27
Nodes (4): ClinicalEventQueryRepository, ClinicalEvent, java.sql.ResultSet, org.springframework.jdbc.core.RowMapper

### Community 187 - "Requirement: Listar los eventos clínicos registrados"
Cohesion: 0.08
Nodes (23): clinical-event-inspection Specification, Purpose, Requirement: Consultar el detalle de un evento, Requirement: Filtrar eventos por tipo y periodo, Requirement: Listar los eventos clínicos registrados, Requirement: Mantener la consulta de eventos como operación de solo lectura, Requirement: Presentar fechas relativas como fechas estimadas, Requirements (+15 more)

### Community 188 - "ADDED Requirements"
Cohesion: 0.09
Nodes (22): ADDED Requirements, Purpose, Requirement: Consultar el detalle de un evento, Requirement: Filtrar eventos por tipo y periodo, Requirement: Listar los eventos clínicos registrados, Requirement: Mantener la consulta de eventos como operación de solo lectura, Requirement: Presentar fechas relativas como fechas estimadas, Scenario: Colocar eventos sin fecha de ocurrencia al final (+14 more)

### Community 189 - "ClinicalAnswerResult"
Cohesion: 0.12
Nodes (15): ClinicalAnswerResult, AbsenceReason, SuggestedAction, Kind, ANSWERED, FAILURE, NO_RECORDS, ClinicalEventRegistrationResult (+7 more)

### Community 190 - "ClinicalEventIndexRebuilder"
Cohesion: 0.17
Nodes (6): ClinicalEventIndexRebuilder, ClinicalEventIndexStartupReconciler, org.slf4j.Logger, org.springframework.boot.context.event.ApplicationReadyEvent, org.springframework.context.event.EventListener, org.springframework.stereotype.Component

### Community 191 - "8. Búsqueda full-text de PostgreSQL"
Cohesion: 0.40
Nodes (5): 8.1 Qué es `tsvector`, 8.2 Qué es `tsquery`, 8.3 Qué es `ts_rank`, 8.4 Flujo completo, 8. Búsqueda full-text de PostgreSQL

### Community 192 - "UC-011 — Inspeccionar los eventos clínicos registrados"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 193 - "button.tsx"
Cohesion: 0.31
Nodes (4): Error(), Loading(), Button(), buttonVariants

### Community 194 - "4. Tipos de pruebas y niveles"
Cohesion: 0.40
Nodes (5): 4.1 Prueba unitaria, 4.2 Prueba de capa web, 4.3 Prueba de integración, 4.4 Prueba de componente frontend, 4. Tipos de pruebas y niveles

### Community 195 - "ClinicalEventIntent"
Cohesion: 0.17
Nodes (8): ClinicalEventIntent, Kind, CLARIFICATION, CONVERSATION, EVENTS, QUERY, InterpretationContext, Override

### Community 196 - "Scenarios"
Cohesion: 0.18
Nodes (11): Scenario: Conservar orden y filtros al volver del detalle, Scenario: Consultar el detalle de un evento, Scenario: Filtrar eventos por periodo, Scenario: Filtrar eventos por tipo de hecho, Scenario: Informar que no existen eventos registrados, Scenario: Informar que un evento no está disponible, Scenario: Informar un fallo al obtener los eventos, Scenario: Mostrar como aproximada una fecha relativa resuelta (+3 more)

### Community 197 - "Decisions"
Cohesion: 0.18
Nodes (10): Context, Decisions, Expose a dedicated read contract, separate from chat, Expose both temporal dimensions and default to record-date ordering, Goals / Non-Goals, Keep filters in the query state and preserve them across detail navigation, Migration Plan, Risks / Trade-offs (+2 more)

### Community 200 - "7. Cobertura de código"
Cohesion: 0.40
Nodes (5): 7.1 Cobertura de línea, 7.2 Cobertura de rama, 7.3 JaCoCo y el umbral del backend, 7.4 Cómo se consigue cobertura útil, 7. Cobertura de código

### Community 202 - "UC-011 — Inspeccionar los eventos clínicos registrados — Acceptance Criteria"
Cohesion: 0.29
Nodes (7): Coverage notes, Execution rules, Feature, Read-only boundary scenarios, Scenario: No modificar la historia al consultar un detalle, Scenario: No modificar la historia al filtrar, UC-011 — Inspeccionar los eventos clínicos registrados — Acceptance Criteria

### Community 203 - "2026-09-17-raise-jacoco-coverage/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 204 - "2026-09-17-uc-011/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 205 - "2026-09-17-raise-jacoco-coverage/design.md"
Cohesion: 0.40
Nodes (4): Context, Decisions, Goals / Non-Goals, Risks / Trade-offs

### Community 206 - "2026-09-17-uc-011/tasks.md"
Cohesion: 0.40
Nodes (4): 1. Read-side domain and date presentation, 2. Backend inspection service and contract, 3. Frontend inspection feature, 4. Integrated verification and documentation

### Community 207 - "2026-09-17-raise-jacoco-coverage/tasks.md"
Cohesion: 0.50
Nodes (3): 1. Baseline and prioritization, 2. Targeted coverage improvements, 3. Verification

## Knowledge Gaps
- **1256 isolated node(s):** `com.careme:backend`, `REGISTERED`, `ANSWERED`, `NO_RECORDS`, `CLARIFICATION_REQUIRED` (+1251 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **29 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ClinicalEvent` connect `ClinicalEvent` to `ChatOrchestrator`, `org.junit.jupiter.api.Test`, `ChatMessageResponse`, `OpenAiClinicalConversationComposerTest`, `OpenAiClinicalAnswerComposerTest`, `ClinicalConversationIntegrationTest`, `ClinicalEventQueryRepository`, `ClinicalAnswerResult`, `ClinicalEventIndexRebuilder`, `ClinicalEventInspectionService`, `ClinicalEventMarkdownStore`, `.process`, `.event`, `ClinicalHistoryQueryFlowIntegrationTest`, `ClinicalEventInspectionIntegrationTest`, `.query`, `ClinicalEventIndexController.java`, `.register`, `org.springframework.stereotype.Service`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Why does `MeasurementEntity` connect `Measurement` to `ClinicalEventIndexControllerTest`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **Why does `ClinicalEventRegistrationResult` connect `ClinicalAnswerResult` to `.process`, `.register`, `ClinicalEvent`?**
  _High betweenness centrality (0.004) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ClinicalEvent` (e.g. with `.attendsAColloquialQuestionAsAHistoryQuery()` and `.answeringAQuestionLeavesTheIndexAndTheDocumentsUntouched()`) actually correct?**
  _`ClinicalEvent` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 26 inferred relationships involving `ChatMessageRequest` (e.g. with `.answersWithRecentTurnsWithoutTreatingThemAsRecords()` and `.composesAConversationalReplyInsteadOfAnAcknowledgement()`) actually correct?**
  _`ChatMessageRequest` has 26 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.careme:backend`, `REGISTERED`, `ANSWERED` to the rest of the system?**
  _1256 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Measurement` be split into smaller, more focused modules?**
  _Cohesion score 0.08552188552188553 - nodes in this community are weakly interconnected._