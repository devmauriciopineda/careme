# architecture.md — Careme

> Documento de arquitectura con dos propósitos: onboarding humano en <15 minutos
> y contexto operativo para agentes IA bajo SDD/OpenSpec.
> Cada afirmación cita el archivo que la respalda.

---

## 1. Resumen ejecutivo

Careme es una aplicación de seguimiento corporal: una persona registra un peso y
una circunferencia abdominal por día, y el sistema los muestra como tendencia y
como detalle por fecha. Se compone de dos servicios independientes —un frontend
Next.js y un backend Spring Boot— más una base de datos PostgreSQL. El backend es
la fuente de verdad del contrato HTTP; el frontend no posee los datos.

**Propuesta de valor:** seguimiento corporal mínimo, sin cuentas de usuario, con
una sola medición por día y con la precisión del dato en manos del dominio
(invariantes en el constructor del record) en lugar de la interfaz.

**Lenguaje ubicuo:**

| Término | Significado | Evidencia |
| --- | --- | --- |
| **Medición** (Measurement) | Un peso y una circunferencia abdominal de un día concreto | `backend/src/main/java/com/careme/backend/entity/Measurement.java` |
| **Día** | La fecha identifica la medición dentro de la tabla; es única y no puede ser futura | `backend/src/main/resources/db/migration/V1__create_measurements_table.sql` |
| **Cintura abdominal** (waist / abdominal circumference) | Perímetro abdominal en centímetros | `backend/src/main/java/com/careme/backend/dto/MeasurementRequest.java` |
| **Peso** (weight) | Peso en kilogramos, un decimal, máximo 500 | `backend/src/main/java/com/careme/backend/dto/MeasurementRequest.java` |
| **Registrar** | Guardar la medición de un día: crea si el día no existía, reemplaza si existía | `backend/src/main/java/com/careme/backend/service/MeasurementService.java` |
| **Medición preliminar** (MeasurementDraft) | Medición sin identidad en base de datos, usada en cargas masivas | `backend/src/main/java/com/careme/backend/entity/MeasurementDraft.java` |
| **Carga / importación** | Leer mediciones desde un archivo CSV y persistirlas | `backend/src/main/java/com/careme/backend/service/MeasurementImportService.java` |
| **Previsualización** | Informe de lo que haría una carga, sin guardar nada | `backend/src/main/java/com/careme/backend/dto/ImportPreviewResponse.java` |
| **Envoltura de respuesta** (ApiResponse) | Sobre único para toda respuesta, éxito o error | `backend/src/main/java/com/careme/backend/dto/ApiResponse.java` |

---

## 2. Contexto y alcance

Diagrama C4 nivel 1 (Context):

```mermaid
flowchart LR
    persona["Persona que hace<br/>seguimiento corporal<br/>[Persona]"]
    subgraph careme["Careme [Sistema]"]
        app["Seguimiento corporal:<br/>registra y visualiza peso<br/>y cintura abdominal"]
    end
    persona -->|"usa, HTTP<br/>navegador :3000"| app
```

**Sistemas externos:** ninguno. El backend no consume APIs de terceros, ni
brokers, ni caché, ni proveedores de identidad (`backend/README.md`, sección
*Use of APIs or external services*). La única dependencia externa de runtime es
PostgreSQL.

**Fuera del alcance del repositorio:**

- Autenticación y autorización — no existen (`frontend/README.md`: *There is no
  authentication yet*).
- Multi-usuario — el modelo no distingue mediciones de distintas personas
  (`docs/use-cases/UC-001.md`, *Fuera de alcance*).
- Objetivos, notificaciones y recomendaciones (`docs/use-cases/UC-002.md`).
- Integración continua — ⚠️ NO DETECTADO: `.github/` solo contiene `prompts/`,
  `hooks/` y `context-mode/`; no hay `.github/workflows/`. Verificar si el CI
  vive en otro proveedor.
- Convención OpenSpec — ⚠️ NO DETECTADO: no existe carpeta `openspec/`. La
  documentación funcional vive en `docs/use-cases/` (UC-001…UC-004) y
  `docs/roadmap/`. Verificar cómo verificar: `Get-ChildItem -Recurse -Force openspec`.

---

## 3. Stack tecnológico

