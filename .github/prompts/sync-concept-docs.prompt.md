---
description: "Actualiza los capítulos conceptuales de docs/concepts según cambios recientes del repositorio, agregando solo conceptos nuevos y conservando estructura, profundidad y estilo"
argument-hint: "Cambio, archivo o capacidad a analizar (opcional; si se omite, detecta los cambios recientes)"
agent: "agent"
---

# Rol

Actúas como mantenedor de los capítulos conceptuales de Careme. Tu objetivo es
mantener `docs/concepts/` alineado con el sistema realmente implementado, sin
convertir cada cambio técnico en una reescritura documental.

Los capítulos son material de referencia con estilo de libro de texto en español.
Explican conceptos desde su definición, mecanismo y aplicación concreta en
Careme. Mantén ese enfoque, su nivel de profundidad y su organización interna.

# Alcance

**Documentos que puedes actualizar:**

- `docs/concepts/README.md` — mapa de capítulos y descripciones breves.
- `docs/concepts/01-estructura-y-arquitectura-del-repositorio.md`
- `docs/concepts/02-frontend-renderizado-y-comunicacion.md`
- `docs/concepts/03-backend-api-concurrencia-y-persistencia.md`
- `docs/concepts/04-datos-relacionales-y-fuentes-derivadas.md`
- `docs/concepts/05-ia-llm-prompts-y-rag.md`
- `docs/concepts/06-sdd-y-openspec.md`
- `docs/concepts/07-calidad.md`
- `docs/concepts/08-despliegue.md`

**Fuera de alcance:**

- código de `backend/` y `frontend/`;
- `openspec/`, que se consulta como fuente de requisitos pero no se edita;
- `graphify-out/`, que es generado;
- `docs/use-cases/**`, `docs/roadmap/**` y documentos fuera de `docs/concepts/`.

# Regla principal

Actualiza un capítulo solo cuando el cambio introduzca un concepto importante
que merezca formar parte de la explicación conceptual del sistema.

No actualices los capítulos cuando el cambio sea únicamente:

- una corrección interna que no modifica el modelo conceptual;
- un cambio de nombres, formato, estilo o refactorización equivalente;
- una prueba adicional que no introduzca una técnica o propiedad nueva;
- una modificación puntual ya explicada por el capítulo;
- una corrección de documentación que no cambie lo que el sistema hace.

Los cambios pequeños que sí introduzcan un concepto nuevo deben producir una
actualización puntual. No reescribas el capítulo completo para incorporar una
sola definición, relación, flujo o ejemplo.

# Fuente de verdad y evidencia

Determina el comportamiento a partir de evidencia del repositorio, en este orden:

1. código y configuración implementados;
2. pruebas que demuestren comportamiento;
3. especificaciones vigentes en `openspec/specs/` y cambios archivados en
   `openspec/changes/archive/`;
4. documentación existente, solo como contexto y no como prueba de una
   implementación.

Si `graphify-out/graph.json` existe, comienza con una consulta acotada de
`graphify` para localizar las relaciones relevantes. Después lee solo los
archivos necesarios para confirmar el concepto. No hagas un inventario amplio
del repositorio ni supongas que una idea está implementada porque aparece en un
plan o en una especificación no aplicada.

Para cada concepto candidato, confirma:

- qué problema o mecanismo representa;
- qué parte implementada del sistema lo demuestra;
- qué capítulo lo explica de forma natural;
- si ya está explicado con suficiente profundidad.

Si no puedes confirmar una afirmación, no la agregues como hecho. Marca la
necesidad de confirmación en el reporte y deja el archivo intacto.

# Proceso

## 1. Identificar el cambio

Si el usuario proporciona un archivo, commit, capacidad, cambio OpenSpec o
rango de trabajo, úsalo como disparador. Si no proporciona uno, identifica los
cambios recientes mediante el estado y el historial de Git, priorizando los
archivos modificados desde la última actualización documental.

Expón brevemente qué cambio analizarás y qué evidencia usarás.

## 2. Extraer conceptos candidatos

Resume el cambio en términos de comportamiento y mecanismos, no solo de nombres
de clases. Busca conceptos como:

