# Plan: Implementar UC-002 (registrar medición del día)

Añadir un formulario de registro en la pantalla de seguimiento que dé de alta la medición del día, con reemplazo si el día ya tenía registro. Backend: nuevo endpoint `POST` con validación y upsert por fecha (la tabla ya tiene `UNIQUE(date)` y `CHECK`, sin migración nueva). Frontend: Client Component con inputs controlados + Zod que invoca una **Server Action**; al revalidar la ruta, la tendencia y la tabla se actualizan solas. El formulario queda siempre visible, incluso sin mediciones (UC-002 no exige registros previos).

## Decisiones confirmadas con el usuario

- Mutación vía **Server Action + revalidatePath** (formulario = Client Component).
- Formulario con **inputs controlados + Zod** (sin dependencias nuevas).
- Pruebas: **backend (JUnit/MockMvc/Testcontainers) + frontend (Vitest/RTL)**. Sin e2e Playwright.

## Contexto clave

- Backend: DDD ligero, layered controller→service→repository. Dominio `Measurement` (record, id UUID no nulo, valores positivos), `MeasurementEntity` JPA (id generado, date unique), `MeasurementResponse` DTO, `ApiResponse<T>` envelope éxito, `ErrorResponse` envelope error, `ApiExceptionHandler` solo maneja Exception→500.
- Esquema `measurements` ya soporta el write (UNIQUE date, CHECK > 0, NUMERIC(5,2)). No requiere migración nueva.
- Frontend: dashboard es Server Component (`MeasurementsDashboard.tsx`) que hace fetch server-side vía `measurementService.getMeasurements()` (cache no-store) y valida con Zod (`schema.ts`). UI shadcn: solo button/card/chart/separator/table (faltan input/label). `API_BASE_URL` es server-only.
- CORS backend solo permite GET, pero el write va por Server Action (server-side), así que no cambia.

## Pasos

### Backend

1. `dto/MeasurementRequest.java` (nuevo): record con Bean Validation
   - `@NotNull @PastOrPresent LocalDate date`
   - `@NotNull @DecimalMin(value="0.0", inclusive=false) @DecimalMax("500.0") @Digits(integer=3, fraction=1) BigDecimal weightKg`
   - `@NotNull @DecimalMin(value="0.0", inclusive=false) @DecimalMax("400.0") @Digits(integer=3, fraction=1) BigDecimal waistCm`
2. `entity/MeasurementEntity.java`: añadir `updateValues(BigDecimal weightKg, BigDecimal waistCm)` (fila gestionada, sin tocar la fecha).
3. `repository/MeasurementJpaDao.java`: añadir `Optional<MeasurementEntity> findByDate(LocalDate date)`.
4. `repository/MeasurementRepository.java` (contrato): añadir
   - `Optional<Measurement> findByDate(LocalDate date)`
   - `Measurement create(LocalDate date, BigDecimal weightKg, BigDecimal waistCm)`
   - `Measurement update(UUID id, BigDecimal weightKg, BigDecimal waistCm)`
5. `repository/MeasurementJpaRepository.java`: implementar los tres (create = save entidad nueva; update = findById + updateValues + save; findByDate = mapear a dominio). `create`/`update` transaccionales de escritura.
6. `service/MeasurementService.java`: `register(MeasurementRequest)` — aplica la regla "una medición por día": si `findByDate` presente → `update(id, ...)`; si no → `create(...)`; devuelve `MeasurementResponse`. Loguear.
7. `controller/MeasurementController.java`: `@PostMapping` con `@Valid @RequestBody MeasurementRequest`, responde **201 Created** con `ApiResponse.ok(saved)`.
8. `exception/ApiExceptionHandler.java`: añadir
   - `MethodArgumentNotValidException` → 400, code `VALIDATION_ERROR`, `details` = mensajes por campo.
   - `HttpMessageNotReadableException` → 400, code `INVALID_REQUEST` (JSON/fecha/número mal formados).

Sin esto, los errores de validación caerían en el 500 genérico.

### Frontend

9. `src/components/ui/input.tsx` y `label.tsx` (nuevos): primitivos shadcn (vía `pnpm dlx shadcn@latest add input label`).
10. `features/measurements/types.ts`: añadir `MeasurementInput` `{ date: string; weightKg: number; waistCm: number }`.
11. `features/measurements/lib/schema.ts`: añadir
    - `measurementInputSchema` (números: positivo, ≤500/≤400, un decimal, fecha ISO no futura).
    - `measurementFormSchema` (strings del formulario → valida requerido/numérico/positivo/un decimal/máximo/no futura y transforma a `MeasurementInput`, mensajes en español).
    - `measurementCreatedResponseSchema` (envelope con `data` = una medición).
