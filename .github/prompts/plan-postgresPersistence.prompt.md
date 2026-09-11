# Plan: Persistencia Postgres para measurements (PK UUID)

Sustituir `MeasurementFileRepository` (JSON en classpath, cacheado en memoria) por PostgreSQL con
Spring Data JPA y Flyway, dejando la tabla `measurements` vacía. El contrato HTTP no cambia:
Jackson serializa el `UUID` como string, y `schema.ts` del frontend exige `id: z.string().min(1)`.

## Contexto

Repo: careme (Spring Boot 3.5.3, Java 21, `backend/` + `frontend/` Next.js).
Hoy: `MeasurementFileRepository` lee `classpath:data/measurements.json` y lo cachea en memoria
(read-only). Estándar de referencia: `docs/standards/java-springboot-standards.md`
(Postgres + JPA + Hibernate 6 + Flyway, líneas 30-37 y 782-800).

**Restricción dura:** `frontend/src/features/measurements/lib/schema.ts` valida
`id: z.string().min(1)`. Por eso el `id` sigue siendo string en el JSON aunque la PK sea `UUID`.

## Decisiones

1. Alcance: SOLO persistencia de lectura. Se mantiene `GET /api/v1/measurements`. Sin POST/PUT.
2. Identidad: `UUID` como PK, con `UNIQUE(date)` (un registro por día).
3. Semilla: tabla vacía, se borra `measurements.json`. El dashboard mostrará el estado vacío.
4. Tests: Testcontainers con Postgres, sin H2.
5. Se elimina la rama 404 porque ya no hay fichero que pueda faltar.

## Fase 1 — Dependencias y esquema

1. `backend/pom.xml`: añadir `spring-boot-starter-data-jpa`; `org.postgresql:postgresql` (runtime);
   `flyway-core` + `flyway-database-postgresql`; en test `spring-boot-testcontainers`,
   `org.testcontainers:postgresql`, `org.testcontainers:junit-jupiter`.
   Versiones del BOM del parent 3.5.3.
2. Nueva migración `backend/src/main/resources/db/migration/V1__create_measurements_table.sql`
   con la tabla `measurements`:

| Columna     | Tipo          | Restricción                              |
| ----------- | ------------- | ---------------------------------------- |
| `id`        | `UUID`        | `PRIMARY KEY DEFAULT gen_random_uuid()`   |
| `date`      | `DATE`        | `NOT NULL`, `UNIQUE`                     |
| `weight_kg` | `NUMERIC(5,2)` | `NOT NULL CHECK > 0`                    |
| `waist_cm`  | `NUMERIC(5,2)` | `NOT NULL CHECK > 0`                    |

Solo V1. Sin migración de seed.

## Fase 2 — Capa de persistencia

3. Nuevo `backend/src/main/java/com/careme/backend/entity/MeasurementEntity.java`:
   `@Entity @Table(name="measurements")`, `@Id @GeneratedValue(strategy = GenerationType.UUID)`,
   constructor sin args para JPA y mapeo explícito `toDomain()` / `from(Measurement)`.
   *(depende de 2)*
4. Nuevo `backend/src/main/java/com/careme/backend/repository/MeasurementJpaDao.java`:
   `interface MeasurementJpaDao extends JpaRepository<MeasurementEntity, UUID>`.
5. Nuevo `backend/src/main/java/com/careme/backend/repository/MeasurementJpaRepository.java`:
   `@Repository`, implementa el contrato existente `MeasurementRepository`,
   `@Transactional(readOnly = true)`, mapea entidad JPA → record de dominio.
   *(depende de 3 y 4 — paralelo con 6)*
6. Cambiar `String` → `UUID` en el id:
   - `backend/src/main/java/com/careme/backend/entity/Measurement.java`: invariante pasa de
     `isBlank` a `!= null`.
   - `backend/src/main/java/com/careme/backend/dto/MeasurementResponse.java`: `UUID id`.
7. Borrar `backend/src/main/java/com/careme/backend/repository/MeasurementFileRepository.java` y
   `backend/src/main/resources/data/measurements.json`. Quitar `careme.measurements.data-location`
   de `application.yml`.

## Fase 3 — Configuración y runtime

8. `backend/src/main/resources/application.yml`: `spring.datasource.url` con default
   `jdbc:postgresql://localhost:5432/careme` y credenciales desde `CAREME_DB_*`;
   `spring.jpa.hibernate.ddl-auto: validate`; `spring.jpa.open-in-view: false`; Flyway activo.
9. `backend/src/main/resources/application-dev.yml`: datasource local + logging DEBUG (ya existe).
   *(paralelo con 10)*