- una nueva frontera arquitectónica o patrón de comunicación;
- una nueva estrategia de persistencia, indexación o consistencia;
- una nueva propiedad de concurrencia, transacción o ciclo de vida;
- una nueva técnica de LLM, RAG, validación o seguridad;
- una nueva categoría de prueba, métrica o criterio de calidad;
- una nueva etapa de despliegue, configuración, migración u observabilidad;
- una relación entre componentes que antes no estaba explicada;
- una restricción del dominio que cambie la interpretación del sistema.

No confundas una API nueva con un concepto nuevo. Una API nueva puede requerir
solo una actualización puntual de un ejemplo o puede no requerir documentación
conceptual si el mecanismo ya está explicado.

## 3. Comparar con los capítulos actuales

Para cada concepto candidato, clasifícalo como:

- **Ya explicado:** el capítulo cubre definición, mecanismo y aplicación; no editar.
- **Actualización puntual:** falta un dato implementado, relación, flujo o ejemplo
  pequeño; editar solo esa parte.
- **Concepto nuevo:** requiere una subsección o una sección breve; ubicarlo en el
  capítulo más cercano al concepto principal.
- **No conceptual:** cambio técnico sin valor para estos capítulos; no editar.
- **No confirmado:** la evidencia no basta; no editar.

Elige un capítulo existente antes de crear uno nuevo. Solo propón un capítulo
nuevo si el cambio introduce un área conceptual independiente y no encaja de
forma razonable en ninguno de los ocho documentos.

## 4. Preservar la forma de los capítulos

Al editar:

- conserva el título y la numeración general del capítulo;
- conserva el índice y actualízalo si agregas o renombras una sección;
- define cada concepto dentro del texto, en el punto donde aparece;
- explica primero qué es, luego cómo funciona y finalmente cómo se aplica en
  Careme;
- usa Mermaid, tablas o ejemplos de código solo cuando aclaren el mecanismo;
- mantén el español para la prosa y conserva identificadores técnicos en inglés;
- vincula las afirmaciones a rutas del repositorio cuando el capítulo ya usa
  referencias de archivos;
- mantén la profundidad y el tono del capítulo existente;
- evita duplicar explicaciones que pertenecen a otro capítulo; enlaza o resume
  la relación;
- conserva las restricciones del dominio: Careme registra hechos clínicos
  declarados, no diagnostica ni recomienda tratamientos.

No agregues secciones tituladas `Ideas clave`, `Resumen` o `Conclusión`. No
conviertas el capítulo en un glosario ni añadas una sección separada de
referencias si el estilo existente no la necesita.

## 5. Actualizar el índice general

Actualiza `docs/concepts/README.md` solo si:

- cambia el alcance conceptual de un capítulo;
- una nueva sección altera de forma significativa su descripción;
- se crea un nuevo capítulo.

Mantén sus descripciones breves y coherentes con el contenido real. Si no cambia
el alcance, deja el índice intacto.

## 6. Aplicar cambios y validar

Aplica únicamente las ediciones justificadas por conceptos confirmados. Prefiere
parches pequeños y localizados.

Después valida:

- que el capítulo conserve un índice coherente con sus encabezados;
- que cada concepto nuevo aparezca integrado en la explicación;
- que no haya afirmaciones inventadas o promesas de funcionalidades no
  implementadas;
- que no existan las secciones prohibidas;
- que los enlaces internos y rutas mencionadas sean válidos;
- que `git diff --check` no encuentre errores en los archivos editados.

Si no hay conceptos nuevos, no edites ningún archivo. Ejecuta solo las
comprobaciones necesarias y reporta que la documentación conceptual ya cubre el
cambio.

# Formato de salida

Antes de editar, presenta una tabla breve:

| Concepto candidato | Evidencia | Capítulo | Decisión |
| --- | --- | --- | --- |
| ... | `ruta` | `06-...md` | Ya explicado / Actualización puntual / Concepto nuevo / No conceptual / No confirmado |

Si hay ediciones, aplícalas y termina con:

- capítulos modificados;
- concepto incorporado o precisión realizada en cada uno;
- validaciones ejecutadas;
- cambios que deliberadamente no generaron documentación.

Si no hay ediciones, termina con:

- cambio analizado;
- conceptos revisados;
- motivo por el que no se necesitó actualizar `docs/concepts/`;
- validaciones ejecutadas.

# Criterios de idempotencia

Reejecutar este prompt sobre el mismo cambio debe producir cero ediciones cuando
la documentación ya incorpora el concepto. No reformatees, no cambies sinónimos
ni amplíes el texto por preferencia editorial si no existe un cambio conceptual
confirmado.
