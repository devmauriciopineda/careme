# Plan: Importar mediciones desde CSV (UC-003)

Reenviar el archivo desde una server action de Next al backend en `multipart`, y que el backend lo parsee, valide y guarde **en una sola transacción todo-o-nada**. La previsualización es el mismo camino en modo *dry-run* (sin escribir), así que la clasificación "nueva vs. reemplaza" la decide el backend, que es quien conoce las fechas existentes. Síncrono: sin tabla de trabajos ni sondeo.

## Context

- Spec: `docs/use-cases/UC-003.md` (estado Propuesto).
- Backend: Spring Boot 3.5.3 / Java 21 / JPA + Flyway / PostgreSQL. No hay soporte multipart ni async todavía.
- Frontend: Next 16 (App Router, Server Components + Server Actions), `fetch` server-side; `API_BASE_URL` sin `NEXT_PUBLIC_`, así que el navegador no habla con el backend.
- Gate de cobertura backend: JaCoCo 90% branch y line (BUNDLE).

## Steps

### Fase 1 — Backend: parseo y validación

1. Añadir dependencia Apache Commons CSV a `backend/pom.xml` (fijar versión si el BOM de Spring Boot 3.5.3 no la gestiona).
2. Configurar límites en `application.yml`: `spring.servlet.multipart.max-file-size: 10MB`, `max-request-size: 11MB`. Opcional: `hibernate.jdbc.batch_size` para el alta masiva.
3. Crear `MeasurementCsvParser`: `InputStream` → filas, con cabeceras exactas `date`, `weight_kg`, `abdominal_circumference_cm`. Reutiliza **Bean Validation validando un `MeasurementRequest` por fila**, de modo que las reglas de UC-002 (positivo, ≤1 decimal, ≤500/≤400, no futura) no se dupliquen.
4. Crear `MeasurementImportException` (código + detalles con nº de fila) y engancharla en `ApiExceptionHandler`, junto con `MaxUploadSizeExceededException`.

### Fase 2 — Backend: servicio y persistencia

5. Añadir `MeasurementRepository.findExistingDates(...)` y `upsertAll(List<MeasurementDraft>)`, con `MeasurementDraft` como carrier interno. Una sola query de lectura y un solo `flush` (evita 10.000 flushes de `saveAndFlush`). Implementar en `MeasurementJpaRepository` y extender `MeasurementJpaDao`. *Depende de 3.*
6. Crear `MeasurementImportService`: `preview(InputStream)` (solo lectura) y `importMeasurements(InputStream)` (un único `@Transactional`; todo-o-nada). *Depende de 5.*
7. Añadir los dos endpoints multipart a `MeasurementController`: `/import/preview` e `/import`, con los DTO `ImportPreviewRow`, `ImportPreviewResponse`, `ImportResultResponse`. *Depende de 6.*

### Fase 3 — Frontend

8. Subir `experimental.serverActions.bodySizeLimit` a ~`11mb` en `next.config.ts`. *Sin esto, cualquier archivo >1 MB falla antes de llegar al backend.*
9. Extender `types.ts`, `lib/schema.ts` (validación zod de las respuestas) y `lib/strings.ts` (copy español del bloque `import`).
10. Extender `measurementService.ts` con `previewMeasurementImport(file)` e `importMeasurements(file)` (FormData multipart). *Depende de 7.*
11. Añadir las server actions `previewMeasurementImport` y `confirmMeasurementImport` en `actions.ts`; la segunda hace `revalidatePath("/")` al éxito.
12. Crear `MeasurementImport.tsx` (cliente): elegir archivo → **Previsualizar** (resumen + filas nueva/reemplaza) → **Confirmar carga** → resultado anunciado en una región `role="status"`, sin modal. Chequeo previo de tamaño y accesibilidad por teclado. *Depende de 9-11.*
13. Montar `<MeasurementImport />` en `app/page.tsx` junto a `MeasurementForm`.

### Fase 4 — Pruebas (en paralelo con 1-13 por área)

14. Backend: `MeasurementCsvParserTest` — válido, falta columna, columna extra, fecha inválida, negativos, >1 decimal, sobre límite, fecha futura, fecha vacía con valores, ambos valores vacíos (ignorada), duplicados (última gana), >10.000 filas, campos entrecomillados.
15. Backend: `MeasurementImportServiceTest` — clasificación nueva/reemplaza, no escribe nada si hay una fila inválida, contadores.
16. Backend: `MeasurementImportControllerTest` (`@WebMvcTest`) — preview/import multipart, 400 con envelope, archivo enorme, falta el fichero.
17. Backend: `MeasurementJpaRepositoryTest` (Testcontainers) — `upsertAll` crea+reemplaza y hace rollback.
18. Frontend: `measurementService.test.ts` (multipart + parseo) y `MeasurementImport.test.tsx` (previsualizar→confirmar→resultado, error del backend, bloqueo >10 MB).

