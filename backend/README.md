# Careme — Backend

## Project description

Spring Boot REST service for the Careme body-tracking application. It exposes the
measurements that the frontend renders: date, weight and abdominal circumference.

It exists as its own service so the UI does not own the data. Measurements
currently live in a JSON file shipped with the application, and the persistence
layer is isolated behind an interface so a database can replace it without
touching the HTTP contract.

## General functionality

- One read endpoint, `GET /api/v1/measurements`, returning every measurement
  ordered from the oldest to the most recent date.
- One response envelope for every outcome, success or failure, plus a global
  exception handler: `404` when the data file is missing, `500` otherwise.
- The data file is read once and cached in memory. It is read-only, so the cache
  cannot go stale.
- CORS is configured explicitly and scoped to known origins.

## Architecture overview

```text
Controller   MeasurementController      maps HTTP ↔ DTOs, no business logic
    │
Service      MeasurementService         sorts by date, maps entity → DTO
    │
Repository   MeasurementRepository      data access contract
             MeasurementFileRepository  reads and caches the JSON file
    │
Domain       Measurement (record)       identity + invariants
```

- **Request flow**: controller → service → repository → `ObjectMapper` parses the
  classpath resource; the service sorts ascending by date and maps each
  `Measurement` to a `MeasurementResponse`, which the controller wraps in
  `ApiResponse`.
- **Failure flow**: a missing resource raises `ResourceNotFoundException` →
  HTTP 404 through `ApiExceptionHandler`; a read failure raises
  `IllegalStateException` → HTTP 500.
- **Invariants** live in the `Measurement` record's compact constructor, so an
  invalid measurement cannot be constructed.
- **Loading is lazy on purpose**: the file is read on the first request, so a
  missing or malformed file surfaces as a request error instead of preventing the
  application from booting.
- **Entity and DTO are separate types** even though they look alike today, so
  adding persistence annotations later does not change the JSON contract.

## Technology stack

- Java 21 (LTS) and Spring Boot 3.5 (Spring Web MVC, Bean Validation)
- Jackson for JSON binding, SLF4J + Logback for logging
- Maven with the wrapper committed (Maven 3.9.9, `only-script` distribution) and
  `maven-enforcer-plugin` requiring JDK 21
- JUnit 5, Mockito, AssertJ, MockMvc and Spring Boot Test
- JaCoCo with a gate of 90% branches and 90% lines, bound to `verify`
- Docker multi-stage image (`maven:3.9-eclipse-temurin-21` →
  `eclipse-temurin:21-jre-alpine`, non-root user)

## Use of APIs or external services

- It consumes **no external or third-party APIs**.
- It **exposes** one HTTP API, documented below.
- There is **no database**, no ORM, no migration tool and no connection pool yet.
- There is **no authentication or authorization**.
- No message broker, cache server or cloud service is used.

## API

### `GET /api/v1/measurements`

Returns every measurement, oldest first.

**Success — `200 OK`**

```json
{
  "success": true,
  "data": [
    {
      "id": "measurement-2026-07-18",
      "date": "2026-07-18",
      "weightKg": 84.5,
      "waistCm": 98.4
    }
  ],
  "messageCode": "SUCCESS",
  "message": "Operation completed successfully"
}
```

An empty dataset is a successful response with `"data": []`.

**Errors** follow the same envelope with `success: false`:

```json
{
  "success": false,
  "error": {
    "message": "Measurements data file not found: classpath:data/measurements.json",
    "code": "NOT_FOUND",
    "details": []
  }
}
```

`404 NOT_FOUND` when the configured data file does not exist, and
`500 INTERNAL_ERROR` for unexpected failures.

Notes on the payload: `date` is an ISO `yyyy-MM-dd` calendar date and both metrics
are decimals with one decimal place.

## General repository structure

```text
backend/
├── src/main/java/com/careme/backend/
│   ├── controller/ · service/ · repository/ · entity/ · dto/
│   ├── config/                         # CORS
│   ├── exception/                      # custom exception + global handler
│   └── CaremeBackendApplication.java   # entry point
├── src/main/resources/
│   ├── application.yml                 # port, data location, CORS origins
│   ├── application-{dev,pre,prd}.yml   # per-environment overrides
│   └── data/measurements.json          # the dataset
├── src/test/java/com/careme/backend/   # mirrors the package structure
├── .mvn/wrapper/                       # Maven wrapper configuration
├── Dockerfile
├── mvnw / mvnw.cmd
└── pom.xml
```

## Installation guide

**Requirements**

- JDK 21. The build is pinned: `maven-enforcer-plugin` fails with an explicit
  message when `JAVA_HOME` points at another major version.
- No Maven installation needed; the wrapper downloads Maven 3.9.9 on first use.

```bash
cd backend
./mvnw dependency:go-offline      # Windows: .\mvnw.cmd dependency:go-offline
```

**Configuration** (`src/main/resources/application.yml`)

| Property                            | Default                            |
| ----------------------------------- | ---------------------------------- |
| `server.port`                       | `8080`                             |
| `careme.measurements.data-location` | `classpath:data/measurements.json` |
| `careme.cors.allowed-origins`       | `http://localhost:3000`            |

Profiles `dev`, `pre` and `prd` override logging and CORS origins; in `pre` and
`prd` the origins come from `CAREME_CORS_ALLOWED_ORIGINS`. Any property can be
overridden per run, e.g. `--careme.measurements.data-location=...`.

## Run guide

```bash
# development, from the backend folder
./mvnw spring-boot:run                # Windows: .\mvnw.cmd spring-boot:run

# packaged jar
./mvnw package && java -jar target/backend-0.0.1-SNAPSHOT.jar

# container, from the repository root
podman compose up --build backend
```

Verify it manually with `curl http://localhost:8080/api/v1/measurements`.

## Testing

```bash
./mvnw test        # unit and web-layer tests
./mvnw verify      # tests + JaCoCo report + coverage gate
```

The suite is 18 tests across 5 classes, organized by layer:

| Class                           | Covers                                                    |
| ------------------------------- | --------------------------------------------------------- |
| `CaremeBackendApplicationTests` | Full stack: real file, envelope, ordering, CORS preflight  |
| `MeasurementControllerTest`     | `@WebMvcTest`: envelope shape, success and error mappings  |
| `MeasurementServiceTest`        | Ordering, mapping, empty dataset, error pass-through       |
| `MeasurementFileRepositoryTest` | Real file, missing file, malformed JSON, cache reuse       |
| `MeasurementTest`               | Domain invariants                                          |

The coverage gate runs at `verify`, not `test`, so a quick `mvn test` is not
blocked by coverage while a full build is.

## Additional notes

- **Read-only by design.** There are no write endpoints. Adding them means adding
  a method to `MeasurementRepository` and, since the current implementation caches
  the file, a way to invalidate that cache.
- **Replacing the file with a database.** Implement `MeasurementRepository` with
  Spring Data JPA, or add a second implementation selected by profile. The
  controller, the service and the JSON contract stay as they are.
- **The dataset is a fixture**, not production data: 56 daily records generated
  from a seeded algorithm, frozen between 2026-07-18 and 2026-09-11.
- **CORS is not strictly required today** because the frontend fetches on the
  server. It is explicit so a future client-side call works without opening the
  API to every origin.
- Follows `docs/standards/java-springboot-standards.md`. JPA, Flyway and
  PostgreSQL are described there but intentionally not adopted: there is no
  database yet.
