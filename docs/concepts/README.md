# Base de Conocimiento del Código

Esta carpeta reúne una guía de referencia sobre los conceptos y mecanismos que sustentan el código de este repositorio: cómo se separan las responsabilidades, cómo viajan los datos, qué ocurre internamente en cada operación y qué propiedades se verifican antes de considerar una modificación terminada.

Está dirigida a desarrolladores con formación en sistemas. Los documentos explican cada tema desde su definición y su problema hasta su funcionamiento interno, con ejemplos breves cuando aclaran una idea.

## Enfoque Metodológico

Cada documento es una referencia conceptual vinculada a una parte del sistema. Cada tema se desarrolla respondiendo a tres preguntas:

1. **¿Qué es?** Definición precisa del concepto y de los términos necesarios para leerlo.
2. **¿Por qué se utiliza aquí?** Problema que resuelve en este proyecto y límites de la decisión.
3. **¿Qué ocurre bajo el capó?** Flujo de datos, ciclo de vida, contratos, gestión de estado, persistencia, concurrencia o mecanismo de ejecución que convierte la abstracción en comportamiento observable.

## Mapa de Ruta

La guía se organiza en ocho documentos independientes. El orden es sugerido; cada archivo puede consultarse como referencia cuando una tarea del repositorio lo requiera.

1. [`01-estructura-y-arquitectura-del-repositorio.md`](./01-estructura-y-arquitectura-del-repositorio.md) — Cómo se distribuyen frontend, backend, base de datos, documentación y configuración; responsabilidades de cada zona; arquitectura por capas del backend; módulos por funcionalidad del frontend; límites entre dominio, transporte y persistencia; y recorrido general de una petición.
2. [`02-frontend-renderizado-y-comunicacion.md`](./02-frontend-renderizado-y-comunicacion.md) — Stack del frontend y responsabilidades de React, Next.js y TypeScript; estructura del proyecto y organización por features; modelos de renderizado y estrategia server-first; comunicación validada con el backend; Server Actions; estilos, tokens, componentes reutilizables y estados de la interfaz.
3. [`03-backend-api-concurrencia-y-persistencia.md`](./03-backend-api-concurrencia-y-persistencia.md) — Responsabilidades del backend; stack Spring Boot, Spring Web MVC, Bean Validation, Hibernate, Flyway y PostgreSQL; arquitectura por capas, DTOs, servicios, repositorios y adaptadores; servidores bloqueantes y reactivos; ciclo HTTP, transacciones, contexto de persistencia, concurrencia, errores y lectura de hechos clínicos.
4. [`04-datos-relacionales-y-fuentes-derivadas.md`](./04-datos-relacionales-y-fuentes-derivadas.md) — Persistencia relacional con PostgreSQL; restricciones, tipos, MVCC y transacciones; relación entre Spring Data JPA, JPA e Hibernate; fuente de verdad Markdown, índices clínicos derivados y vistas derivadas que conservan un contrato; búsqueda full-text con `tsvector`, `tsquery`, `ts_rank` y GIN; contexto de `LIKE`, trigramas, BM25, embeddings y búsqueda híbrida; reconstrucción y coherencia entre sistemas.
5. [`05-ia-llm-prompts-y-rag.md`](./05-ia-llm-prompts-y-rag.md) — LLMs, tokens, prompts y roles de mensajes (`system`, `user`, `assistant` y `tool`); flujo completo de una consulta; puertos y adaptadores de proveedores; el asistente como agente con un conjunto declarado de operaciones y un bucle de decisión dentro del turno; patrones de RAG y RAG agentivo del proyecto; recuperación léxica con filtros, composición fundamentada y validación; memoria multiturno —el búfer efímero de `ConversationStateStore` y la consulta persistida que recoge notas—; límites y controles para datos clínicos.
6. [`06-sdd-y-openspec.md`](./06-sdd-y-openspec.md) — Desarrollo guiado por especificaciones; diferencia entre requisitos, escenarios, propuestas, diseños y tareas; estructura y ciclo de vida de OpenSpec; deltas `ADDED`, `MODIFIED`, `REMOVED` y `RENAMED`; trazabilidad entre requisitos, pruebas y código; sincronización, archivo e historial; y relación con BDD, ADR, TDD y otros enfoques.
7. [`07-calidad.md`](./07-calidad.md) — Definición de calidad, oráculos y diseño de casos de prueba; secuencia y tipos de pruebas —unitarias, de capa, integración, contrato, extremo a extremo y medición sobre un corpus de evaluación—; ejemplos reales del backend y frontend; cobertura de línea y rama con JaCoCo; verificación estática con linting y tipado; y contexto de pruebas de rendimiento, seguridad, accesibilidad, mutación y resiliencia.
8. [`08-despliegue.md`](./08-despliegue.md) — Despliegue como proceso de convertir código validado en artefactos ejecutables; imágenes multietapa, contenedores, redes y volúmenes; configuración, secretos y perfiles; orquestación Compose, healthchecks y readiness; migraciones Flyway, validación Hibernate, reconstrucción del índice y continuidad de Markdown; seguridad de runtime, observabilidad, estrategias de despliegue y validación posterior.