| Capa | Tecnología | Versión | Justificación (deducible) | Fuente de verdad |
| --- | --- | --- | --- | --- |
| Frontend runtime | Next.js (App Router) | 16.3.4 | Server Components por defecto: los gráficos se derivan en servidor | `frontend/package.json` |
| Frontend UI | React | 19.2.8 | Compatibilidad Next 16 | `frontend/package.json` |
| Frontend lenguaje | TypeScript (strict) | ^5 | Contrato tipado con la API | `frontend/package.json`, `frontend/tsconfig.json` |
| Frontend estilos | Tailwind CSS | ^4 | Utilidades + tokens shadcn/ui | `frontend/package.json` |
| Frontend componentes | shadcn/ui + radix-ui | ^4.21.0 / ^1.6.7 | Primitivas accesibles ya resueltas | `frontend/components.json` |
| Frontend gráficos | Recharts | 3.8.0 | Vía el componente `chart` de shadcn/ui | `frontend/package.json` |
| Frontend validación | Zod | ^4.6.2 | Valida la respuesta de la API y el formulario | `frontend/src/features/measurements/lib/schema.ts` |
| Frontend tests | Vitest + Testing Library | ^5.0.0 | Entorno jsdom, alias `@` | `frontend/vitest.config.mts` |
| Frontend gestor | pnpm | 10.34.5 | Declarado en `packageManager` | `frontend/package.json` |
| Backend lenguaje | Java (LTS) | 21 | Pinned por enforcer | `backend/pom.xml` |
| Backend framework | Spring Boot (Web MVC, Validation, Data JPA) | 3.5.3 | Stack prescrito por el estándar del repo | `backend/pom.xml`, `docs/standards/java-springboot-standards.md` |
| Backend persistencia | Hibernate ORM | 6 (gestionado por el parent) | `ddl-auto: validate`, el esquema lo posee Flyway | `backend/src/main/resources/application.yml` |
| Backend migraciones | Flyway | Gestionado por el parent | Esquema creado en el primer arranque | `backend/src/main/resources/db/migration/` |
| Backend CSV | Apache Commons CSV | 1.14.1 | Lectura de archivos de importación | `backend/pom.xml` |
| Backend build | Maven Wrapper + enforcer | 3.9.9 / JDK 21 | Build reproducible sin Maven instalado | `backend/pom.xml`, `backend/.mvn/wrapper/` |
| Backend tests | JUnit 5, Mockito, AssertJ, MockMvc, Spring Boot Test, Testcontainers | — | Unit + capa web + integración real | `backend/pom.xml` |
| Backend cobertura | JaCoCo (gate 90 % ramas / 90 % líneas) | 0.8.12 | Bound a `verify`, no a `test` | `backend/pom.xml` |
| Datos | PostgreSQL | 17 (`postgres:17-alpine`) | Estándar del repo y de los tests de integración | `docker-compose.yml`, `backend/README.md` |
| Empaquetado | Docker multi-stage | `maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine` | Imagen no-root | `backend/Dockerfile` |
| Orquestación local | Docker/Podman Compose | — | Levanta los tres contenedores | `docker-compose.yml` |

---

## 4. Vista de contenedores (C4 nivel 2)

```mermaid
flowchart TB
    persona["Persona<br/>[Persona]"]
    subgraph careme["Careme [Sistema]"]
        fe["Frontend<br/>[Contenedor: Next.js 16 / Node]<br/>Server Components + Server Action<br/>:3000"]
        be["Backend<br/>[Contenedor: Spring Boot 3.5 / Java 21]<br/>API REST JSON :8080"]
        db[("Base de datos<br/>[Contenedor: PostgreSQL 17]<br/>tabla measurements :5432")]
    end
    persona -->|"HTTP, navegador"| fe
    fe -->|"fetch en servidor, JSON<br/>API_BASE_URL"| be
    be -->|"JDBC / Spring Data JPA<br/>síncrono"| db
```

| Contenedor | Responsabilidad | Comunicación |
| --- | --- | --- |
| **Frontend** | Renderizar tendencia y detalle diario; validar payloads antes de enviarlos; refrescar la vista tras registrar | Servidor Node: `fetch` server-side a la API (`frontend/src/services/measurementService.ts`). Entrega HTML/JS al navegador |
| **Backend** | Poseer el contrato HTTP, validar, aplicar invariantes, persistir y ordenar | Expone HTTP/JSON en `/api/v1/**` (`backend/.../controller/MeasurementController.java`); habla JDBC con PostgreSQL |
| **PostgreSQL** | Almacenar una fila por día; imponer `UNIQUE(date)` y `CHECK > 0` | Volumen `careme-pgdata`; healthcheck `pg_isready` (`docker-compose.yml`) |

