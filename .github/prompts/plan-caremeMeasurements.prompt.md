# Plan: MVP Careme — tracking de peso y cintura (Next, solo frontend)

Verifiqué el workspace: está **vacío** salvo `docs/standards/next-standards.md`; `frontend/` y `backend/` no tienen archivos. Proyecto greenfield, así que no hay plantillas internas que reutilizar — pero el estándar sí fija el stack.

**TL;DR:** Scaffold con Next 16 + React 19 + TS strict + Tailwind + shadcn/ui (pnpm), un dashboard con *Server Components* por defecto que lee 8 semanas de datos mock a través de una capa `service` ya **async** (para que conectar la API real después no sea un reproceso), dos gráficas de línea Recharts vía el `chart` de shadcn renderizadas por **un solo** componente reutilizable parametrizado por métrica, y una tabla responsive. Código en inglés, textos de UI en español centralizados, Vitest + RTL con tests de la lógica pura.

## Decisiones clave de arquitectura
- **Server Components por defecto.** `app/page.tsx` → `MeasurementsDashboard` (async SC) hace `await measurementService.getMeasurements()` y **pre-calcula las series** en el servidor. Los componentes de gráfica llevan `'use client'` (Recharts necesita browser) pero reciben solo primitivos serializables (`{date, label, value}[]`), nunca objetos `Date`.
- **Un componente de gráfica, no dos.** `MetricTrendChart` se parametriza por `MetricKey` y se instancia dos veces (peso y cintura). Evita duplicar ~90 líneas casi idénticas.
- **Config de métricas en un mapa `METRICS`** (label es, unidad, decimales, token de color `--chart-1/--chart-2`, padding de dominio). Agregar una métrica futura = agregar una entrada al mapa.
- **Lógica de transformación 100% pura** en `lib/metrics.ts` → corre en el servidor y es testeable sin DOM.
- **Zod en el borde.** El schema valida el mock hoy y la respuesta de API mañana sin tocar la UI.
- **Strings de UI en un objeto `lib/strings.ts`** — elegiste "código inglés, UI español" (sin i18n runtime), y esto evita literales dispersos sin introducir infraestructura de traducción.

## Pasos

**Fase 0 — Scaffold** *(bloquea todo lo demás)*
1. `create-next-app` en `frontend/`: TypeScript, Tailwind, ESLint, App Router, `src/`, alias `@/*`.
2. `shadcn init` + agregar solo `chart`, `card`, `table`, `separator` (`chart` trae Recharts y los tokens de color).
3. Confirmar `strict: true` y alias `@/* → src/*`; scripts `typecheck` y `lint`.
4. Vitest + RTL + jsdom + `@vitejs/plugin-react`, con `vitest.config.ts` (alias, globals, jsdom) y `src/test/setup.ts`.

**Fase 1 — Dominio y datos** *(bloquea la UI)*
5. Tipos: `Measurement` (id, `date` ISO, `weightKg`, `waistCm`), `MetricKey`, `MetricConfig`, `SeriesPoint`.
6. Mock: 56 registros (8 semanas) con PRNG de semilla fija, **fechas relativas a hoy**, tendencia de peso descendente realista y cintura correlacionada.
7. Schema Zod + `measurementService.getMeasurements()` async: valida, ordena ascendente y es el único punto a tocar cuando exista backend.
8. Funciones puras: `sortByDateAsc/Desc`, `buildSeries`, `computeDomain`, formateo `es-ES` (1 decimal), `computeDateRange`.
9. `strings.ts` con los textos en español.

**Fase 2 — UI** *(depende de Fase 1)*
10. `MetricTrendChart` (client): `ChartContainer` + `LineChart` (`dot={false}`, `strokeWidth={2}`, `accessibilityLayer`, grid horizontal suave, eje Y con dominio calculado **que no arranca en 0**, tooltip con fecha + valor + unidad), `aria-label` + resumen `sr-only`.
11. `MeasurementsTable` (server): columnas Fecha / Peso (kg) / Cintura (cm), orden descendente, `<caption className="sr-only">`, wrapper `overflow-x-auto` con header sticky.
12. `MeasurementsDashboard` (async server): encabezado con rango de fechas y nº de registros, grid 1 col móvil → 2 cols `lg` con las gráficas, tabla a ancho completo.
13. `app/layout.tsx`: `<html lang="es">`, fuente con `next/font`, container `max-w-6xl`. `app/page.tsx` delgada con `metadata` en español.

**Fase 3 — Tests** *(pasos 14-15 tras 6-8; paso 16 depende de 11)*
14. Tests de `lib/metrics.ts`: orden, buildSeries, dominio con padding, formateo es-ES.
15. Test del schema Zod: válido + inválidos (fecha malformada, campo faltante, valor no numérico).
16. Test RTL de la tabla: nº de filas, fila más reciente primero, valores formateados, caption accesible.

**Fase 4 — Docs**
17. `frontend/README.md` (scripts, estructura, cómo cambiar mock → API, qué del estándar queda diferido) + `.env.example` + `e2e/playwright/.gitkeep`.

## Archivos relevantes
- `docs/standards/next-standards.md` — fuente de todas las convenciones aplicadas (estructura, naming en inglés, SC por defecto, shadcn+Tailwind, alias, scripts).
- `frontend/src/features/measurements/types.ts` — contratos `Measurement`, `MetricKey`, `MetricConfig`, `SeriesPoint`.
- `frontend/src/features/measurements/lib/metrics.ts` — toda la lógica pura (transformación, dominio, formato).
- `frontend/src/features/measurements/data/measurements.mock.ts` — dataset de 8 semanas.
- `frontend/src/services/measurementService.ts` — frontera de datos, listo para TanStack Query.
- `frontend/src/features/measurements/components/MetricTrendChart.tsx` — gráfica reutilizable.
- `frontend/src/features/measurements/components/MeasurementsDashboard.tsx` — composición de la página.
- `frontend/src/features/measurements/lib/strings.ts` — textos UI en español.
- `frontend/vitest.config.ts` + `frontend/src/test/setup.ts` — runner de tests.

## Verificación
1. `pnpm typecheck` → 0 errores.
2. `pnpm lint` → 0 warnings.
3. `pnpm test` → todos pasan.
4. `pnpm build` → exitoso (esto valida la frontera Server/Client: falla si un Server Component importa Recharts, el error más probable del diseño).
5. `pnpm dev` manual: 375px / 768px / 1280px (1 col → 2 cols, tabla con scroll horizontal sin romper layout); charts redimensionan; tooltip correcto; eje Y no arranca en 0; 56 puntos y eje X sin solapamiento; `lang="es"`; cero warnings de hidratación; navegación por teclado en la gráfica.
6. Revisión cruzada contra el estándar: estructura de carpetas, naming en inglés, SC por defecto, alias `@/*`.

## Fuera de alcance
Backend, API, DB, auth, Storybook, Playwright, TanStack Query, Zustand, dark mode, i18n runtime, export CSV, filtros/orden de tabla, línea de objetivo, stats resumen, CI/Docker.

## Further considerations
1. **Tipo de gráfica:** Line (más claro para tendencia) vs Area con gradiente (más vistoso). Recomiendo Line para el MVP.
2. **Tabla en móvil:** scroll horizontal (recomendado, menos código, mantiene semántica de tabla) vs lista de tarjetas por fila.
3. **Workspace pnpm en la raíz ahora** vs carpeta `frontend/` simple (recomendado) hasta que el backend necesite compartir tipos.