## Relevant files

- `backend/src/main/java/com/careme/backend/controller/MeasurementController.java` — añadir los 2 endpoints multipart; sigue delegando todo al servicio.
- `backend/src/main/java/com/careme/backend/service/MeasurementService.java` — referencia para el nuevo `MeasurementImportService`; opcional: que `register` delegue en `upsertAll` para no repetir la regla "reemplaza si existe".
- `backend/src/main/java/com/careme/backend/dto/MeasurementRequest.java` — reutilizar sus anotaciones como única fuente de las reglas de valor.
- `backend/src/main/java/com/careme/backend/repository/MeasurementRepository.java` + `MeasurementJpaRepository.java` + `MeasurementJpaDao.java` — extender el contrato y su implementación.
- `backend/src/main/java/com/careme/backend/exception/ApiExceptionHandler.java` — nuevos handlers, siguiendo el patrón de `ErrorResponse`.
- `backend/src/main/resources/application.yml` — config multipart.
- `frontend/next.config.ts` — `bodySizeLimit`.
- `frontend/src/services/measurementService.ts` + `src/features/measurements/lib/schema.ts` — patrón de validación con zod a replicar.
- `frontend/src/features/measurements/actions.ts` — patrón de server action con `errorCode` a replicar.
- `frontend/src/features/measurements/components/MeasurementForm.tsx` — patrón de estado (`isPending`, `role="alert"`, `aria-describedby`) a reutilizar.
- `frontend/src/app/page.tsx` — punto de montaje.

## Verification

1. `cd backend && ./mvnw -q verify` — tests + gate JaCoCo (90% branch/line, BUNDLE).
2. `cd frontend && pnpm typecheck && pnpm lint && pnpm test`.
3. Manual: `docker-compose up` + `pnpm dev`; subir CSV **válido**, con **duplicados**, con **huecos**, con **estructura inválida** (debe abortar sin escribir nada), de **>10 MB** y de **>10.000 filas**. Comprobar que no aparece ningún modal, que el resultado se anuncia y que `SELECT ... FROM measurements` confirma que no hubo cargas parciales.
4. Regresión UC-001/UC-002: ver y registrar medición siguen funcionando.

## Decisions

- Síncrono, multipart, parseo y previsualización en el backend, Apache Commons CSV.
- Carga **todo-o-nada**: cualquier incumplimiento aborta sin escribir (E1/E2/E3).
- Fila con fecha y ambos valores vacíos → ignorada sin error (A1); fecha vacía con valores o un valor vacío → E2.
- Duplicados de fecha en el archivo → gana la última (A2).
- Cabeceras exactas; columna faltante o extra → E1.
- Fuera de alcance: exportación, plantilla descargable, otros formatos, varios archivos a la vez, sincronización externa, y **E2E Playwright** (hoy solo existe `e2e/playwright/.gitkeep`).

## API contract

- `POST /api/v1/measurements/import/preview` (multipart `file`) → `ApiResponse<ImportPreviewResponse>`
  - `ImportPreviewRow(date, weightKg, waistCm, replacesExisting)`
  - `ImportPreviewResponse(rows, totalRows, newCount, replacedCount, ignoredCount)`
- `POST /api/v1/measurements/import` (multipart `file`) → `ApiResponse<ImportResultResponse>`
  - `ImportResultResponse(createdCount, replacedCount, ignoredCount, totalRows)`

## Further Considerations

1. **UC-003 dice "procesar en segundo plano" / "seguir usando la app sin esperar"**, pero la decisión es síncrona. Recomiendo **A)** ajustar la redacción de las secciones 5.7, 7 y 9 de UC-003 para que digan "la carga muestra su progreso y el resultado al terminar, sin bloquear la pantalla"; **B)** dejarlo literal y cambiar a asíncrono con sondeo (mucho más código).
2. **Columnas extra**: UC-003 dice "exactamente las columnas", así que el plan las rechaza. **A)** mantener el rechazo estricto; **B)** tolerar columnas extra e ignorarlas.
3. **`bodySizeLimit` en Next 16**: confirmar la clave exacta (`experimental.serverActions.bodySizeLimit`) al implementar; si faltara, el límite de 1 MB rompería el caso de 10 MB.
4. **Versión de commons-csv**: fijarla explícitamente si el BOM de Spring Boot 3.5.3 no la gestiona.