**Protocolos:** todo síncrono, sin colas ni eventos. El frontend consume la API
desde el servidor (no desde el navegador), por lo que CORS no es estrictamente
necesario hoy — está configurado de forma explícita para un cliente futuro
(`backend/src/main/java/com/careme/backend/config/CorsConfig.java`).

**Arranque local** (`docker-compose.yml`): `postgres` (healthcheck) →
`backend` (`depends_on: service_healthy`, `SPRING_PROFILES_ACTIVE=dev`) →
`frontend` (`API_BASE_URL=http://backend:8080`, dentro de la red de Compose).

---

## 5. Vista de componentes (C4 nivel 3)

### 5.1 Backend (contenedor más complejo)

```mermaid
flowchart TB
    subgraph backend["Backend — Spring Boot"]
        ctrl["MeasurementController<br/>@RestController · sin lógica de negocio"]
        svc["MeasurementService<br/>ordena y mapea"]
        imp["MeasurementImportService<br/>preview + carga"]
        parser["MeasurementCsvParser<br/>lectura y validación del CSV"]
        port["MeasurementRepository<br/>(puerto)"]
        adapter["MeasurementJpaRepository<br/>(adaptador @Transactional)"]
        dao["MeasurementJpaDao<br/>Spring Data JPA"]
        entity["MeasurementEntity<br/>@Entity → tabla measurements"]
        domain["Measurement / MeasurementDraft<br/>records de dominio, invariantes"]
        dtos["dto/*<br/>MeasurementRequest · MeasurementResponse<br/>Import* · ApiResponse · ErrorResponse"]
        exc["ApiExceptionHandler<br/>@RestControllerAdvice"]
        cors["CorsConfig"]
    end
    pg[("PostgreSQL")]
    ctrl --> svc
    ctrl --> imp
    ctrl --> dtos
    imp --> parser
    svc --> port
    imp --> port
    parser --> dtos
    port --> adapter
    adapter --> dao
    adapter --> entity
    entity --> domain
    dao --> pg
    exc -.-> ctrl
    cors -.-> ctrl
```

| Módulo | Rol arquitectónico | Evidencia |
| --- | --- | --- |
| `controller/` | Adaptador de entrada: traduce HTTP ↔ DTO. No decide nada | `backend/.../controller/MeasurementController.java` |
| `service/` | Casos de uso: ordenar, decidir crear vs. reemplazar, previsualizar y cargar | `backend/.../service/MeasurementService.java`, `MeasurementImportService.java`, `MeasurementCsvParser.java` |
| `repository/` | Puerto (`MeasurementRepository`) + adaptadores JPA. Aísla el motor de persistencia | `backend/.../repository/` |
| `entity/` | Tres tipos con tres trabajos: `MeasurementEntity` (mapeo), `Measurement` (dominio sin anotaciones), `MeasurementDraft` (sin identidad) | `backend/.../entity/` |
| `dto/` | Contrato HTTP, independiente del esquema | `backend/.../dto/` |
| `config/` | CORS explícito y acotado por origen | `backend/.../config/CorsConfig.java` |
| `exception/` | Manejador global: 400/500 uniformes | `backend/.../exception/ApiExceptionHandler.java` |

### 5.2 Frontend

```mermaid
flowchart TB
    page["app/page.tsx<br/>Server Component"]
    subgraph feat["features/measurements"]
        dash["MeasurementsDashboard<br/>async server"]
        chart["MetricTrendChart<br/>client island Recharts"]
        table["MeasurementsTable"]
        form["MeasurementForm<br/>client island"]
        impc["MeasurementImport<br/>client island"]
        action["actions.ts<br/>Server Action"]
        lib["lib/ metrics · schema · strings · importMessages"]
    end
    svc["services/measurementService.ts<br/>único punto que conoce el origen de datos"]
    cfg["services/apiConfig.ts<br/>API_BASE_URL"]
    ui["components/ui<br/>primitivas shadcn/ui"]
    be["Backend :8080"]
    page --> dash
    dash --> chart
    dash --> table
    dash --> form
    dash --> impc
    dash --> svc
    dash --> lib
    form --> action
    impc --> action
    action --> svc
    svc --> cfg
    svc --> be
    action --> be
    chart --> ui
    table --> ui
```