12. `features/measurements/lib/metrics.ts`: añadir `todayIsoDate()` (día local, sin shift UTC) y `isFutureIsoDate(iso, today)`.
13. `features/measurements/lib/strings.ts`: añadir `form` (título, etiquetas, botón, enviando, éxito, `saveFailed` con reintento) y mensajes de validación (requerido, no numérico, positivo, un decimal, máximo, fecha futura).
14. `src/services/measurementService.ts`: añadir `createMeasurement(input: MeasurementInput): Promise<Measurement>` — POST JSON a `/api/v1/measurements`, valida con `measurementCreatedResponseSchema`, devuelve la medición; lanza si el status no es ok.
15. `features/measurements/actions.ts` (nuevo, `"use server"`): `registerMeasurement(input)` — revalida con `measurementInputSchema`, llama `measurementService.createMeasurement`, `revalidatePath("/")`, devuelve resultado discriminado `{ ok: true } | { ok: false; errorCode }` serializable.
16. `features/measurements/components/MeasurementForm.tsx` (nuevo, `"use client"`): inputs controlados fecha (default hoy, `max`=hoy)/peso/cintura; valida con `measurementFormSchema`; errores por campo con `aria-invalid`/`aria-describedby` y anunciador `aria-live` (no solo color); deshabilita botón mientras pending; en éxito limpia campos numéricos y muestra confirmación; en fallo (E4) conserva lo digitado y ofrece reintentar.
17. `src/app/page.tsx`: renderizar `<MeasurementForm />` encima de `<MeasurementsDashboard />` (form visible también con 0 mediciones).

## Archivos relevantes

- `backend/.../controller/MeasurementController.java`, `service/MeasurementService.java`, `repository/MeasurementRepository.java` + `MeasurementJpaRepository.java`, `entity/MeasurementEntity.java`, `dto/MeasurementRequest.java` (nuevo), `exception/ApiExceptionHandler.java` — patrón existente `MeasurementResponse`/`ApiResponse`/`ErrorResponse` reutilizable.
- `frontend/src/app/page.tsx`, `features/measurements/components/MeasurementsDashboard.tsx` (se mantiene), `lib/schema.ts`, `lib/metrics.ts`, `lib/strings.ts`, `services/measurementService.ts`, `components/MeasurementForm.tsx` (nuevo), `features/measurements/actions.ts` (nuevo).
- `backend/src/main/resources/db/migration/V1__create_measurements_table.sql` — sin cambios.

## Pruebas

Backend:

- `MeasurementControllerTest`: POST 201 envelope/eco; POST inválido → 400 VALIDATION_ERROR; fecha futura → 400; JSON mal formado → 400.
- `MeasurementServiceTest`: `register` crea si no existe la fecha; reemplaza si existe (sin duplicar); mapea campos.
- `MeasurementJpaRepositoryTest`: `findByDate` presente/ausente; `create` inserta; `update` modifica la misma fila (count sin cambios).

Frontend:

- `schema.test.ts`: casos de `measurementFormSchema`/`measurementInputSchema` (válido, faltante, no numérico, no positivo, >500/400, dos decimales, fecha futura, coma decimal).
- `measurementService.test.ts`: `createMeasurement` (éxito, error de status, payload malformado, método/body/cache).
- `MeasurementForm.test.tsx`: etiquetas accesibles; submit inválido no llama la acción y muestra errores; submit válido llama la acción; fallo conserva valores y muestra reintento; botón deshabilitado mientras pending.

## Verificación

1. Backend: `.\mvnw.cmd verify` (JUnit + JaCoCo ≥90% branch/line; Testcontainers requiere Docker).
2. Frontend: `pnpm test`, `pnpm typecheck`, `pnpm lint`.
3. Manual con `docker-compose up`: registrar medición → tendencia y tabla se actualizan sin recargar; re-registrar el mismo día → reemplaza (no duplica); valores inválidos → mensajes; backend caído → E4 conserva datos y permite reintentar.

## Documentación

- Actualizar `backend/README.md` (contrato POST), `frontend/README.md` (formulario) y `README.md` general (ya no es read-only).
- Marcar UC-002 `Estado: Implementado`.

## Fuera de alcance

- Editar/borrar medición como acción independiente; multiusuario/auth; notificaciones/objetivos; e2e Playwright; nueva ruta dedicada.

## Consideraciones abiertas

1. Zona horaria de "hoy": `@PastOrPresent` usa la zona del servidor; el default del formulario usa la zona del navegador. Cerca de medianoche podrían discrepar. Opción A: dejarlo como MVP (recomendado) / Opción B: fijar zona explícita configurable en backend.
2. Carrera de doble envío real: el `UNIQUE(date)` es la última barrera; el botón deshabilitado cubre el caso UI. Opción A: dejarlo (recomendado) / Opción B: capturar `DataIntegrityViolationException` y reintentar como update.
