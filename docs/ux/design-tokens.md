# Design tokens de Careme

> Versión 0.1 · 25 de septiembre de 2026
>
> Este documento describe los tokens visuales implementados en `frontend/src/app/globals.css`. Los tokens de marca expresan la identidad de Careme; los tokens semánticos son la API visual que consumen los componentes de Tailwind y shadcn/ui.

## 1. Principios

- Usar tokens semánticos en componentes: `bg-primary`, `text-foreground`, `border-border` y equivalentes.
- Reservar los tokens `careme-*` para identidad, gráficos y casos donde el rol de marca sea explícito.
- No introducir valores HEX u OKLCH directamente en componentes si existe un token adecuado.
- El color no debe ser el único canal para comunicar estados.
- Mantener contraste WCAG AA en texto y controles; validar cada combinación cuando se use en una superficie nueva.
- El modo oscuro forma parte del contrato de tokens aunque todavía no sea una funcionalidad expuesta de la aplicación.

## 2. Tokens de marca

| Token CSS | Valor claro | Rol |
|---|---:|---|
| `--careme-primary` | `#2F8F91` | Azul verdoso luminoso. Marca, navegación y acciones principales. |
| `--careme-primary-deep` | `#1F5F63` | Azul verdoso profundo. Contraste, texto de acción y estados activos. |
| `--careme-leaf` | `#79A878` | Verde hoja. Continuidad, evolución y confirmaciones suaves. |
| `--careme-peach` | `#F3B59F` | Melocotón pálido. Énfasis humano y destacados puntuales. |
| `--careme-ink` | `#203234` | Tinta suave. Texto principal. |
| `--careme-paper` | `#F7F8F4` | Marfil frío. Fondo principal. |
| `--careme-attention` | `#B77935` | Ocre moderado. Información incompleta o revisión necesaria. |
| `--careme-critical` | `#A34B4B` | Rojo sobrio. Errores reales y acciones destructivas. |

Los nombres describen el rol de marca, no un componente concreto. Un componente debe preferir `--primary`, `--muted` o `--destructive` cuando el uso sea semántico.

## 3. Tokens semánticos

### Superficies y texto

| Token | Valor claro | Uso |
|---|---:|---|
| `--background` | `var(--careme-paper)` | Fondo general. |
| `--foreground` | `var(--careme-ink)` | Texto principal. |
| `--card` | `#FFFFFF` | Superficies de contenido agrupado. |
| `--popover` | `#FFFFFF` | Menús, diálogos y contenido flotante. |
| `--muted` | `#E9EFEC` | Superficies secundarias y estados inactivos. |
| `--muted-foreground` | `#536466` | Texto secundario. |
| `--secondary` | `#E5F0ED` | Acción o superficie secundaria. |
| `--secondary-foreground` | `var(--careme-primary-deep)` | Texto sobre secondary. |
| `--accent` | `#FBE5DC` | Énfasis visual suave. |
| `--accent-foreground` | `#6F3F36` | Texto sobre accent. |

### Acciones, foco y bordes

| Token | Valor claro | Uso |
|---|---:|---|
| `--primary` | `var(--careme-primary)` | Acción principal y elementos de marca. |
| `--primary-foreground` | `#FFFFFF` | Texto e iconos sobre primary. |
| `--border` | `#D4E0DC` | Separadores y bordes de baja intensidad. |
| `--input` | `#C4D4D0` | Bordes de campos de formulario. |
| `--ring` | `var(--careme-primary)` | Indicador de foco visible. |
| `--destructive` | `var(--careme-critical)` | Error crítico y acción destructiva. |

### Gráficos

| Token | Valor claro | Uso |
|---|---:|---|
| `--chart-1` | `var(--careme-primary-deep)` | Serie principal. |
| `--chart-2` | `var(--careme-leaf)` | Serie secundaria. |
| `--chart-3` | `var(--careme-attention)` | Serie de atención o referencia. |
| `--chart-4` | `#7C6B99` | Serie adicional. No representa un estado clínico. |
| `--chart-5` | `var(--careme-ink)` | Serie de alto contraste. |