| Módulo | Rol arquitectónico | Evidencia |
| --- | --- | --- |
| `app/` | Punto de entrada, layout, frontera de error y estilos globales | `frontend/src/app/page.tsx`, `error.tsx`, `layout.tsx` |
| `features/measurements/` | Módulo de funcionalidad autocontenido (feature-sliced) | `frontend/src/features/measurements/` |
| `features/.../lib/metrics.ts` | Registro de métricas + helpers puros (series, dominio de ejes, etiquetas) | `frontend/src/features/measurements/lib/metrics.ts` |
| `features/.../lib/schema.ts` | Esquemas Zod de la API y del formulario | `frontend/src/features/measurements/lib/schema.ts` |
| `features/.../lib/strings.ts` | Único lugar con textos de usuario (español) | `frontend/src/features/measurements/lib/strings.ts` |
| `features/.../actions.ts` | Server Action de registro + `revalidatePath("/")` | `frontend/src/features/measurements/actions.ts` |
| `services/` | Frontera de datos: `fetch` + envoltura + Zod | `frontend/src/services/measurementService.ts`, `apiConfig.ts` |
| `components/ui/` | Primitivas shadcn/ui reutilizables | `frontend/src/components/ui/` |
| `test/` | Setup de Vitest | `frontend/src/test/setup.ts` |

---

## 6. Mapa del repositorio

```text
careme/
├── backend/                        # Servicio REST (fuente de verdad del contrato)
│   ├── src/main/java/com/careme/backend/
│   │   ├── controller/             # Adaptador de entrada HTTP
│   │   ├── service/                # Casos de uso y parseo CSV
│   │   ├── repository/             # Puerto de datos + adaptadores JPA
│   │   ├── entity/                 # Dominio (records) + mapeo JPA
│   │   ├── dto/                    # Contrato HTTP
│   │   ├── config/                 # CORS
│   │   ├── exception/              # Manejador global de errores
│   │   └── CaremeBackendApplication.java
│   ├── src/main/resources/         # application.yml + perfiles + migraciones Flyway
│   ├── src/test/java/              # Espeja la estructura de paquetes
│   ├── Dockerfile                  # Imagen multi-stage no-root
│   └── pom.xml
├── frontend/                       # SPA/SSR Next.js
│   ├── src/app/                    # Entrada, layout, error, estilos
│   ├── src/components/ui/          # Primitivas shadcn/ui
│   ├── src/features/measurements/  # Módulo de la funcionalidad
│   ├── src/services/               # Frontera de datos (fetch + Zod)
│   ├── src/test/                   # Setup de tests
│   └── e2e/playwright/             # Reservado; sin tests todavía
├── docs/                           # Documentación del proyecto
│   ├── standards/                  # Estándares de Java/Spring y de Next
│   ├── use-cases/                  # UC-001…UC-004
│   ├── roadmap/                    # Roadmap y alcance del MVP del asistente clínico
│   ├── data-model.md               # Modelo de datos
│   └── architecture.md             # Este documento
├── .github/                        # prompts/, hooks/ y context-mode/ (sin workflows)
├── graphify-out/                   # Grafo de conocimiento del repo para agentes IA
└── docker-compose.yml              # postgres + backend + frontend
```

Rol arquitectónico de las carpetas de primer nivel:

- `backend/` y `frontend/`: despliegues independientes; solo se comunican por
  HTTP. Ninguno importa código del otro.
- `docs/`: fuente de requisitos y estándares. Un cambio de regla de negocio debe
  reflejarse aquí.
- `.github/`: configuración del asistente de IA (prompts, hooks), no del
  producto.
- `graphify-out/`: artefacto de ingeniería de contexto (grafo de código). Se
  regenera; no es fuente de verdad.

---

## 7. Patrones arquitectónicos

**Patrón principal: por capas (Layered) con puerto/adaptador en persistencia
(Hexagonal parcial).**

- El backend sigue `controller → service → repository`, explícito en
  `backend/README.md` y verificable en los paquetes de
  `backend/src/main/java/com/careme/backend/`.
- El dominio (`Measurement`) es un `record` sin anotaciones de persistencia y con
  invariantes en su constructor compacto
  (`backend/.../entity/Measurement.java`). `MeasurementEntity` mapea la tabla y
  solo ella; `MeasurementResponse` es el contrato HTTP. Tres tipos, tres
  responsabilidades.
- `MeasurementRepository` es un **puerto** (`backend/.../repository/MeasurementRepository.java`)
  implementado por `MeasurementJpaRepository`; el servicio depende de la interfaz,
  no de JPA. Es la evidencia del componente hexagonal.

