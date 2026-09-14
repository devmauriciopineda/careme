# Base de Conocimiento del Código

Esta carpeta reúne una guía de referencia sobre los conceptos y mecanismos que sustentan el código de este repositorio: cómo se separan las responsabilidades, cómo viajan los datos, qué ocurre internamente en cada operación y qué propiedades se verifican antes de considerar una modificación terminada.

Está dirigida a desarrolladores con formación en sistemas. Los documentos explican cada tema desde su definición y su problema hasta su funcionamiento interno, con ejemplos breves cuando aclaran una idea.

## Enfoque Metodológico

Cada documento es una referencia conceptual vinculada a una parte del sistema. Cada tema se desarrolla respondiendo a tres preguntas:

1. **¿Qué es?** Definición precisa del concepto y de los términos necesarios para leerlo.
2. **¿Por qué se utiliza aquí?** Problema que resuelve en este proyecto y límites de la decisión.
3. **¿Qué ocurre bajo el capó?** Flujo de datos, ciclo de vida, contratos, gestión de estado, persistencia, concurrencia o mecanismo de ejecución que convierte la abstracción en comportamiento observable.

## Mapa de Ruta

La guía se organiza en siete documentos independientes. El orden es sugerido; cada archivo puede consultarse como referencia cuando una tarea del repositorio lo requiera.

1. [`01-estructura-y-arquitectura-del-repositorio.md`](./01-estructura-y-arquitectura-del-repositorio.md) — Cómo se distribuyen frontend, backend, base de datos, documentación y configuración; responsabilidades de cada zona; arquitectura por capas del backend; módulos por funcionalidad del frontend; límites entre dominio, transporte y persistencia; y recorrido general de una petición.
2. [`02-frontend-renderizado-y-comunicacion.md`](./02-frontend-renderizado-y-comunicacion.md) — App Router, Server Components, Client Components, Server Actions y estrategia de renderizado; serialización e hidratación; obtención y validación de datos; comunicación server-side con la API; estados de error y actualización de la interfaz.
3. [`03-backend-api-concurrencia-y-persistencia.md`](./03-backend-api-concurrencia-y-persistencia.md) — Ciclo de una petición HTTP en Spring Boot; controllers, DTOs, validación, servicios, dominio y repositorios; ejecución multihilo de un servidor web; transacciones; JPA/Hibernate; migraciones Flyway; invariantes y contrato de la API.
4. [`04-datos-relacionales-y-fuentes-derivadas.md`](./04-datos-relacionales-y-fuentes-derivadas.md) — Modelo relacional de PostgreSQL, restricciones y consistencia; diferencia entre fuente de verdad e índice derivado; documentos Markdown como fuente de verdad e índice clínico reconstruible; estructuras de búsqueda utilizadas (árboles B para clave primaria, unicidad y orden por fecha; índice invertido GIN sobre un vector de texto) y búsqueda de texto completo con `tsvector`, consultas `tsquery` y orden por relevancia `ts_rank`; coste de mantener y reconstruir un índice derivado; ACID y aislamiento transaccional.
5. [`05-ia-llm-prompts-y-rag.md`](./05-ia-llm-prompts-y-rag.md) — Qué es un LLM y cómo genera texto; tokens, contexto y probabilidad; diseño y versionado de prompts; salidas estructuradas; RAG y recuperación de hechos; recuperación léxica real del proyecto (construcción de la consulta a partir de las palabras del usuario, filtrado por metadatos de tipo y rango de fechas, orden por relevancia y límite de resultados, con búsqueda por metadatos como alternativa cuando no hay coincidencias de texto); composición de una respuesta fundamentada y citada; memoria conversacional, límites, fallos y controles necesarios para datos clínicos.
6. [`06-sdd-y-openspec.md`](./06-sdd-y-openspec.md) — Desarrollo guiado por especificaciones (SDD): qué es una especificación como fuente de referencia, cómo se relacionan requisitos, cambios y código, y de qué manera una especificación restringe y orienta la implementación; papel de OpenSpec y estructura de especificaciones y cambios en el repositorio.
7. [`07-calidad-y-despliegue.md`](./07-calidad-y-despliegue.md) — Estrategia de pruebas, linting, tipado, cobertura, verificaciones de integración y criterios conceptuales de preparación para despliegue; qué propiedad del sistema comprueba cada verificación y qué queda fuera de su alcance.