Las series deben tener también leyenda, etiqueta o contexto textual. No usar una serie como diagnóstico, recomendación o señal de gravedad sin respaldo explícito del dominio.

## 4. Modo oscuro

El selector `.dark` redefine los tokens semánticos con superficies azul verdosas oscuras y primarios más luminosos. Los tokens de marca se mantienen como referencia conceptual, pero no todos sus valores claros se reutilizan directamente sobre fondo oscuro.

Las combinaciones oscuras deben comprobarse con contenido real en español, gráficos, campos de formulario y estados de error. El modo oscuro no debe interpretarse como disponible para el usuario hasta que exista un control o una decisión de producto que lo habilite.

## 5. Tipografía

La implementación actual conserva la fuente Geist cargada por `frontend/src/app/layout.tsx` mediante `--font-geist-sans` y `--font-geist-mono`.

| Token | Valor actual | Rol |
|---|---|---|
| `--font-sans` | `var(--font-geist-sans)` | Texto general y componentes. |
| `--font-heading` | `var(--font-sans)` | Títulos; misma familia por ahora. |
| `--font-mono` | `var(--font-geist-mono)` | Valores técnicos o código. |

La guía de marca propone explorar una sans geométrica suave, pero cambiar la familia tipográfica requiere validar legibilidad, carga y renderizado. No se introduce una nueva fuente en esta iteración.

## 6. Forma, espaciado y movimiento

### Forma

- `--radius`: `0.5rem` como radio base.
- `--radius-sm`, `--radius-md`, `--radius-lg` y escalas superiores se derivan del radio base en `@theme`.
- Preferir superficies contenidas y radios moderados; no convertir cada sección en una tarjeta flotante.

### Espaciado

Se utiliza la escala de espaciado de Tailwind v4. Los componentes deben preferir clases de la escala (`gap-2`, `p-4`, `mt-6`) en lugar de valores arbitrarios. La escala debe mantenerse consistente antes de añadir nuevos valores.

### Motion

| Token | Valor | Uso |
|---|---:|---|
| `--motion-fast` | `150ms` | Feedback inmediato y microinteracciones. |
| `--motion-standard` | `220ms` | Cambios de estado y transición de contenido. |
| `--motion-slow` | `320ms` | Entrada de secciones o superficies completas. |
| `--motion-ease` | `cubic-bezier(0.22, 1, 0.36, 1)` | Easing orgánico y contenido. |

Toda animación debe respetar `prefers-reduced-motion`. El movimiento no puede ser necesario para entender una fecha, un estado, una fuente o un resultado.

## 7. Correspondencia con la guía de marca

| Decisión de marca | Implementación |
|---|---|
| Tecnología humana y discreta | Paleta azul verdosa, superficies suaves y contraste contenido. |
| Calidez humana | `--careme-leaf`, `--careme-peach` y `--accent` en usos puntuales. |
| Claridad | Roles semánticos separados para fondo, texto, acción, foco y estados. |
| Continuidad | Series de gráficos y recursos visuales pueden usar `--chart-1` y `--chart-2`. |
| No alarmismo | El rojo queda limitado a `--destructive` y no es color de marca. |
| Sutil y duradera | Una escala reducida de radios, motion y colores con roles explícitos. |

Consulta la [guía de identidad de Careme](careme_brand_guide.md) para la estrategia visual completa y los límites de uso.

## 8. Validación pendiente

1. Verificar contraste WCAG AA de cada combinación usada en componentes.
2. Revisar los gráficos con datos reales de peso y circunferencia.
3. Probar los estados de chat, eventos clínicos, mediciones, carga y error.
4. Decidir y validar la familia tipográfica definitiva.
5. Activar el modo oscuro solo después de probarlo en una vista completa.
6. Extraer tokens a una fuente estructurada compartida si aparecen otros consumidores además del frontend.