**Reglas de dependencia:**

| Desde | Puede importar | No puede importar |
| --- | --- | --- |
| `controller` | `dto`, `service`, `exception` | `repository`, `entity` (JPA) |
| `service` | `dto`, `entity` (dominio), `repository` (interfaz) | `controller`, `MeasurementJpaDao` |
| `repository` (adaptador) | `entity`, JPA | `controller`, `service` |
| `entity` (dominio) | Solo JDK | Cualquier capa de aplicación |

- **Frontend:** arquitectura por capas de render + feature-sliced. Server
  Components por defecto; `MetricTrendChart` es la única isla cliente por
  Recharts y `MeasurementForm`/`MeasurementImport` por interacción
  (`frontend/README.md`, *Architecture notes*). La frontera de datos está
  concentrada en `services/measurementService.ts`.
- **Reactividad:** Server Actions + `revalidatePath("/")` en lugar de una capa de
  estado en cliente (`frontend/src/features/measurements/actions.ts`).

**Anti-patrones y deuda técnica detectada:**

| Hallazgo | Evidencia | Impacto |
| --- | --- | --- |
| Documentación desactualizada en el backend: afirma «Read-only by design. There are no write endpoints», pero existen `POST` y endpoints de importación | `backend/README.md` (*Additional notes*) vs. `backend/.../controller/MeasurementController.java` | Medio: confunde a agentes y a nuevos desarrolladores |
| CORS permite solo `GET`, pero la API expone `POST` y `POST` multipart | `backend/.../config/CorsConfig.java` (`.allowedMethods("GET")`) vs. `MeasurementController.java` | Bajo hoy (el fetch es server-side); bloquea un cliente de navegador |
| Sin autenticación ni autorización en toda la API | `backend/README.md`; `frontend/README.md` | Alto si los datos salen del entorno local |
| Sin integración continua | `.github/` sin `workflows/` | Medio: los gates de cobertura solo corren localmente |
| `e2e/playwright/` reservado y vacío; Storybook, Playwright, TanStack Query, Zustand, i18n en runtime y modo oscuro pendientes | `frontend/README.md`, *Deferred from the frontend standard* | Bajo: adopción aditiva prevista |
| Persistencia de datos clínicos futura sin modelo implementado (solo documentado) | `docs/roadmap/mvp_alcance_asistente_historia_clinica.md` | Informativo |

---

## 8. Decisiones arquitectónicas (ADR-light)

No existen ADRs formales en el repositorio — ⚠️ NO DETECTADO: no hay carpeta
`docs/adr/` ni archivos `adr-*.md`. Las decisiones siguientes se infieren del
código y de los README.

| ID | Decisión | Estado | Contexto | Consecuencias |
| --- | --- | --- | --- | --- |
| ADR-001 | Flyway posee el esquema; Hibernate solo valida (`ddl-auto: validate`) | Vigente | Evitar DDL implícito | Un desajuste entre mapeo y migración falla al arrancar, no en silencio |
| ADR-002 | Separar dominio (`Measurement`), mapeo (`MeasurementEntity`) y contrato (`MeasurementResponse`) | Vigente | El contrato JSON no debe moverse al cambiar el esquema | Más clases por entidad; aislamiento real entre capas |
| ADR-003 | `MeasurementRepository` como interfaz (puerto) ante JPA | Vigente | Cambiar el motor de persistencia sin tocar el contrato | Indirección extra; testeabilidad alta |
| ADR-004 | Una medición por día: `date` única; registrar reemplaza | Vigente | Evitar duplicados y ambigüedad de "la del día" | Registrar dos veces al día sobrescribe; requiere migración para cambiarlo |
| ADR-005 | Envoltura única `ApiResponse` para todo resultado | Vigente | Respuestas predecibles para el cliente | El cliente debe desenvolver siempre |
| ADR-006 | Frontend consume la API desde el servidor; `API_BASE_URL` sin prefijo `NEXT_PUBLIC_` | Vigente | El navegador nunca necesita la URL del backend | CORS no es necesario hoy; hay que documentarlo al añadir llamadas de navegador |
| ADR-007 | Server Components por defecto; islas cliente mínimas | Vigente | Evitar enviar lógica y `Date` al navegador | Las etiquetas de fecha no se desplazan por zona horaria |
| ADR-008 | Validación de importación todo-o-nada, reutilizando `MeasurementRequest` | Vigente | Un archivo no puede aceptar lo que el formulario rechaza | Archivos grandes con un error no cargan nada; una sola fuente de reglas |
| ADR-009 | Tests de integración contra PostgreSQL real (Testcontainers), no H2 | Vigente | Probar el esquema de producción | Requiere runtime de contenedores en local |
| ADR-010 | Gate de cobertura (90 % ramas/líneas) atado a `verify`, no a `test` | Vigente | No bloquear ciclos rápidos de test | `mvn test` puede pasar con cobertura insuficiente |
| ADR-011 | Java 21 fijado por `maven-enforcer-plugin` y wrapper de Maven commiteado | Vigente | Build reproducible | Builds con otro JDK fallan explícitamente |
| ADR-012 | Textos de usuario en español aislados en `strings.ts`; código en inglés | Vigente | i18n futura sin refactor | Disciplina manual; sin verificación automática |
| ADR-013 | `Patient` implícito y eventos clínicos en Markdown + índice PostgreSQL | Propuesto | MVP del asistente de historia clínica | No implementado; si se adopta, cambia el modelo de persistencia |

