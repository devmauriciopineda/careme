# Graph Report - careme  (2026-09-21)

## Corpus Check
- 416 files · ~307,996 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 4603 nodes · 8715 edges · 300 communities (263 shown, 37 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 845 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dd267375`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MeasurementImportServiceTest
- Requirements
- measurements/lib/schema.ts
- measurements/actions.ts
- metrics.ts
- Requirements
- ClinicalEvent
- dependencies
- compilerOptions
- Careme Frontend README
- devDependencies
- ADDED Requirements
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
- AgentOperationResult
- Subfases
- Metric
- Subfases
- 15. Casos de prueba conversacionales
- Subfases
- Plan: Alinear roadmap, MVP, use cases y README con el repositorio
- 14. Exclusiones explícitas del MVP
- Scenarios
- 10. Reglas de comportamiento
- ADDED Requirements
- 3. Alcance funcional
- 4. Modelo de datos mínimo
- 8. Interpretación del LLM y herramientas del backend
- 9. Responsabilidades del LLM y del backend
- org.springframework.stereotype.Service
- Requirements
- ADDED Requirements
- corpus.spec.ts
- ClinicalEventMarkdownStore
- AgentOperationExecutorTest
- Decisions
- Requirements
- openspec-explore/SKILL.md
- opsx-explore.prompt.md
- scripts
- 2026-09-13-uc-004/proposal.md
- 07 — Calidad y pruebas
- Requirement: Confirmar el resultado y mantener atomicidad ante fallos
- package.json
- 2026-09-13-uc-004/tasks.md
- ClinicalHistoryQueryServiceTest
- V2__create_clinical_event_index.sql
- Requirement: Declarar explícitamente la ausencia de registros
- @tailwindcss/postcss
- Requirements
- @types/react
- MeasurementCsvParser
- ADDED Requirements
- MeasurementController.java
- ChatWorkspace.tsx
- Scenarios
- Requirement: Report the outcome of the turn and the operations it went through
- Decisions
- Requirement: Return explicit chat outcomes
- Requirement: Produce provider-neutral clinical intents
- Requirement: Produce provider-neutral clinical intents
- Decisions
- Requirement: Represent clarification, success, duplicate, and failure states
- Requirement: Produce provider-neutral clinical intents
- ClinicalEventRegistrationService
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
- Requirements
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
- ADDED Requirements
- Requirement: Represent clarification, success, duplicate, and failure states
- 03 — Backend: API, concurrencia y persistencia
- Requirement: Return explicit chat outcomes
- 3. Qué ocurre durante una consulta
- 06 — Desarrollo guiado por especificaciones y OpenSpec
- doc-sync.prompt.md
- ADDED Requirements
- Requirement: Represent clarification, success, duplicate, and failure states
- 2026-09-13-uc-007/tasks.md
- 2026-09-13-uc-007/proposal.md
- 2026-09-16-uc-008/tasks.md
- Backend (Java / Spring Boot) — Careme
- Scenarios
- Requirement: Produce provider-neutral clinical intents
- Frontend (Next.js / React / TypeScript) — Careme
- Requirement: Compose grounded answers from retrieved events
- .status
- backend-tests.instructions.md
- docs.instructions.md
- frontend-tests.instructions.md
- openspec-sdd.instructions.md
- 2026-09-16-uc-008/proposal.md
- Use Cases
- UC-008 — Gestionar información clínica ausente
- UC-010 — Consultar información que no pertenece a la historia clínica
- ChatMessageResponse
- roadmap_asistente_historia_clinica.md
- UC-007 — Consultar la historia clínica mediante lenguaje natural
- Scenarios
- Scenarios
- ProviderTurn
- Measurement
- sync-concept-docs.prompt.md
- Requirement: Represent clarification, success, duplicate, and failure states
- org.springframework.http.ResponseEntity
- Decisions
- AgentToolContractTest
- Requirement: Produce provider-neutral clinical intents
- OpenAiClinicalAnswerComposerTest
- Requirement: Represent clarification, success, duplicate, and failure states
- Requirement: Reconocer las preguntas sobre la propia historia clínica
- Requirement: Produce provider-neutral clinical intents
- Requirement: Declarar explícitamente la ausencia de registros
- chatService.ts
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
- .register
- Subfases
- Requirement: Listar los eventos clínicos registrados
- ADDED Requirements
- ClinicalEventQueryRepository
- Principios de UX de Careme
- UC-013 — Sostener una consulta
- UC-011 — Inspeccionar los eventos clínicos registrados
- OpenAiMeasurementAnswerComposerTest
- UC-013b — Registrar con procedencia los hechos de la consulta
- AgentOperationExecutor
- Scenarios
- Decisions
- ClinicalEventTest
- Decisions
- org.junit.jupiter.api.Test
- Requirement: Represent clarification, success, duplicate, and failure states
- Requirements
- 2026-09-17-raise-jacoco-coverage/proposal.md
- 2026-09-17-uc-011/proposal.md
- 2026-09-17-raise-jacoco-coverage/design.md
- 2026-09-17-uc-011/tasks.md
- 2026-09-17-raise-jacoco-coverage/tasks.md
- AgentTurnRunnerTest
- UC-012 — Atender un mensaje que requiere varias operaciones
- MeasurementRegistrationServiceTest
- .execute
- Reglas de negocio transversales
- Requirement: Report the outcome of the turn and the operations it went through
- MeasurementDraft
- ADDED Requirements
- Subfases
- MeasurementCollectionTest
- Requirement: Report the outcome of the turn and the operations it went through
- MetricBackedMeasurementRepositoryTest
- Requirement: Compose conversational replies outside the clinical history
- ClinicalConversationIntegrationTest
- 2026-09-20-uc-012/tasks.md
- Decisions
- 2026-09-20-uc-012/proposal.md
- Requirement: Registrar eventos clínicos mediante la frontera conversacional
- Alinear la redacción del contrato del adaptador con el agente con operaciones
- Requirement: Confirmar el resultado y mantener atomicidad ante fallos
- AgentOperation
- MeasurementImportControllerTest
- Tasks — Alinear la redacción del contrato del adaptador
- @testing-library/jest-dom
- MetricMeasurement
- ChatOrchestratorTest
- EncounterMarkdownStore
- .create
- MetricMeasurementEntity
- Encounter
- ChatWorkspace.test.tsx
- Requirement: Validar y escribir dentro del camino de la operación
- README.md
- 2026-09-20-uc-013/design.md
- .query
- Requirement: Declarar la procedencia de todo hecho registrado desde una consulta
- ADDED Requirements
- Decisions
- Decisions
- Scenarios
- Requirement: End the consultation from the chat surface
- 2026-09-20-uc-013/tasks.md
- ClinicalHistoryQueryService
- MigrationV5BackfillsLegacyMeasurementsTest
- Kind
- Visión de Careme
- Requirement: Produce provider-neutral clinical intents
- 7. Cobertura de código
- .answer
- .current
- V3__create_encounter_index.sql
- Scenarios
- EncounterTest
- UC-014 — Registrar una medición por lenguaje natural
- UC-014b — Consultar las mediciones por lenguaje natural
- Requirement: Report the outcome of the turn and the operations it went through
- MetricComponent
- MeasurementTurnContractTest
- UC-004 — Registrar un evento clínico
- Requirement: Reconocer las preguntas sobre la propia historia clínica
- .process
- FakeClinicalAnswerComposerTest
- Requirement: Acotar las operaciones disponibles
- button.tsx
- Kind
- .rebuild
- Requirement: Acotar las operaciones disponibles
- MeasurementControllerTest
- chat/lib/schema.ts
- CaremeBackendApplicationTests
- Requirement: Redactar la respuesta solo con los hechos recuperados
- V5__create_metric_measurements.sql
- tailwindcss
- 2026-09-20-uc-014/tasks.md
- 2026-09-20-uc-014/proposal.md
- Requirement: Registrar hechos clínicos propios
- Requirement: Recoger y registrar las mediciones de la consulta
- ClinicalEventIndexControllerTest
- org.springframework.transaction.annotation.Transactional
- @vitejs/plugin-react
- 2026-09-21-uc-014b/proposal.md
- 2026-09-21-uc-014b/tasks.md
- ChatOrchestrator
- org.springframework.beans.factory.annotation.Autowired
- Kind
- RetiredFactTypeTest
- 3. Principios de arquitectura
- chat/actions.ts
- 3. Cómo se construye un caso de prueba
- 5. Arquitectura evolutiva

## God Nodes (most connected - your core abstractions)
1. `ClinicalEvent` - 121 edges
2. `Metric` - 64 edges
3. `ChatMessageResponse` - 50 edges
4. `Encounter` - 47 edges
5. `AgentOperationResult` - 47 edges
6. `MetricMeasurement` - 40 edges
7. `EncounterService` - 39 edges
8. `AgentOperationExecutor` - 37 edges
9. `EncounterNote` - 36 edges
10. `ClinicalEventMarkdownStore` - 36 edges

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

## Communities (300 total, 37 thin omitted)

### Community 0 - "MeasurementImportServiceTest"
Cohesion: 0.29
Nodes (3): ParsedCsv, Measurement, MeasurementImportServiceTest

### Community 1 - "Requirements"
Cohesion: 0.06
Nodes (31): clinical-measurement-registration Specification, Purpose, Requirement: Atender por el cauce de las mediciones un mensaje que contiene una, Requirement: Comprobar antes de escribir y no dejar registros a medias, Requirement: Confirmar brevemente lo registrado, Requirement: Declarar la procedencia de toda medición registrada, Requirement: Exigir una fecha exacta a toda medición, Requirement: Mantener una sola medición por métrica y día (+23 more)

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
Cohesion: 0.07
Nodes (21): ChatMessageRequest, ClinicalEventIntent, ClinicalEvent, ClinicalEventIndexRebuilder, ClinicalEventIndexWriter, PostgresIntegrationTest, ClinicalEventInspectionIntegrationTest, ClinicalEvent (+13 more)

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
Nodes (21): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, jsdom, @playwright/test, @testing-library/react (+13 more)

### Community 11 - "ADDED Requirements"
Cohesion: 0.06
Nodes (30): ADDED Requirements, Purpose, Requirement: Atender por el cauce de las mediciones un mensaje que contiene una, Requirement: Comprobar antes de escribir y no dejar registros a medias, Requirement: Confirmar brevemente lo registrado, Requirement: Declarar la procedencia de toda medición registrada, Requirement: Exigir una fecha exacta a toda medición, Requirement: Mantener una sola medición por métrica y día (+22 more)

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
Cohesion: 0.09
Nodes (22): Conversation integration scenarios, Coverage notes, Execution rules, Feature, Scenario: Conservar una fecha aproximada, Scenario: Continuar una aclaración durante la sesión, Scenario: Evitar duplicar un hecho ya registrado en la conversación, Scenario: Informar indisponibilidad temporal del proveedor (+14 more)

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
Cohesion: 0.12
Nodes (16): 11. Interfaz de usuario, 12. Arquitectura, 13. Estructura de proyecto, 16. Definition of Done, 17. Criterio técnico de éxito, 18. Principios de diseño, 19. Evolución posterior, 1. Propósito (+8 more)

### Community 35 - "MetricTrendChart.tsx"
Cohesion: 0.11
Nodes (21): metadata, Card(), CardContent(), CardDescription(), CardHeader(), CardTitle(), ClinicalEventsBrowser(), formatDate() (+13 more)

### Community 36 - "architecture.md — Careme"
Cohesion: 0.15
Nodes (12): 1. Executive summary, 2. Context and scope, 3. Technology stack, 4. Container view (C4 level 2), 5.1 Backend (the most complex container), 5.2 Frontend, 5. Component view (C4 level 3), 6. Repository map (+4 more)

### Community 37 - "Data model — Careme"
Cohesion: 0.10
Nodes (20): 1.1 MetricMeasurement, 1.1b The legacy daily view, 1.2 MeasurementDraft, 1.3 API models, 1.4 Cross-cutting import rules, 1. Implemented model, 2.1 Patient, 2.2 ClinicalEvent (+12 more)

### Community 38 - "Scenarios"
Cohesion: 0.11
Nodes (18): Coverage notes, Execution rules, Feature, Scenario: Conservar la imprecisión temporal en la respuesta, Scenario: Declarar la ausencia de registros que respalden la pregunta, Scenario: Descartar el estado efímero tras una interrupción, Scenario: Informar un fallo al interpretar la pregunta sin inventar, Scenario: Informar un fallo de búsqueda sin inventar una respuesta (+10 more)

### Community 39 - "chart.tsx"
Cohesion: 0.21
Nodes (11): ChartConfig, ChartContainer(), ChartContext, ChartContextProps, ChartLegendContent(), ChartTooltipContent(), getPayloadConfigFromPayload(), INITIAL_DIMENSION (+3 more)

### Community 40 - "04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual"
Cohesion: 0.06
Nodes (31): 04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual, 10. Transacciones y coherencia, 11. Reconstrucción y operación del índice, 1. Qué es la persistencia, 2.1 Elementos fundamentales, 2.2 Tipos y semántica, 2. Qué es una base de datos relacional, 3.1 Características relevantes (+23 more)

### Community 41 - "Frontend Project Standards"
Cohesion: 0.18
Nodes (11): Careme MVP (frontend-only), Server Components, Frontend Project Standards, Playwright, pnpm Workspaces, Server Components Default, Storybook, TanStack Query (+3 more)

### Community 42 - "Subfases"
Cohesion: 0.17
Nodes (12): 3.1. Búsqueda textual, 3.2. Índice, 3.3. Búsqueda de eventos, 3.4. Retrieval controlado, 3.5. Respuestas basadas en evidencia, 3.6. Persistencia del historial de conversación (diferida), Cierre del MVP, Criterio de éxito del MVP (+4 more)

### Community 43 - "Subfases"
Cohesion: 0.20
Nodes (10): 8.1. Identidad y acceso, 8.2. Protección de datos, 8.3. Auditoría, 8.4. Versionado, 8.5. Seguridad del agente, 8.6. Privacidad y regulación, Fase 8 — Seguridad, auditoría y producción, Objetivo (+2 more)

### Community 44 - "Subfases"
Cohesion: 0.22
Nodes (9): 0.1. Definir entidades mínimas, 0.2. Definir tipos de evento, 0.3. Definir metadatos, 0.4. Definir formato Markdown, 4. Roadmap, Fase 0 — Definición del modelo mínimo — ✅ completada, Objetivo, Resultado (+1 more)

### Community 45 - "2. Alcance funcional proyectado"
Cohesion: 0.22
Nodes (9): 1. Objeto del aplicativo, 2.1. Registro de información clínica, 2.2. Consulta de la historia, 2.3. Síntesis mediante IA, 2.4. Evidencia y trazabilidad, 2.5. Seguridad y operación, 2. Alcance funcional proyectado, Principio arquitectónico fundamental (+1 more)

### Community 46 - "Subfases"
Cohesion: 0.22
Nodes (9): 2.1. Integración del modelo, 2.2. Operaciones del LLM, 2.3. Extracción de información, 2.4. Control de incertidumbre, 2.5. Validación, Fase 2 — Integración del LLM como interfaz — ✅ completada, Objetivo, Resultado (+1 more)

### Community 47 - "AgentOperationResult"
Cohesion: 0.10
Nodes (9): AgentOperationResult, AgentTurnOutcome, AbsenceReason, ClinicalEvent, OperationSummary, Status, SuggestedAction, AgentTurnOutcomeTest (+1 more)

### Community 48 - "Subfases"
Cohesion: 0.22
Nodes (9): 5.1. Entidad Document, 5.2. Object storage, 5.3. Extracción, 5.4. Representación Markdown, 5.5. Relación evidencia-hecho, Fase 5 — Documentos y evidencia, Objetivo, Resultado (+1 more)

### Community 49 - "Metric"
Cohesion: 0.04
Nodes (34): fromCode(), MeasurementUnit, CM, IN, KG, LB, MG_DL, MMHG (+26 more)

### Community 50 - "Subfases"
Cohesion: 0.22
Nodes (9): 7.1. Timeline, 7.2. Resúmenes por condición, 7.3. Comparación de mediciones, 7.4. Correlación temporal, 7.5. Síntesis con evidencia, Fase 7 — Inteligencia longitudinal, Objetivo, Resultado (+1 more)

### Community 51 - "15. Casos de prueba conversacionales"
Cohesion: 0.25
Nodes (8): 15. Casos de prueba conversacionales, Caso 1 — Crear diagnóstico, Caso 2 — Crear medición, Caso 3 — Consultar mediciones, Caso 4 — Consulta temporal, Caso 5 — Información inexistente, Caso 6 — Fecha aproximada, Caso 7 — Pregunta general

### Community 52 - "Subfases"
Cohesion: 0.25
Nodes (8): 1.1. Backend mínimo, 1.2. Persistencia, 1.3. API, 1.4. Interfaz mínima, Fase 1 — MVP técnico de almacenamiento — ✅ completada, Objetivo, Resultado, Subfases

### Community 53 - "Plan: Alinear roadmap, MVP, use cases y README con el repositorio"
Cohesion: 0.25
Nodes (7): Conflictos detectados (documento → repositorio), Decisiones que deben quedar escritas en los documentos, Decisions, Plan: Alinear roadmap, MVP, use cases y README con el repositorio, Relevant files, Steps, Verification

### Community 54 - "14. Exclusiones explícitas del MVP"
Cohesion: 0.29
Nodes (7): 14. Exclusiones explícitas del MVP, Datos clínicos, Documentos y fuentes externas, IA avanzada, Infraestructura, Integración con el seguimiento corporal, Producto

### Community 55 - "Scenarios"
Cohesion: 0.12
Nodes (17): Coverage notes, Execution rules, Feature, Scenario: Cerrar la consulta anterior al iniciar una conversación nueva, Scenario: Cerrar la consulta aunque quede información por recoger, Scenario: Cerrar la consulta cuando la conversación se interrumpe, Scenario: Consulta que permanece abierta porque su cierre no pudo completarse, Scenario: Fallo controlado cuando la consulta no puede abrirse (+9 more)

### Community 56 - "10. Reglas de comportamiento"
Cohesion: 0.33
Nodes (6): 10.1 No inventar, 10.2 No guardar inferencias como hechos, 10.3 Retrieval antes de responder, 10.4 No encontrar información, 10.5 Información reciente, 10. Reglas de comportamiento

### Community 57 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Declarar explícitamente la ausencia de registros, Requirement: Informar fallos sin inventar una respuesta, Requirement: Interpretar preguntas de seguimiento con la conversación activa, Requirement: Mantener la historia intacta, Requirement: Mostrar los hechos que sustentan la respuesta, Requirement: Pedir aclaración ante una pregunta ambigua (+21 more)

### Community 58 - "3. Alcance funcional"
Cohesion: 0.40
Nodes (5): 3.1 Registro de información, 3.2 Consulta de información, 3.3 Preguntas que no requieren recuperación, 3.4 Capacidades incorporadas durante el MVP, 3. Alcance funcional

### Community 59 - "4. Modelo de datos mínimo"
Cohesion: 0.50
Nodes (4): 4.1 ClinicalEvent, 4.2 Tipos de evento iniciales, 4.3 Ejemplo de evento, 4. Modelo de datos mínimo

### Community 60 - "8. Interpretación del LLM y herramientas del backend"
Cohesion: 0.50
Nodes (4): 8.1 create_event, 8.2 search_events, 8.3 get_event y list_events, 8. Interpretación del LLM y herramientas del backend

### Community 61 - "9. Responsabilidades del LLM y del backend"
Cohesion: 0.67
Nodes (3): 9. Responsabilidades del LLM y del backend, Backend, LLM

### Community 62 - "org.springframework.stereotype.Service"
Cohesion: 0.09
Nodes (23): ComposedAnswer, Coverage, FULL, NONE, PARTIAL, FakeClinicalAnswerComposer, Override, FakeMeasurementAnswerComposer (+15 more)

### Community 63 - "Requirements"
Cohesion: 0.05
Nodes (41): clinical-event-registration Specification, Purpose, Requirement: Asignar códigos de evento únicos y no reutilizables, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Conservar la precisión temporal, Requirement: Evitar duplicados dentro de la conversación, Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown, Requirement: Rechazar contenido no registrable o ambiguo (+33 more)

### Community 64 - "ADDED Requirements"
Cohesion: 0.10
Nodes (19): ADDED Requirements, Purpose, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Conservar la precisión temporal, Requirement: Evitar duplicados dentro de la conversación, Requirement: Rechazar contenido no registrable o ambiguo, Requirement: Registrar hechos clínicos propios, Scenario: Confirmar un registro exitoso (+11 more)

### Community 65 - "corpus.spec.ts"
Cohesion: 0.10
Nodes (21): ChatResponse, corpus, EventSummary, Expectation, Fact, measured, NEGATIONS, OperationSummary (+13 more)

### Community 66 - "ClinicalEventMarkdownStore"
Cohesion: 0.07
Nodes (11): ClinicalEventInspectionResponse, ClinicalEventInspectionService, SortBy, OCCURRENCE_DATE, RECORD_DATE, ClinicalEventMarkdownStore, ClinicalEvent, ClinicalEventInspectionServiceTest (+3 more)

### Community 67 - "AgentOperationExecutorTest"
Cohesion: 0.14
Nodes (7): EncounterNoteResult, Kind, COLLECTED, DUPLICATE, FAILURE, AgentOperationExecutorTest, ClinicalEvent

### Community 68 - "Decisions"
Cohesion: 0.17
Nodes (11): Context, Contrato estructurado entre chat y registro, Decisions, Duplicación limitada a la conversación actual, Goals / Non-Goals, La aplicación valida antes de persistir, Markdown como fuente primaria y PostgreSQL como índice derivado, Migration Plan (+3 more)

### Community 69 - "Requirements"
Cohesion: 0.05
Nodes (36): clinical-measurement-query Specification, Purpose, Requirement: Declarar explícitamente la ausencia de mediciones, Requirement: Declarar una métrica que no forma parte del seguimiento, Requirement: Declinar interpretaciones y recomendaciones sobre las mediciones, Requirement: Determinar la métrica y el periodo de la pregunta, Requirement: Informar de un fallo al recuperar sin presentarlo como ausencia, Requirement: Permitir identificar las mediciones en que se apoya la respuesta (+28 more)

### Community 70 - "openspec-explore/SKILL.md"
Cohesion: 0.18
Nodes (10): Check for context, Ending Discovery, Guardrails, Handling Different Entry Points, OpenSpec Awareness, The Stance, What You Don't Have To Do, What You Might Do (+2 more)

### Community 71 - "opsx-explore.prompt.md"
Cohesion: 0.20
Nodes (9): Check for context, Ending Discovery, Guardrails, OpenSpec Awareness, The Stance, What You Don't Have To Do, What You Might Do, When a change exists (+1 more)

### Community 72 - "scripts"
Cohesion: 0.20
Nodes (10): scripts, build, dev, lint, start, test, test:e2e, test:e2e:corpus (+2 more)

### Community 73 - "2026-09-13-uc-004/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 74 - "07 — Calidad y pruebas"
Cohesion: 0.08
Nodes (24): 07 — Calidad y pruebas, 1. Qué es calidad, 2.1 Secuencia de aplicación, 2.2 Secuencia real del repositorio, 2. La estrategia de pruebas, 4.1 Prueba unitaria, 4.2 Prueba de capa web, 4.3 Prueba de integración (+16 more)

### Community 75 - "Requirement: Confirmar el resultado y mantener atomicidad ante fallos"
Cohesion: 0.10
Nodes (20): ADDED Requirements, MODIFIED Requirements, Requirement: Asignar códigos de evento únicos y no reutilizables, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Garantizar la persistencia y el diagnóstico del almacenamiento Markdown, Requirement: Registrar eventos clínicos mediante la frontera conversacional, Scenario: Configurar la ubicación de la fuente de verdad, Scenario: Confirmar un registro exitoso (+12 more)

### Community 76 - "package.json"
Cohesion: 0.40
Nodes (4): name, packageManager, private, version

### Community 77 - "2026-09-13-uc-004/tasks.md"
Cohesion: 0.40
Nodes (4): 1. Modelo y persistencia de eventos, 2. Interpretación y normalización, 3. Registro y coordinación conversacional, 4. Verificación de integración

### Community 78 - "ClinicalHistoryQueryServiceTest"
Cohesion: 0.17
Nodes (3): ClinicalHistoryQueryServiceTest, AbsenceReason, ClinicalEvent

### Community 80 - "Requirement: Declarar explícitamente la ausencia de registros"
Cohesion: 0.09
Nodes (21): ADDED Requirements, MODIFIED Requirements, Requirement: Declarar explícitamente la ausencia de registros, Requirement: Distinguir la ausencia de registros de un fallo de búsqueda, Requirement: Mantener la declaración de ausencia ante la insistencia, Requirement: Ofrecer una salida accionable tras declarar la ausencia, Requirement: Responder la parte respaldada y declarar la parte ausente, Scenario: Ausencia acotada al periodo consultado (+13 more)

### Community 82 - "Requirements"
Cohesion: 0.06
Nodes (32): assistant-agent-turn Specification, Purpose, Requirement: Acotar la actuación al mensaje que la origina, Requirement: Acotar las operaciones disponibles, Requirement: Decidir y ejecutar las operaciones del mensaje, Requirement: Entregar una sola respuesta para todo el mensaje, Requirement: Informar una indisponibilidad del asistente sin efecto alguno, Requirement: Pedir la información que falta sin ejecutar la operación (+24 more)

### Community 94 - "MeasurementCsvParser"
Cohesion: 0.23
Nodes (6): MeasurementRequest, MeasurementImportException, MeasurementCsvParser, jakarta.validation.ConstraintViolation, jakarta.validation.Validator, org.apache.commons.csv.CSVFormat

### Community 95 - "ADDED Requirements"
Cohesion: 0.08
Nodes (25): ADDED Requirements, Purpose, Requirement: Acotar la actuación al mensaje que la origina, Requirement: Acotar las operaciones disponibles, Requirement: Decidir y ejecutar las operaciones del mensaje, Requirement: Entregar una sola respuesta para todo el mensaje, Requirement: Informar una indisponibilidad del asistente sin efecto alguno, Requirement: Pedir la información que falta sin ejecutar la operación (+17 more)

### Community 96 - "MeasurementController.java"
Cohesion: 0.18
Nodes (10): ChatController, ClinicalEventIndexController, MeasurementController, ApiResponse, ClinicalEventInspectionException, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping (+2 more)

### Community 97 - "ChatWorkspace.tsx"
Cohesion: 0.14
Nodes (14): metadata, closeConsultation(), sendChatMessage(), absenceReasonLabels, ChatWorkspace(), endConsultation(), recordFailure(), submit() (+6 more)

### Community 98 - "Scenarios"
Cohesion: 0.12
Nodes (17): Coverage notes, Execution rules, Feature, Scenario: Cerrar la consulta cuando el resumen no puede guardarse, Scenario: Cerrar una consulta sin hechos clínicos, Scenario: Consulta que no se da por cerrada porque el cierre no pudo completarse, Scenario: Informar de un hecho que falla tras otro ya registrado, Scenario: No duplicar un hecho ya registrado (+9 more)

### Community 99 - "Requirement: Report the outcome of the turn and the operations it went through"
Cohesion: 0.05
Nodes (43): assistant-conversation Specification, Purpose, Requirement: Allow the declared origin to reach the operations a browser client uses, Requirement: Answer general conversation without consulting the clinical history, Requirement: Close the consultation in progress, Requirement: Decline requests for diagnosis, recommendation or interpretation, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation (+35 more)

### Community 100 - "Decisions"
Cohesion: 0.15
Nodes (12): Context, Decisions, El compositor declara cuánto de la pregunta queda respaldado, El contrato HTTP crece de forma aditiva, El fallo y la ausencia son excluyentes por construcción, El modo `fake` cubre también la respuesta parcial, El motivo y la salida viajan como valores enumerados, no como prosa, Goals / Non-Goals (+4 more)

### Community 101 - "Requirement: Return explicit chat outcomes"
Cohesion: 0.13
Nodes (14): ADDED Requirements, Purpose, Requirement: Make message retries idempotent, Requirement: Process chat messages through a bounded conversation, Requirement: Return explicit chat outcomes, Scenario: Continue a conversation after clarification, Scenario: Conversation state is lost after restart, Scenario: Reject an invalid chat request (+6 more)

### Community 102 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.04
Nodes (46): llm-clinical-intent-adapter Specification, Purpose, Requirement: Compose conversational replies outside the clinical history, Requirement: Compose grounded answers from retrieved events, Requirement: Compose grounded answers from retrieved measurements, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Select the assistant provider with the real assistant by default (+38 more)

### Community 103 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.15
Nodes (12): ADDED Requirements, Purpose, Requirement: Keep provider access behind the backend, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Browser sends a chat message, Scenario: Map a clarification, Scenario: Map general conversation (+4 more)

### Community 104 - "Decisions"
Cohesion: 0.17
Nodes (11): Backend owns orchestration, Context, Decisions, Ephemeral state with bounded keys, Frontend client boundary, Goals / Non-Goals, Markdown plus derived PostgreSQL index, Migration Plan (+3 more)

### Community 105 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.06
Nodes (34): assistant-chat-interface Specification, Purpose, Requirement: End the consultation from the chat surface, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Send and display assistant turns, Requirement: Show the events that support an answer, Requirement: Show the measurements that support an answer, Requirements (+26 more)

### Community 106 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.12
Nodes (16): ADDED Requirements, MODIFIED Requirements, Requirement: Compose grounded answers from retrieved events, Requirement: Produce provider-neutral clinical intents, Requirement: Validate provider responses before side effects, Scenario: Compose from retrieved events, Scenario: Map a clarification, Scenario: Map a query (+8 more)

### Community 107 - "ClinicalEventRegistrationService"
Cohesion: 0.10
Nodes (12): DatePrecision, APPROXIMATE, EXACT, UNKNOWN, ClinicalEventConversationRegistry, ClinicalEventDateNormalizer, NormalizedDate, ClinicalEventRegistrationService (+4 more)

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

### Community 118 - "Requirements"
Cohesion: 0.06
Nodes (35): clinical-consultation Specification, Purpose, Requirement: Abrir una consulta con la conversación, Requirement: Cerrar la consulta ante una interrupción, Requirement: Cerrar la consulta cuando la persona lo decide y no reabrirla, Requirement: Completar la información de lo recogido sin inventarla, Requirement: Conservar el resumen de la consulta, Requirement: Mantener una sola consulta en curso (+27 more)

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

### Community 130 - "ADDED Requirements"
Cohesion: 0.06
Nodes (35): ADDED Requirements, Purpose, Requirement: Declarar explícitamente la ausencia de mediciones, Requirement: Declarar una métrica que no forma parte del seguimiento, Requirement: Declinar interpretaciones y recomendaciones sobre las mediciones, Requirement: Determinar la métrica y el periodo de la pregunta, Requirement: Informar de un fallo al recuperar sin presentarlo como ausencia, Requirement: Permitir identificar las mediciones en que se apoya la respuesta (+27 more)

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
Cohesion: 0.10
Nodes (21): 05 — IA: LLM, prompts y RAG, 1.1 Qué es un agente, 1.2 Qué son las herramientas y cómo las usa un agente, 1. Qué es un LLM, 2. Por qué se utiliza aquí, 3.1 Cómo genera texto un modelo, 3.2.1 Roles de los mensajes, 3.2 El prompt como contrato (+13 more)

### Community 135 - "06 — Desarrollo guiado por especificaciones y OpenSpec"
Cohesion: 0.12
Nodes (17): 06 — Desarrollo guiado por especificaciones y OpenSpec, 1. Qué es SDD, 2. Especificación, diseño y tareas, 3. Estructura de OpenSpec, 4.1 Propiedades de un buen escenario, 4.2 Alcance y no objetivos, 4. Cómo se escribe un comportamiento verificable, 5.1 Propuesta (+9 more)

### Community 136 - "doc-sync.prompt.md"
Cohesion: 0.18
Nodes (10): Alcance, Paso 1 — Identificar el cambio disparador, Paso 2 — Extraer los hechos del cambio, Paso 3 — Fuente de verdad (SDD), Paso 4 — Detectar drift por documento, Paso 4b — Estados en `docs/roadmap/use-cases.md` (solo estados), Paso 5 — Reporte (en el chat), Paso 6 — Aplicar correcciones con confirmación (+2 more)

### Community 137 - "ADDED Requirements"
Cohesion: 0.06
Nodes (30): ADDED Requirements, Purpose, Requirement: Abrir una consulta con la conversación, Requirement: Cerrar la consulta ante una interrupción, Requirement: Cerrar la consulta cuando la persona lo decide y no reabrirla, Requirement: Completar la información de lo recogido sin inventarla, Requirement: Conservar el resumen de la consulta, Requirement: Mantener una sola consulta en curso (+22 more)

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

### Community 143 - "Scenarios"
Cohesion: 0.11
Nodes (19): Coverage notes, Execution rules, Feature, Scenario: Atender en un solo intercambio un mensaje que requiere varias operaciones, Scenario: Completar un registro con lo que consta en la historia, Scenario: Declarar que una parte del mensaje no puede atenderse, Scenario: Encadenar varias consultas antes de responder, Scenario: Fallo controlado cuando el asistente no está disponible (+11 more)

### Community 144 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.08
Nodes (25): ADDED Requirements, MODIFIED Requirements, Requirement: Compose grounded answers from retrieved measurements, Requirement: Produce provider-neutral clinical intents, Scenario: Ask for clarification when the channel is undetermined, Scenario: Ask which metric when the question does not specify one, Scenario: Compose from retrieved measurements, Scenario: Consult the clinical history more than once in a turn (+17 more)

### Community 145 - "Frontend (Next.js / React / TypeScript) — Careme"
Cohesion: 0.40
Nodes (4): Conventions, Frontend (Next.js / React / TypeScript) — Careme, Rendering and data, Service layer and validation

### Community 146 - "Requirement: Compose grounded answers from retrieved events"
Cohesion: 0.25
Nodes (7): MODIFIED Requirements, Requirement: Compose grounded answers from retrieved events, Scenario: Compose from retrieved events, Scenario: No retrieved events, Scenario: Preserve temporal precision in the answer, Scenario: Retrieved events do not support the question, Scenario: Retrieved events support only part of the question

### Community 152 - "2026-09-16-uc-008/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 153 - "Use Cases"
Cohesion: 0.07
Nodes (29): Fase 4 — no forman parte del MVP, Revisión prevista de los casos de uso del MVP, UC-001 — Ver el seguimiento corporal, UC-002 — Registrar la medición del día, UC-003 — Cargar mediciones desde un archivo, UC-004 — Registrar un evento clínico, UC-005 — Consultar un evento clínico, UC-006 — Buscar información en la historia clínica (+21 more)

### Community 154 - "UC-008 — Gestionar información clínica ausente"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con UC-007 y UC-010, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 155 - "UC-010 — Consultar información que no pertenece a la historia clínica"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con UC-007 y UC-008, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 156 - "ChatMessageResponse"
Cohesion: 0.08
Nodes (27): AbsenceReason, EMPTY_HISTORY, METRIC_NOT_TRACKED, NO_EVENTS_IN_PERIOD, NO_EVENTS_OF_TYPE, NO_MEASUREMENTS, NO_MEASUREMENTS_IN_PERIOD, NO_TERM_MATCH (+19 more)

### Community 158 - "UC-007 — Consultar la historia clínica mediante lenguaje natural"
Cohesion: 0.18
Nodes (11): 10. Fuera de alcance, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones, 7. Postcondiciones (+3 more)

### Community 159 - "Scenarios"
Cohesion: 0.11
Nodes (19): Boundary scenarios with UC-007 and UC-010, Coverage notes, Execution rules, Feature, Scenario: Acotar la ausencia a un periodo concreto, Scenario: Declarar el estado inicial cuando la historia está vacía, Scenario: Declarar la ausencia ante términos que no coinciden con el registro, Scenario: Declarar la ausencia de registros ante una pregunta no respaldada (+11 more)

### Community 160 - "Scenarios"
Cohesion: 0.11
Nodes (18): Boundary scenarios with UC-007 and UC-008, Coverage notes, Execution rules, Feature, Scenario: Atender como consulta de la historia una pregunta coloquial sobre registros, Scenario: Declinar una petición de recomendación o diagnóstico, Scenario: Distinguir la conversación general de una respuesta fundada, Scenario: Explicar un concepto médico sin aplicarlo al caso de la persona (+10 more)

### Community 161 - "ProviderTurn"
Cohesion: 0.10
Nodes (11): AgentContext, AgentProvider, ProviderCall, ProviderTurn, AgentTurnRunner, Override, Override, Override (+3 more)

### Community 162 - "Measurement"
Cohesion: 0.15
Nodes (6): MeasurementResponse, Measurement, MeasurementRepository, MeasurementImportService, MeasurementService, MeasurementTest

### Community 163 - "sync-concept-docs.prompt.md"
Cohesion: 0.14
Nodes (13): 1. Identificar el cambio, 2. Extraer conceptos candidatos, 3. Comparar con los capítulos actuales, 4. Preservar la forma de los capítulos, 5. Actualizar el índice general, 6. Aplicar cambios y validar, Alcance, Criterios de idempotencia (+5 more)

### Community 164 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.09
Nodes (21): ADDED Requirements, MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Requirement: Show the measurements that support an answer, Scenario: Continue after clarification, Scenario: Display the supporting measurements, Scenario: Keep the answer verifiable without technical detail, Scenario: Retry a failed request (+13 more)

### Community 165 - "org.springframework.http.ResponseEntity"
Cohesion: 0.20
Nodes (12): ErrorDetail, ErrorResponse, ApiExceptionHandler, ApiExceptionHandlerTest, org.springframework.http.converter.HttpMessageNotReadableException, org.springframework.http.ResponseEntity, org.springframework.validation.FieldError, org.springframework.web.bind.annotation.ExceptionHandler (+4 more)

### Community 166 - "Decisions"
Cohesion: 0.12
Nodes (16): Context, Decisions, El camino conversacional no puede leer la historia por construcción, El clasificador distingue el cauce y conserva la parte general, El contrato HTTP crece con un campo opcional, El fallo de composición es el fallo del turno, El prompt conversacional lleva sus límites, no una instrucción de buscar, El servicio compone la parte general y el resultado de la historia por separado (+8 more)

### Community 168 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.12
Nodes (15): ADDED Requirements, MODIFIED Requirements, Requirement: Compose conversational replies outside the clinical history, Requirement: Produce provider-neutral clinical intents, Scenario: Ask for clarification when the channel is undetermined, Scenario: Compose a reply for a general message, Scenario: Decline inside the composed reply, Scenario: Keep a colloquial question in the clinical history channel (+7 more)

### Community 169 - "OpenAiClinicalAnswerComposerTest"
Cohesion: 0.21
Nodes (4): ClinicalEvent, OpenAiClinicalAnswerComposerTest, com.sun.net.httpserver.HttpServer, org.junit.jupiter.api.AfterEach

### Community 170 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.15
Nodes (12): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Show a declined request, Scenario: Show a general conversation turn, Scenario: Show a partially supported answer, Scenario: Show a turn that mixes conversation and clinical history (+4 more)

### Community 171 - "Requirement: Reconocer las preguntas sobre la propia historia clínica"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Pedir aclaración ante una pregunta ambigua, Requirement: Reconocer las preguntas sobre la propia historia clínica, Scenario: Cauce por determinar, Scenario: Mensaje que mezcla una parte general con una consulta sobre la historia, Scenario: No puede determinarse si la pregunta depende de la historia, Scenario: Pregunta ambigua, Scenario: Pregunta coloquial sobre hechos registrados (+3 more)

### Community 172 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.11
Nodes (17): ADDED Requirements, MODIFIED Requirements, Requirement: Produce provider-neutral clinical intents, Requirement: Select the assistant provider with the real assistant by default, Scenario: Ask for clarification when the channel is undetermined, Scenario: Consult the clinical history more than once in a turn, Scenario: Keep a colloquial question in the clinical history channel, Scenario: Keep the simulated assistant available for development and testing (+9 more)

### Community 173 - "Requirement: Declarar explícitamente la ausencia de registros"
Cohesion: 0.22
Nodes (9): Requirement: Declarar explícitamente la ausencia de registros, Scenario: Ausencia acotada al periodo consultado, Scenario: Ausencia acotada al tipo de hecho consultado, Scenario: Ausencia porque ningún hecho responde a la pregunta, Scenario: Historia clínica sin ningún hecho registrado, Scenario: La ausencia no afirma que el hecho no ocurriera, Scenario: La búsqueda devuelve hechos que no responden a la pregunta, Scenario: La búsqueda no devuelve ningún hecho (+1 more)

### Community 174 - "chatService.ts"
Cohesion: 0.14
Nodes (8): chatServiceMock, RESPONSE, ChatMessageInput, API_BASE_URL, chatService, ChatUnavailableError, CHAT_RESPONSE, INPUT

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

### Community 185 - ".register"
Cohesion: 0.28
Nodes (4): ClinicalEventRegistrationResult, ClinicalEvent, EncounterServiceTest, ClinicalEvent

### Community 186 - "Subfases"
Cohesion: 0.13
Nodes (15): 4.10. Perfil extendido, 4.11. Requisitos de ingeniería de la fase, 4.1. Consulta, 4.2. Agente con herramientas, 4.3. Modelo de mediciones, 4.4. Perfil del paciente, 4.5. Nuevos tipos de evento, 4.6. Condiciones longitudinales (+7 more)

### Community 187 - "Requirement: Listar los eventos clínicos registrados"
Cohesion: 0.08
Nodes (23): clinical-event-inspection Specification, Purpose, Requirement: Consultar el detalle de un evento, Requirement: Filtrar eventos por tipo y periodo, Requirement: Listar los eventos clínicos registrados, Requirement: Mantener la consulta de eventos como operación de solo lectura, Requirement: Presentar fechas relativas como fechas estimadas, Requirements (+15 more)

### Community 188 - "ADDED Requirements"
Cohesion: 0.09
Nodes (22): ADDED Requirements, Purpose, Requirement: Consultar el detalle de un evento, Requirement: Filtrar eventos por tipo y periodo, Requirement: Listar los eventos clínicos registrados, Requirement: Mantener la consulta de eventos como operación de solo lectura, Requirement: Presentar fechas relativas como fechas estimadas, Scenario: Colocar eventos sin fecha de ocurrencia al final (+14 more)

### Community 189 - "ClinicalEventQueryRepository"
Cohesion: 0.12
Nodes (12): codes(), admissible(), ClinicalEventType, DIAGNOSIS, MEASUREMENT, MEDICATION, NOTE, ClinicalEventQueryRepository (+4 more)

### Community 190 - "Principios de UX de Careme"
Cohesion: 0.12
Nodes (17): 1. Propósito, 2. Alcance, 3. Principios, 4. Aplicación, Implicación, Implicación, Implicación, Implicación (+9 more)

### Community 191 - "UC-013 — Sostener una consulta"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 192 - "UC-011 — Inspeccionar los eventos clínicos registrados"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 193 - "OpenAiMeasurementAnswerComposerTest"
Cohesion: 0.23
Nodes (3): Override, FakeMeasurementAnswerComposerTest, OpenAiMeasurementAnswerComposerTest

### Community 194 - "UC-013b — Registrar con procedencia los hechos de la consulta"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 195 - "AgentOperationExecutor"
Cohesion: 0.18
Nodes (10): Scope, HISTORY, MEASUREMENTS, AgentOperationExecutor, MeasurementRejected, Rejection, ELSEWHERE, NOT_ADMISSIBLE (+2 more)

### Community 196 - "Scenarios"
Cohesion: 0.11
Nodes (18): Coverage notes, Execution rules, Feature, Read-only boundary scenarios, Scenario: Conservar orden y filtros al volver del detalle, Scenario: Consultar el detalle de un evento, Scenario: Filtrar eventos por periodo, Scenario: Filtrar eventos por tipo de hecho (+10 more)

### Community 197 - "Decisions"
Cohesion: 0.18
Nodes (10): Context, Decisions, Expose a dedicated read contract, separate from chat, Expose both temporal dimensions and default to record-date ordering, Goals / Non-Goals, Keep filters in the query state and preserve them across detail navigation, Migration Plan, Risks / Trade-offs (+2 more)

### Community 198 - "ClinicalEventTest"
Cohesion: 0.27
Nodes (4): EventSource, PATIENT, ClinicalEventTest, ClinicalEvent

### Community 199 - "Decisions"
Cohesion: 0.13
Nodes (14): Context, D1 — El bucle de operaciones vive en el backend, y el modelo elige entre funciones declaradas, D2 — El turno transporta una lista de operaciones en lugar de un estado por combinación, D3 — La operación de registro valida y escribe dentro de la llamada, D4 — Un fallo posterior no revierte lo completado, D5 — El modo simulado pasa a ser un doble de prueba que también elige operaciones, D6 — El proveedor real es el valor por defecto, D7 — Los prompts se versionan de nuevo (+6 more)

### Community 200 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (6): AgentOperationTest, MetricTest, ConversationStateStoreTest, OpenAiAgentProviderTest, org.junit.jupiter.api.Test, SuppressWarnings

### Community 201 - "Requirement: Represent clarification, success, duplicate, and failure states"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Represent clarification, success, duplicate, and failure states, Scenario: Continue after clarification, Scenario: Retry a failed request, Scenario: Show a declined request, Scenario: Show a general conversation turn, Scenario: Show a partially supported answer, Scenario: Show a turn that mixes conversation and clinical history (+6 more)

### Community 202 - "Requirements"
Cohesion: 0.09
Nodes (22): clinical-metric-catalog Specification, Purpose, Requirement: Admitir un catálogo extensible de métricas, Requirement: Ampliar el catálogo solo con la confirmación de la persona, Requirement: Convertir un valor expresado en otra unidad de la misma métrica, Requirement: Guardar toda medición en la unidad de referencia de su métrica, Requirement: Resolver la unidad de una medición solo cuando es inequívoca, Requirement: Tratar una métrica compuesta como una sola medición (+14 more)

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

### Community 208 - "AgentTurnRunnerTest"
Cohesion: 0.26
Nodes (3): AgentTurnRunnerTest, ClinicalEvent, ObjectMapper

### Community 209 - "UC-012 — Atender un mensaje que requiere varias operaciones"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 210 - "MeasurementRegistrationServiceTest"
Cohesion: 0.14
Nodes (13): MetricMeasurementOutcome, Kind, COMPLETED, FAILED, NO_RECORDS, REJECTED, Status, Kind (+5 more)

### Community 211 - ".execute"
Cohesion: 0.14
Nodes (5): AgentOperationCall, FakeClinicalAgent, Override, FakeClinicalAgentTest, ClinicalEvent

### Community 212 - "Reglas de negocio transversales"
Cohesion: 0.20
Nodes (10): 1. Fundamentación, 2. Temporalidad, 3. Ausencia de información, 4. Límite clínico, 5. Escritura, validación y no modificación, 6. Conversación y cauce, 7. Procedencia y fuente, Convenciones (+2 more)

### Community 213 - "Requirement: Report the outcome of the turn and the operations it went through"
Cohesion: 0.13
Nodes (14): ADDED Requirements, REMOVED Requirements, Requirement: Report the outcome of the turn and the operations it went through, Requirement: Return explicit chat outcomes, Requirement: Separate the general part of a mixed message, Scenario: Answer general conversation without touching the history, Scenario: Keep absence out of the failed outcome, Scenario: Report a completed operation when another one failed (+6 more)

### Community 215 - "ADDED Requirements"
Cohesion: 0.09
Nodes (21): ADDED Requirements, Purpose, Requirement: Admitir un catálogo extensible de métricas, Requirement: Ampliar el catálogo solo con la confirmación de la persona, Requirement: Convertir un valor expresado en otra unidad de la misma métrica, Requirement: Guardar toda medición en la unidad de referencia de su métrica, Requirement: Resolver la unidad de una medición solo cuando es inequívoca, Requirement: Tratar una métrica compuesta como una sola medición (+13 more)

### Community 216 - "Subfases"
Cohesion: 0.22
Nodes (9): 6.1. Embeddings, 6.2. Vector search, 6.3. Retrieval híbrido, 6.4. Ranking, 6.5. Evaluación, Fase 6 — Retrieval semántico y RAG, Objetivo, Resultado (+1 more)

### Community 217 - "MeasurementCollectionTest"
Cohesion: 0.18
Nodes (7): EncounterMeasurementResult, Kind, COLLECTED, DUPLICATE, FAILURE, ObjectMapper, MeasurementCollectionTest

### Community 218 - "Requirement: Report the outcome of the turn and the operations it went through"
Cohesion: 0.08
Nodes (23): ADDED Requirements, MODIFIED Requirements, Requirement: Close the consultation in progress, Requirement: Process chat messages through a bounded conversation, Requirement: Report the outcome of the turn and the operations it went through, Scenario: Answer a follow-up question from recent turns, Scenario: Answer general conversation without touching the history, Scenario: Close a consultation that collected no clinical facts (+15 more)

### Community 219 - "MetricBackedMeasurementRepositoryTest"
Cohesion: 0.12
Nodes (3): MetricBackedMeasurementRepositoryTest, Measurement, MeasurementServiceTest

### Community 220 - "Requirement: Compose conversational replies outside the clinical history"
Cohesion: 0.20
Nodes (9): MODIFIED Requirements, Requirement: Compose conversational replies outside the clinical history, Requirement: Validate provider responses before side effects, Scenario: Compose a reply for a general message, Scenario: Decline inside the composed reply, Scenario: Provider cites an event outside the retrieved set, Scenario: Provider returns invalid JSON, Scenario: Provider times out or is unavailable (+1 more)

### Community 222 - "2026-09-20-uc-012/tasks.md"
Cohesion: 0.22
Nodes (8): 1. Frontera de operaciones, 2. Bucle del turno y aislamiento de fallos, 3. Contrato HTTP del turno, 4. Agente simulado, 5. Prompts, 6. Frontend, 7. Proveedor por defecto y despliegue, 8. Verificación de extremo a extremo

### Community 223 - "Decisions"
Cohesion: 0.25
Nodes (7): Context, D1 — Alinear la redacción en lugar de dejar la deuda, D2 — No cambiar el comportamiento que los requisitos protegen, D3 — Un cambio propio en lugar de un retoque a mano, Decisions, Design — Alinear la redacción del contrato del adaptador, Risks / Trade-offs

### Community 224 - "2026-09-20-uc-012/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 225 - "Requirement: Registrar eventos clínicos mediante la frontera conversacional"
Cohesion: 0.29
Nodes (6): MODIFIED Requirements, Requirement: Registrar eventos clínicos mediante la frontera conversacional, Scenario: Conservar un registro completado tras un fallo posterior, Scenario: Mantener publicación atómica, Scenario: Reconstruir el índice derivado, Scenario: Registrar mediante el flujo conversacional

### Community 226 - "Alinear la redacción del contrato del adaptador con el agente con operaciones"
Cohesion: 0.33
Nodes (5): Alinear la redacción del contrato del adaptador con el agente con operaciones, Capabilities, Impact, What Changes, Why

### Community 227 - "Requirement: Confirmar el resultado y mantener atomicidad ante fallos"
Cohesion: 0.10
Nodes (20): MODIFIED Requirements, Requirement: Confirmar el resultado y mantener atomicidad ante fallos, Requirement: Evitar duplicados dentro de la conversación, Requirement: Registrar eventos clínicos mediante la frontera conversacional, Requirement: Registrar hechos clínicos propios, Scenario: Confirmar un registro exitoso, Scenario: Conservar un registro completado tras un fallo posterior, Scenario: Informar un fallo sin registro parcial (+12 more)

### Community 228 - "AgentOperation"
Cohesion: 0.11
Nodes (7): AgentOperation, CONSULT_HISTORY, CONSULT_MEASUREMENTS, RECORD_MEASUREMENT, RECORD_NOTE, byName(), AgentToolContract

### Community 229 - "MeasurementImportControllerTest"
Cohesion: 0.16
Nodes (6): ImportPreviewResponse, ImportPreviewRow, ImportResultResponse, MeasurementImportControllerTest, MockMultipartFile, org.springframework.mock.web.MockMultipartFile

### Community 232 - "MetricMeasurement"
Cohesion: 0.12
Nodes (8): ValueSummary, MeasurementValue, MetricMeasurement, MeasurementSummary, MeasurementFact, MeasurementFactValue, Override, MetricMeasurementTest

### Community 233 - "ChatOrchestratorTest"
Cohesion: 0.28
Nodes (3): AgentTurn, ChatOrchestratorTest, ClinicalEvent

### Community 235 - "EncounterMarkdownStore"
Cohesion: 0.15
Nodes (3): EncounterIndexRebuilder, EncounterMarkdownStore, EncounterMeasurementNoteRoundTripTest

### Community 237 - "MetricMeasurementEntity"
Cohesion: 0.08
Nodes (13): AdmittedMetricEntity, MeasurementValue, MetricMeasurementEntity, MeasurementValue, MetricMeasurementValueEntity, AdmittedMetricJpaDao, MetricMeasurementJpaDao, MeasurementValue (+5 more)

### Community 238 - "Encounter"
Cohesion: 0.14
Nodes (8): Encounter, Status, CLOSED, OPEN, EncounterMeasurementNote, EncounterNote, EncounterIndexWriter, EncounterService

### Community 239 - "ChatWorkspace.test.tsx"
Cohesion: 0.14
Nodes (9): closeConsultationMock, REFUSED, sendChatMessageMock, AbsenceReason, ChatResponse, ChatStatus, MeasurementSummary, MeasurementValue (+1 more)

### Community 240 - "Requirement: Validar y escribir dentro del camino de la operación"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Acotar las operaciones disponibles, Requirement: Decidir y ejecutar las operaciones del mensaje, Requirement: Validar y escribir dentro del camino de la operación, Scenario: Atender un mensaje que necesita más de una operación, Scenario: Conservar lo completado ante un fallo posterior, Scenario: Declarar lo que ninguna operación puede producir, Scenario: Encadenar una consulta y un registro (+6 more)

### Community 242 - "2026-09-20-uc-013/design.md"
Cohesion: 0.14
Nodes (12): Context, Decisions, Goals / Non-Goals, Migration Plan, Open Questions, Risks / Trade-offs, Capabilities, Impact (+4 more)

### Community 243 - ".query"
Cohesion: 0.17
Nodes (4): Candidate, Query, ClinicalEventIntentTest, ClinicalEventIntentValidatorTest

### Community 244 - "Requirement: Declarar la procedencia de todo hecho registrado desde una consulta"
Cohesion: 0.14
Nodes (13): clinical-fact-provenance Specification, Purpose, Requirement: Conservar la procedencia como parte de la fuente de verdad, Requirement: Declarar la procedencia de todo hecho registrado desde una consulta, Requirement: No exponer la procedencia en la conversación, Requirements, Scenario: Atribuir la procedencia a la persona, Scenario: Cubrir hecho y respuesta sin enumerar el origen (+5 more)

### Community 245 - "ADDED Requirements"
Cohesion: 0.15
Nodes (12): ADDED Requirements, Purpose, Requirement: Conservar la procedencia como parte de la fuente de verdad, Requirement: Declarar la procedencia de todo hecho registrado desde una consulta, Requirement: No exponer la procedencia en la conversación, Scenario: Atribuir la procedencia a la persona, Scenario: Cubrir hecho y respuesta sin enumerar el origen, Scenario: No alterar la procedencia de un hecho ya registrado (+4 more)

### Community 246 - "Decisions"
Cohesion: 0.12
Nodes (16): Context, D10 — El cauce de las mediciones ya existe; su lectura la entrega `UC-014b`, D1 — El seguimiento de mediciones tiene su fuente de verdad en PostgreSQL, D2 — Tabla `measurements` con dimensión de métrica y tabla hija de valores, D3 — El catálogo es un registro en código más un conjunto admitido persistido, D4 — La conversión de unidades ocurre al recoger, con equivalencias exactas del catálogo, D5 — La nota de medición vive en el documento de la consulta, D6 — El cierre escribe por cauces separados, cada uno de todo o nada (+8 more)

### Community 247 - "Decisions"
Cohesion: 0.12
Nodes (15): Context, D1 — Una operación propia para consultar el seguimiento, D2 — El ámbito `measurements` de la historia deja de redirigir y se delega, D3 — La consulta de mediciones es un servicio de solo lectura, D4 — Lectura por métricas acotada por periodo, sin tocar el esquema, D5 — La unidad de referencia y la etiqueta salen del catálogo, D6 — La aplicación resuelve la métrica y el periodo; el proveedor solo propone, D7 — Los valores, las unidades y las fechas los formatea la aplicación (+7 more)

### Community 248 - "Scenarios"
Cohesion: 0.10
Nodes (20): Coverage notes, Execution rules, Feature, Scenario: Actualizar la medición de una métrica y un día ya registrados, Scenario: Ampliar el catálogo cuando la persona menciona una métrica nueva, Scenario: Cerrar una consulta sin ninguna medición, Scenario: Informar de una medición que falla tras otra ya registrada, Scenario: No incorporar una medición que no supera la comprobación previa (+12 more)

### Community 249 - "Requirement: End the consultation from the chat surface"
Cohesion: 0.29
Nodes (6): ADDED Requirements, Requirement: End the consultation from the chat surface, Scenario: Close a consultation that collected no clinical facts, Scenario: End the consultation, Scenario: Keep the consultation internals out of the surface, Scenario: Show the closed consultation

### Community 250 - "2026-09-20-uc-013/tasks.md"
Cohesion: 0.29
Nodes (6): 1. Modelo y almacén de la consulta, 2. Persistencia de la consulta y de sus notas, 3. Recogida de notas en el turno, 4. Cierre de la consulta, 5. Contrato y superficie, 6. Documentación y verificación

### Community 251 - "ClinicalHistoryQueryService"
Cohesion: 0.14
Nodes (8): ClinicalAnswerComposer, ClinicalAnswerResult, AbsenceReason, SuggestedAction, ClinicalEventIntentValidator, ClinicalHistoryQueryService, AbsenceReason, SuggestedAction

### Community 252 - "MigrationV5BackfillsLegacyMeasurementsTest"
Cohesion: 0.19
Nodes (4): MigrationV5BackfillsLegacyMeasurementsTest, java.sql.Connection, org.flywaydb.core.Flyway, org.junit.jupiter.api.BeforeAll

### Community 253 - "Kind"
Cohesion: 0.33
Nodes (6): Kind, CLARIFICATION, CONVERSATION, DUPLICATE, FAILURE, REGISTERED

### Community 254 - "Visión de Careme"
Cohesion: 0.22
Nodes (7): El problema, Límites del asistente, Principio fundamental, Quién es el usuario, Qué es Careme, Qué significa "historia clínica personal", Visión de Careme

### Community 255 - "Requirement: Produce provider-neutral clinical intents"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Produce provider-neutral clinical intents, Scenario: Ask for clarification when the channel is undetermined, Scenario: Consult the clinical history more than once in a turn, Scenario: Keep a colloquial question in the clinical history channel, Scenario: Map a clarification, Scenario: Map a measurement candidate, Scenario: Map a mixed message to the clinical history channel (+6 more)

### Community 256 - "7. Cobertura de código"
Cohesion: 0.40
Nodes (5): 7.1 Cobertura de línea, 7.2 Cobertura de rama, 7.3 JaCoCo y el umbral del backend, 7.4 Cómo se consigue cobertura útil, 7. Cobertura de código

### Community 257 - ".answer"
Cohesion: 0.08
Nodes (15): MeasurementQueryIntent, MeasurementAnswerComposer, AbsenceReason, SuggestedAction, Kind, ANSWERED, CLARIFICATION_REQUIRED, FAILURE (+7 more)

### Community 260 - "Scenarios"
Cohesion: 0.13
Nodes (15): Coverage notes, Execution rules, Feature, Scenario: Declarar que una medición no consta, Scenario: Declarar que una métrica no forma parte del seguimiento, Scenario: Declinar una interpretación o recomendación sobre las mediciones, Scenario: Informar de un fallo al recuperar las mediciones, Scenario: Pedir que se concrete la métrica preguntada (+7 more)

### Community 263 - "UC-014 — Registrar una medición por lenguaje natural"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 264 - "UC-014b — Consultar las mediciones por lenguaje natural"
Cohesion: 0.17
Nodes (12): 10. Fuera de alcance, 11. Frontera con otros casos de uso, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones (+4 more)

### Community 265 - "Requirement: Report the outcome of the turn and the operations it went through"
Cohesion: 0.12
Nodes (15): MODIFIED Requirements, Requirement: Report the outcome of the turn and the operations it went through, Scenario: Answer general conversation without touching the history, Scenario: Keep a measurement retrieval failure out of the absence outcome, Scenario: Keep absence out of the failed outcome, Scenario: Report a completed operation when another one failed, Scenario: Report a failure with a retryable message, Scenario: Report a measurement absence distinctly from a clinical-history absence (+7 more)

### Community 266 - "MetricComponent"
Cohesion: 0.22
Nodes (3): components(), MetricComponent, MeasurementModelInvariantsTest

### Community 268 - "UC-004 — Registrar un evento clínico"
Cohesion: 0.18
Nodes (11): 10. Fuera de alcance, 1. Identificación, 2. Objetivo, 3. Precondiciones, 4. Disparador, 5. Flujo principal, 6. Flujos alternos y excepciones, 7. Postcondiciones (+3 more)

### Community 269 - "Requirement: Reconocer las preguntas sobre la propia historia clínica"
Cohesion: 0.13
Nodes (14): MODIFIED Requirements, Requirement: Reconocer las preguntas sobre la propia historia clínica, Requirement: Redactar la respuesta solo con los hechos recuperados, Scenario: Conservar la imprecisión temporal, Scenario: Mensaje que mezcla una parte general con una consulta sobre la historia, Scenario: No puede determinarse si la pregunta depende de la historia, Scenario: Pregunta coloquial sobre hechos registrados, Scenario: Pregunta que no depende de la historia (+6 more)

### Community 270 - ".process"
Cohesion: 0.25
Nodes (3): ConversationStateStore, State, Turn

### Community 272 - "Requirement: Acotar las operaciones disponibles"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Acotar las operaciones disponibles, Requirement: Decidir y ejecutar las operaciones del mensaje, Scenario: Atender un mensaje que necesita más de una operación, Scenario: Declarar lo que ninguna operación puede producir, Scenario: Encadenar una consulta y un registro, Scenario: No ejecutar una operación fuera del conjunto disponible, Scenario: No exigir a la persona que declare la operación (+3 more)

### Community 273 - "button.tsx"
Cohesion: 0.31
Nodes (4): Error(), Loading(), Button(), buttonVariants

### Community 274 - "Kind"
Cohesion: 0.50
Nodes (4): Kind, ANSWERED, FAILURE, NO_RECORDS

### Community 275 - ".rebuild"
Cohesion: 0.17
Nodes (5): ClinicalEventIndexStartupReconciler, ClinicalEvent, org.springframework.boot.context.event.ApplicationReadyEvent, org.springframework.context.event.EventListener, org.springframework.stereotype.Component

### Community 276 - "Requirement: Acotar las operaciones disponibles"
Cohesion: 0.14
Nodes (13): MODIFIED Requirements, Requirement: Acotar las operaciones disponibles, Requirement: Decidir y ejecutar las operaciones del mensaje, Scenario: Atender un mensaje que consulta el seguimiento de mediciones, Scenario: Atender un mensaje que necesita más de una operación, Scenario: Declarar lo que ninguna operación puede producir, Scenario: Encadenar una consulta y un registro, Scenario: No ejecutar una operación fuera del conjunto disponible (+5 more)

### Community 278 - "chat/lib/schema.ts"
Cohesion: 0.20
Nodes (9): absenceReasonSchema, chatEventSchema, chatResponseSchema, chatStatusSchema, measurementSummarySchema, measurementValueSchema, operationSummarySchema, suggestedActionSchema (+1 more)

### Community 279 - "CaremeBackendApplicationTests"
Cohesion: 0.31
Nodes (3): CaremeBackendApplicationTests, org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc, org.springframework.test.web.servlet.MockMvc

### Community 280 - "Requirement: Redactar la respuesta solo con los hechos recuperados"
Cohesion: 0.25
Nodes (7): MODIFIED Requirements, Requirement: Redactar la respuesta solo con los hechos recuperados, Scenario: Conservar la imprecisión temporal, Scenario: Preguntar por un hecho concreto y su fecha, Scenario: Preguntar por valores o mediciones registradas, Scenario: Responder una pregunta con hechos registrados, Scenario: Responder una pregunta que requiere varios hechos

### Community 281 - "V5__create_metric_measurements.sql"
Cohesion: 0.67
Nodes (3): admitted_metrics, measurement_values, measurements

### Community 283 - "2026-09-20-uc-014/tasks.md"
Cohesion: 0.25
Nodes (7): 1. Modelo de métrica y migración, 2. Catálogo de métricas y unidades, 3. Recogida de la medición en la consulta, 4. Cierre de la consulta, 5. Retirada de `measurement` del catálogo de hechos clínicos, 6. Compatibilidad con las vías de registro existentes, 7. Documentación y verificación

### Community 284 - "2026-09-20-uc-014/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 285 - "Requirement: Registrar hechos clínicos propios"
Cohesion: 0.29
Nodes (6): MODIFIED Requirements, Requirement: Registrar hechos clínicos propios, Scenario: No registrar una medición como hecho clínico, Scenario: Rechazar un tipo de hecho no admitido, Scenario: Registrar un hecho médico con información suficiente, Scenario: Registrar varios hechos del mismo mensaje

### Community 286 - "Requirement: Recoger y registrar las mediciones de la consulta"
Cohesion: 0.33
Nodes (5): ADDED Requirements, Requirement: Recoger y registrar las mediciones de la consulta, Scenario: Cerrar una consulta con hechos y mediciones, Scenario: Recoger una medición mencionada, Scenario: Registrar las mediciones al cerrar

### Community 288 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.20
Nodes (7): AdmittedMetricJpaRepository, Override, Measurement, Override, MetricBackedMeasurementRepository, org.springframework.stereotype.Repository, org.springframework.transaction.annotation.Transactional

### Community 290 - "2026-09-21-uc-014b/proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 291 - "2026-09-21-uc-014b/tasks.md"
Cohesion: 0.29
Nodes (6): 1. Operación de consulta y servicio de lectura, 2. Resolución de la métrica, el periodo y los casos límite, 3. Composición de la respuesta y contrato del turno, 4. Cauce de la historia clínica, 5. Interfaz de chat, 6. Documentación y verificación

### Community 294 - "Kind"
Cohesion: 0.40
Nodes (5): Kind, CLARIFICATION, CONVERSATION, EVENTS, QUERY

### Community 296 - "3. Principios de arquitectura"
Cohesion: 0.40
Nodes (5): 3.1. Markdown como representación canónica inicial, 3.2. Los eventos son la fuente primaria, 3.3. Separación entre almacenamiento e inteligencia, 3.4. Retrieval híbrido, 3. Principios de arquitectura

### Community 297 - "chat/actions.ts"
Cohesion: 0.40
Nodes (4): CloseConsultationResult, SendChatResult, chatMessageInputSchema, conversationIdSchema

### Community 298 - "3. Cómo se construye un caso de prueba"
Cohesion: 0.50
Nodes (4): 3.1 De requisito a casos, 3.2 Arrange, Act, Assert, 3.3 Dobles de prueba, 3. Cómo se construye un caso de prueba

### Community 299 - "5. Arquitectura evolutiva"
Cohesion: 0.50
Nodes (4): 5. Arquitectura evolutiva, Arquitectura avanzada, Etapa intermedia, MVP — completado el 2026-09-18

## Knowledge Gaps
- **2083 isolated node(s):** `com.careme:backend`, `CONSULT_HISTORY`, `RECORD_NOTE`, `RECORD_MEASUREMENT`, `CONSULT_MEASUREMENTS` (+2078 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ClinicalEvent` connect `ClinicalEvent` to `FakeClinicalAnswerComposerTest`, `.status`, `.rebuild`, `ChatMessageResponse`, `org.springframework.beans.factory.annotation.Autowired`, `OpenAiClinicalAnswerComposerTest`, `AgentOperationResult`, `Metric`, `.register`, `ClinicalEventQueryRepository`, `org.springframework.stereotype.Service`, `ClinicalEventMarkdownStore`, `AgentOperationExecutor`, `AgentOperationExecutorTest`, `ClinicalEventTest`, `ClinicalHistoryQueryServiceTest`, `AgentTurnRunnerTest`, `MeasurementRegistrationServiceTest`, `.execute`, `ClinicalConversationIntegrationTest`, `MeasurementController.java`, `AgentOperation`, `ChatOrchestratorTest`, `ClinicalEventRegistrationService`, `.create`, `Encounter`, `.query`, `ClinicalHistoryQueryService`?**
  _High betweenness centrality (0.020) - this node is a cross-community bridge._
- **Why does `03 — Backend: API, concurrencia y persistencia` connect `03 — Backend: API, concurrencia y persistencia` to `README.md`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Why does `ChatMessageResponse` connect `ChatMessageResponse` to `MeasurementController.java`, `.answer`, `.current`, `ChatOrchestrator`, `org.springframework.beans.factory.annotation.Autowired`, `ClinicalEvent`, `MeasurementTurnContractTest`, `.process`, `AgentOperationResult`, `Encounter`, `Metric`, `MeasurementRegistrationServiceTest`, `.status`, `CaremeBackendApplicationTests`, `ClinicalConversationIntegrationTest`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ClinicalEvent` (e.g. with `.attendsAColloquialQuestionAsAHistoryQuery()` and `.answeringAQuestionLeavesTheIndexAndTheDocumentsUntouched()`) actually correct?**
  _`ClinicalEvent` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.careme:backend`, `CONSULT_HISTORY`, `RECORD_NOTE` to the rest of the system?**
  _2083 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Requirements` be split into smaller, more focused modules?**
  _Cohesion score 0.0625 - nodes in this community are weakly interconnected._
- **Should `measurements/lib/schema.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.08571428571428572 - nodes in this community are weakly interconnected._