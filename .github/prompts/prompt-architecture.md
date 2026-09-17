# Rol

Actúas como un Arquitecto de Software Senior especializado en ingeniería de contexto para agentes de IA (Context Engineering), con experiencia en Spec-Driven Development (SDD) y en la convención OpenSpec (carpetas `openspec/`, archivos `proposal.md`, `spec.md`, `tasks.md`, `design.md`).

# Objetivo

Analizar el repositorio existente en el que estás trabajando y generar un único archivo llamado `architecture.md` en la raíz del proyecto (o en `/docs/architecture.md` si ya existe una carpeta `docs/`). Este archivo debe cumplir DOS propósitos simultáneos:

1. **Documentación humana**: que cualquier desarrollador nuevo entienda el sistema en <15 minutos.
2. **Contexto operativo para agentes IA bajo SDD/OpenSpec**: que un agente (GitHub Copilot, Claude, Cursor, etc.) pueda leerlo y, junto con los specs en `openspec/`, producir propuestas, diseños, tareas e implementaciones coherentes con la arquitectura existente.

# Proceso de análisis (obligatorio antes de escribir)

Antes de redactar, inspecciona el repositorio y recopila evidencia real (no inventes):

1. **Stack y dependencias**: lee `package.json`, `pyproject.toml`, `go.mod`, `pom.xml`, `Cargo.toml`, `composer.json`, etc.
2. **Estructura de carpetas**: lista los directorios de primer y segundo nivel; identifica capas (`domain`, `application`, `infrastructure`, `adapters`, `controllers`, `services`, `hooks`, `components`).
3. **Puntos de entrada**: `main.*`, `index.*`, `app.*`, `server.*`, `cmd/*`, `src/pages/*`, `app/layout.*`.
4. **Rutas/endpoints**: busca routers, controllers, `routes.*`, `api/`, GraphQL schemas, OpenAPI/Swagger.
5. **Modelo de datos**: esquemas ORM, migraciones, `schema.prisma`, `models/`, `entities/`, `migrations/`.
6. **Configuración y entorno**: `.env.example`, `config/`, `docker-compose.yml`, `Dockerfile`, CI/CD (`.github/workflows/`).
7. **Testing**: framework, ubicación, cobertura, estrategia (unit/integration/e2e).
8. **OpenSpec existente**: si ya hay carpeta `openspec/`, léela para alinear terminología, no la dupliques.
9. **Convenciones de código**: linters, formatters, `tsconfig.json`, `.editorconfig`, husky, etc.
10. **Documentación previa**: `README.md`, `CONTRIBUTING.md`, ADRs existentes.

Si algo no es deducible, márcalo explícitamente como `⚠️ NOT DETECTED — requires human confirmation` en lugar de inventarlo.

# Estructura obligatoria del `architecture.md`

El documento se redacta **en inglés**: los títulos de sección, las columnas de tabla, los
comentarios del árbol y el pie son cadenas literales en inglés, tal como se listan aquí. Si una
sección no aplica, déjala con `N/A` y una justificación breve de una línea.

Genera el archivo con EXACTAMENTE estas secciones, con estos títulos literales (usa encabezados H2).

## 1. Executive summary
- 3–6 líneas: qué hace el sistema, para quién, y su propuesta de valor.
- Lenguaje ubicuo (glosario de 5–10 términos del dominio).

## 2. Context and scope
- Diagrama C4 nivel 1 (Context) en Mermaid (`flowchart` o `C4Context`).
- Sistemas externos con los que interactúa (APIs, DBs, colas, auth providers).
- Qué está **fuera** del alcance del repositorio.

## 3. Technology stack
Tabla con estas columnas: `Layer | Technology | Version | Rationale (deducible) | Source of truth` (ej. `package.json:42`).

## 4. Container view (C4 level 2)
- Diagrama Mermaid de contenedores (frontend, backend, workers, DB, cache, broker…).
- Responsabilidad de cada contenedor y cómo se comunican (protocolo, sincronía/async).

## 5. Component view (C4 level 3)
- Solo para los contenedores más complejos.
- Diagrama Mermaid + descripción por módulo.

## 6. Repository map
Árbol comentado (los comentarios, en inglés):
```
src/
├── domain/          # Pure business rules, no external dependencies
├── application/     # Use cases, orchestration
├── infrastructure/  # Adapters: DB, HTTP, queues
└── ...
```
Regla: cada carpeta listada debe existir realmente; explica su rol arquitectónico (no solo qué contiene).

## 7. Architectural patterns
- Patrón principal (Hexagonal / Clean / Layered / Microservicios / Event-driven…).
- Justificación y evidencia en el código.
- Reglas de dependencia entre capas (qué puede importar a qué).
- Anti-patrones conocidos o deuda técnica detectada.

## 8. Architectural decisions (ADR-light)
Tabla con estas columnas: `ID | Decision | Status | Context | Consequences`.
Si existen ADRs formales, enlázalos en lugar de duplicar.

## 9. Security and compliance
- Autenticación / autorización / gestión de secretos.
- Superficies expuestas y validaciones.
- Cumplimiento relevante (GDPR, PCI, etc.) si aplica.

# Reglas de estilo del documento

- **Idioma**: inglés técnico claro. Todo el documento va en inglés: encabezados, columnas de
tabla, comentarios del árbol y pie, con las cadenas literales indicadas arriba.
- **Longitud**: la necesaria para cubrir todo; prioriza densidad sobre prosa. No repitas el README.
- **Evidencia**: cada afirmación arquitectónica debe citar el archivo que la respalda (`ruta:línea` cuando sea posible).
- **Diagramas**: usa Mermaid. No uses imágenes externas.
- **Tablas**: úsalas para todo lo que sea comparativo o enumerable.
- **Versionado del documento**: incluye al final un bloque con estas etiquetas literales:
  ```
  ---
  Generated: <fecha ISO>
  Analysed commit: <hash si está disponible>
  Analysis author: <agente IA>
  Suggested next review: <fecha + 3 meses>
  ```

- **No inventes**: si no puedes deducir algo, escribe `⚠️ NOT DETECTED — requires human confirmation` y describe cómo verificarlo.

# Entregable

1. Crea el archivo `architecture.md` con todo lo anterior.
3. No modifiques ningún otro archivo del repositorio.

