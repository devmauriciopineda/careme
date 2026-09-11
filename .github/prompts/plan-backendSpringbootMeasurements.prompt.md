# Plan: Backend Spring Boot + endpoint de mediciones

Crear un backend Spring Boot 3 / Java 21 con un único `GET /api/v1/measurements` que lee las mediciones desde un archivo JSON en el classpath y responde con el envelope del standard, y conectar el frontend Next.js a ese endpoint reemplazando el mock. La capa de persistencia se aísla detrás de una interfaz de repositorio para que reemplazar el archivo por PostgreSQL/JPA después sea un cambio de una sola clase.

**Steps**

**Fase 1 — Scaffold del backend**
1. Crear `backend/pom.xml` (parent `spring-boot-starter-parent` 3.x, Java 21, deps: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-test`, plugin JaCoCo) y el wrapper (`mvn wrapper:wrapper`) o `mvnw` descargado.
2. Crear `CaremeBackendApplication` en `com.careme.backend` y los `application*.yml`: `application.yml` (puerto 8080, `careme.measurements.data-location: classpath:data/measurements.json`), más `-dev` (orígenes CORS y logging), `-pre`, `-prd`.
3. Generar `backend/src/main/resources/data/measurements.json` con las 56 filas del mock actual, usando un script Node desechable que replica mulberry32 con seed `20260910` y fechas terminando el 2026-09-10 — así la UI queda idéntica a hoy. Borrar el script después.

**Fase 2 — Capas del backend**
4. `entity/Measurement.java` (`id: String`, `date: LocalDate`, `weightKg: BigDecimal`, `waistCm: BigDecimal`).
5. `dto/`: `ApiResponse<T>` (`success`, `data`, `messageCode`, `message` + factory `ok`), `MeasurementResponse` (record), `ErrorResponse` (`success`, `error{message, code, details}`).
6. `repository/MeasurementRepository` (interfaz) + `repository/MeasurementFileRepository` (impl): carga el JSON con `ObjectMapper` desde `ResourceLoader` en `@PostConstruct`, lo cachea en memoria ordenado ascendente. Sin JPA todavía.
7. `service/MeasurementService`: retorna `List<MeasurementResponse>`; usa constructor injection y logger SLF4J. Lanza `ResourceNotFoundException` si el recurso no existe o no tiene filas válidas.
8. `controller/MeasurementController`: `@GetMapping("/api/v1/measurements")` → `ApiResponse<List<MeasurementResponse>>`, thin, delega al service.
9. `exception/`: `ResourceNotFoundException` + `ApiExceptionHandler` (`@RestControllerAdvice`) que mapea a 404 y 500 con `ErrorResponse`. `config/CorsConfig` con orígenes leídos de properties (por defecto `http://localhost:3000`) — no es necesario porque el fetch es server-side, pero cumple el standard.

**Fase 3 — Tests backend** *(paralelizable con Fase 4)*
10. `MeasurementServiceTest` (Mockito sobre el repositorio), `MeasurementFileRepositoryTest` (JSON real del classpath + caso archivo ausente), `MeasurementControllerTest` (`@WebMvcTest` + MockMvc: envelope `success: true`, orden ascendente, 404). Verificar umbral JaCoCo.

**Fase 4 — Integración del frontend** *(paralelizable con Fase 3 una vez definido el contrato)*
11. Nuevo `src/services/apiConfig.ts` con `API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080"` (variable server-only, sin prefijo `NEXT_PUBLIC_`, porque nunca se consume desde el navegador).
12. `src/features/measurements/lib/schema.ts`: añadir `measurementResponseSchema` (envelope con `success: true` y `data: measurementsSchema`), manteniendo los schemas actuales.
13. `src/services/measurementService.ts`: reemplazar el import del mock por `fetch(`${API_BASE_URL}/api/v1/measurements`, { cache: "no-store" })`, validar `response.ok`, parsear el envelope, validar `data` con Zod y devolver `sortByDateAsc(...)`. Firma sin cambios → `MeasurementsDashboard.tsx` no se toca.
14. Eliminar `src/features/measurements/data/measurements.mock.ts` y la carpeta `data/`. Actualizar `.env.example` a `API_BASE_URL=http://localhost:8080`.
15. Nuevo `src/services/measurementService.test.ts` con `fetch` mockeado: caso OK (desenvuelve y ordena), envelope inválido, `!response.ok`. Inyectar el base URL en el test o mockear la constante.

**Fase 5 — Contenedores**
16. `backend/Dockerfile` multi-stage (`maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine`, usuario non-root) + `.dockerignore`.
17. `frontend/Dockerfile` multi-stage (`node:22-alpine` con `pnpm` vía corepack, build + `output: "standalone"`) + `.dockerignore`; agregar `output: "standalone"` en `next.config.ts`.
18. `docker-compose.yml` en la raíz: `backend` (8080), `frontend` (3000, `API_BASE_URL=http://backend:8080`, `depends_on: backend`).

**Fase 6 — Verificación**
19. Correr todo lo listado abajo.

**Relevant files**
- `c:\Mauricio\27. software-IA-projects\careme\frontend\src\services\measurementService.ts` — único consumidor del mock; se convierte en cliente HTTP manteniendo la firma `getMeasurements(): Promise<Measurement[]>`
- `c:\Mauricio\27. software-IA-projects\careme\frontend\src\features\measurements\lib\schema.ts` — añadir schema del envelope; ya tiene `measurementSchema`/`measurementsSchema` reutilizables
- `c:\Mauricio\27. software-IA-projects\careme\frontend\src\features\measurements\components\MeasurementsDashboard.tsx` — sin cambios (ya maneja array vacío con `STRINGS.dashboard.empty`)
- `c:\Mauricio\27. software-IA-projects\careme\frontend\src\features\measurements\data\measurements.mock.ts` — se elimina; su algoritmo seeded es la fuente del JSON semilla
- `c:\Mauricio\27. software-IA-projects\careme\frontend\next.config.ts` — `output: "standalone"`
- `c:\Mauricio\27. software-IA-projects\careme\frontend\.env.example` — renombrar la variable a `API_BASE_URL`
- `c:\Mauricio\27. software-IA-projects\careme\docs\standards\java-springboot-standards.md` — contrato de envelope, capas, `@RestControllerAdvice`, CORS y umbral de cobertura a respetar
- `c:\Mauricio\27. software-IA-projects\careme\frontend\node_modules\next\dist\docs\01-app\03-api-reference\04-functions\fetch.md` — referencia obligatoria antes de escribir el fetch (Next 16 tiene cambios de ruptura; por defecto `auto no cache`)

**Verification**
1. Backend unit/integration: `cd backend && ./mvnw test` (o `mvn test`) — service, repositorio y MockMvc en verde; reporte JaCoCo generado.
2. Manual API: `./mvnw spring-boot:run` y `curl http://localhost:8080/api/v1/measurements` — verificar envelope `success: true`, 56 items, fechas ascendentes en `yyyy-MM-dd`, valores con 1 decimal.
3. Caso 404/error: renombrar temporalmente `measurements.json` y confirmar `success: false` con `code` de error y HTTP 404/500 coherente.
4. Frontend estático: `cd frontend && pnpm typecheck && pnpm lint && pnpm test`.
5. Integración end-to-end: backend arriba + `pnpm dev`, abrir `http://localhost:3000` y confirmar 2 gráficos y tabla con los mismos datos que antes del cambio; luego apagar el backend y observar el comportamiento ante el error.
6. Contenedores: `docker compose up --build` y repetir la comprobación del punto 5 contra `http://localhost:3000`, verificando que el frontend habla con `http://backend:8080`.

**Decisions**
- Envelope `{success, data, messageCode, message}` según standard; el service del frontend desenvuelve `data` antes de validar con Zod, por lo que la UI no cambia.
- Fetch server-side en el Server Component: CORS no es necesario en runtime, pero se añade `CorsConfig` con orígenes explícitos por cumplimiento del standard y para futuras llamadas desde el navegador.
- Variable de entorno `API_BASE_URL` (server-only) en lugar de `NEXT_PUBLIC_API_BASE_URL` para no exponer la URL del backend al bundle del cliente.
- `cache: "no-store"` explícito para que el dashboard refleje siempre el archivo; el default de Next 16 en build podría servir datos congelados.
- Persistencia detrás de `MeasurementRepository` (interfaz) + implementación de archivo, para que la futura migración a JPA sea una sola clase nueva.
- Sin JPA, sin Flyway, sin Postgres en este corte: no hay base de datos.
- Fuera de alcance: POST/PUT/DELETE, autenticación, paginación, i18n, endpoint de métricas agregadas.
- El JSON semilla queda con fechas congeladas al 2026-09-10 (el mock generaba fechas relativas a "hoy").

**Further Considerations**
1. Comportamiento si el backend no responde: el RSC lanzará excepción y Next mostrará su pantalla de error. Opción A: dejar que falle (explícito, recomendado) / Opción B: `catch` en el service devolviendo `[]` para mostrar "Todavía no hay mediciones" / Opción C: agregar `src/app/error.tsx` con un mensaje en español. Recomiendo C.
2. Dockerfile del frontend: implica `output: "standalone"` en `next.config.ts` y pasar `API_BASE_URL` en runtime desde compose. Opción A: incluirlo (recomendado) / Opción B: solo backend en Docker y frontend en `pnpm dev` por ahora.
3. Nivel de cobertura backend: el standard pide 90% en ramas/líneas. Opción A: hacer fallar el build por debajo del umbral (recomendado) / Opción B: reportar JaCoCo sin umbral mientras el proyecto es un esqueleto.