10. `application-pre.yml` / `application-prd.yml`: `CAREME_DB_URL`, `CAREME_DB_USER` y
    `CAREME_DB_PASSWORD` obligatorios.
11. `docker-compose.yml`: servicio `postgres` (imagen `postgres:17-alpine`),
    `POSTGRES_DB/USER/PASSWORD=careme`, volumen nombrado `careme-pgdata`, healthcheck `pg_isready`;
    en `backend` añadir `depends_on: postgres: condition: service_healthy` y
    `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/careme`.

## Fase 4 — Tests

12. Nueva base `backend/src/test/java/com/careme/backend/PostgresIntegrationTest.java`:
    `@Testcontainers` + `@Container @ServiceConnection static PostgreSQLContainer` (`postgres:17-alpine`),
    contenedor único reutilizado. **Verificar antes que Testcontainers alcanza el runtime de contenedores.**
13. Reescribir `backend/src/test/java/com/careme/backend/CaremeBackendApplicationTests.java`:
    extiende la base, siembra vía `MeasurementJpaDao`, verifica envelope, count, orden cronológico
    y preflight CORS. Ya no lee `ClassPathResource`. *(depende de 12)*
14. Borrar `.../repository/MeasurementFileRepositoryTest.java`. Nuevo
    `.../repository/MeasurementJpaRepositoryTest.java`: tabla vacía → lista vacía, mapeo de campos,
    duplicado de `date` → `DataIntegrityViolationException`, métrica negativa rechazada por el CHECK.
    *(paralelo con 13)*
15. Ajustar fixtures de id en `MeasurementTest`, `MeasurementServiceTest` y `MeasurementControllerTest`.
16. `./mvnw verify` con el gate JaCoCo de 90% branch y línea sobre las clases nuevas.

## Fase 5 — Limpieza y documentación

17. Eliminar `backend/src/main/java/com/careme/backend/exception/ResourceNotFoundException.java` y su
    `@ExceptionHandler` en `ApiExceptionHandler` (deja de ser alcanzable; el 500 genérico se mantiene).
18. Actualizar `backend/README.md` (arquitectura, tabla de configuración, errores, run guide, tabla de
    tests, notas) y `README.md` raíz (diagrama de arquitectura, paso 4 de "Basic usage", notas).

## Archivos relevantes

- `backend/src/main/java/com/careme/backend/repository/MeasurementRepository.java` — contrato que
  **no cambia**; `MeasurementJpaRepository` lo implementa.
- `backend/src/main/java/com/careme/backend/service/MeasurementService.java` — sin cambios; ya ordena
  en memoria con `Comparator.comparing(Measurement::date)`.
- `backend/src/main/java/com/careme/backend/exception/ApiExceptionHandler.java` — quitar la rama 404.
- `backend/src/main/resources/data/measurements.json` — 56 filas fixture, se elimina.
- `frontend/src/features/measurements/lib/schema.ts` — fuente de la restricción de contrato, sin cambios.

## Verificación

1. `cd backend && ./mvnw verify` → tests verdes y gate JaCoCo.
2. `podman compose up --build postgres backend` →
   `curl http://localhost:8080/api/v1/measurements` devuelve `data: []` con `success: true`.
3. `\d measurements` en psql → columnas y constraints; `SELECT * FROM flyway_schema_history` → V1 aplicada.
4. Insertar dos filas con la misma `date` por psql → error de unicidad.
5. `cd frontend && pnpm test` sigue verde.
6. Dashboard en http://localhost:3000 → estado vacío "Todavía no hay mediciones registradas."
   Insertar 1 fila por psql y recargar → tabla y gráfico renderizan (prueba que el UUID string pasa
   `z.string()`).
7. Buscar `measurements.json`, `data-location` y `MeasurementFileRepository` → sin referencias vivas
   (ignorar `.github/prompts/*` y `target/`).

## Further Considerations

1. **Runtime de contenedores:** el repo usa `podman compose`, pero Testcontainers necesita la API de
   Docker. Opción A: apuntar `DOCKER_HOST` al socket de Podman (recommended). Opción B: usar Docker
   Desktop en local. Opción C: posponer la Fase 4 y validar solo con el Postgres del compose.
2. **`UNIQUE(date)`:** asume un único registro por día. Opción A: mantenerla (recommended, encaja con
   "registro diario"). Opción B: quitarla si prevés varios pesajes el mismo día.
3. **`ResourceNotFoundException`:** se elimina en el paso 17. Opción A: eliminarla (recommended, hoy es
   código muerto). Opción B: conservarla si un `GET /measurements/{id}` está próximo.