---

## 9. Seguridad y cumplimiento

**Autenticación y autorización:** ninguna. No hay login, tokens, sesiones ni
roles en ninguno de los dos servicios (`backend/README.md`; `frontend/README.md`).
Toda la API es anónima y abierta a quien alcance el puerto 8080.

**Gestión de secretos:** por variables de entorno, con valores por defecto solo
en desarrollo.

| Variable | Uso | Default |
| --- | --- | --- |
| `CAREME_DB_URL` | URL JDBC | `jdbc:postgresql://localhost:5432/careme` |
| `CAREME_DB_USER` / `CAREME_DB_PASSWORD` | Credenciales de base de datos | `careme` / `careme` |
| `CAREME_CORS_ALLOWED_ORIGINS` | Orígenes permitidos en `pre`/`prd` | Sin default: la app no arranca sin ella |
| `API_BASE_URL` (frontend) | URL del backend para el fetch en servidor | `http://localhost:8080` |

Evidencia: `backend/src/main/resources/application.yml`,
`backend/src/main/resources/application-{dev,pre,prd}.yml`, `frontend/.env.example`.
Las credenciales de `docker-compose.yml` (`careme`/`careme`) son de desarrollo
local y no deben reutilizarse fuera de él.

**Superficies expuestas y validaciones:**

| Superficie | Validación | Evidencia |
| --- | --- | --- |
| `POST /api/v1/measurements` | Bean Validation: fecha no futura, métricas positivas, límites 500/400, un decimal | `backend/.../dto/MeasurementRequest.java` |
| Invariantes de dominio | Constructor compacto del `record` rechaza valores nulos o no positivos | `backend/.../entity/Measurement.java` |
| Base de datos | `UNIQUE(date)` + `CHECK (weight_kg > 0)` + `CHECK (waist_cm > 0)` | `db/migration/V1__create_measurements_table.sql` |
| Carga de archivo | Multipart limitado a 10 MB (11 MB de request), máximo 10000 filas, cabeceras exactas, validación fila a fila antes de persistir | `application.yml`, `MeasurementCsvParser.java` |
| Errores | Manejador global: 400 `VALIDATION_ERROR` / `INVALID_REQUEST`, 500 `INTERNAL_ERROR`; sin detalles internos al cliente | `backend/.../exception/ApiExceptionHandler.java` |

**CORS:** mapeo `/api/**` restringido a orígenes configurados y solo método
`GET` (`CorsConfig.java`). No es una defensa de seguridad por sí mismo: no
sustituye a la autenticación.

**Cumplimiento:** no hay requisitos implementados. El producto trata datos de
salud personales, por lo que la evolución prevista incluye privacidad, cifrado,
auditoría y control de acceso
(`docs/roadmap/roadmap_asistente_historia_clinica.md`, §2.5) — ⚠️ NO DETECTADO en
código: ninguna de esas capacidades existe hoy. Antes de exponer el sistema fuera
de un entorno local hay que confirmar el marco regulatorio aplicable (p. ej.
GDPR si hay usuarios en la UE) y decidir autenticación, cifrado en tránsito y en
reposo, y política de retención.

---

Generado: 2026-09-12
Commit analizado: 84223a1f7d8bd67c262251536b02294a4699171a
Autor del análisis: GitHub Copilot
Próxima revisión sugerida: 2026-12-12
