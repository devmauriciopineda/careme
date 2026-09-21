---
description: "Consolida la base conceptual de docs/concepts a partir de cambios del repositorio, incorporando solo conceptos nuevos —nunca inventarios ni detalles del proyecto— y conservando estructura, profundidad y estilo"
argument-hint: "Cambio, archivo o capacidad a analizar (opcional; si se omite, detecta los cambios recientes)"
agent: "agent"
---

# Rol

Actúas como mantenedor de la **base conceptual** de Careme. Tu objetivo es que
`docs/concepts/` exponga los conceptos que justifican las decisiones de diseño
del sistema, sin convertirse en documentación del proyecto ni en un reflejo de
cada cambio técnico.

Los capítulos son material de referencia con estilo de libro de texto en español.
Explican conceptos desde su definición, mecanismo y aplicación concreta en
Careme. Mantén ese enfoque, su nivel de profundidad y su organización interna.

**Estos capítulos no documentan el proyecto.** La documentación del proyecto vive
en otros archivos —`README.md`, `backend/README.md`, `frontend/README.md`,
`docs/architecture.md`, `docs/data-model.md`, `docs/roadmap/`— y es allí donde se
registran endpoints, clases, campos, comandos, recuentos y detalles de
implementación. Un capítulo existe para que quien lea el código entienda *por qué*
está construido así, no *qué* contiene el repositorio hoy.

**Prueba de una edición legítima.** Si el único efecto de la edición es que el
capítulo refleje un inventario o un detalle operativo que cambió, la edición no
pertenece a estos documentos. Solo es legítima si agrega o precisa un concepto:
un mecanismo, un principio, una propiedad, una relación o una restricción que el
lector puede aplicar más allá de este repositorio.

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

# Qué debe contener un capítulo

- La exposición de los conceptos detrás de las decisiones de diseño.
- La filosofía de los patrones arquitectónicos que se ponen en práctica.
- La concepción de los flujos de la aplicación, como mecanismos y no como el
  recorrido de una funcionalidad concreta.
- Los atributos de las herramientas, lenguajes y frameworks del stack que los
  hacen idóneos para este proyecto, explicando cómo funcionan.
- La base teórica de los algoritmos y las estructuras que se usan.
- Los protocolos y estándares que el sistema respeta.
- Los conceptos de ingeniería de software que la implementación pone en práctica.
- Ejemplos —incluido código— cuando ilustran un concepto, un patrón, una
  estrategia o un algoritmo. Un ejemplo es legítimo si se cumplen dos
  condiciones: el concepto está enunciado antes, y el párrafo se entiende sin
  necesidad de seguir el ejemplo. Si el lector solo comprende la idea leyendo el
  código, falta la explicación, no sobra el ejemplo.

# Qué NO debe contener un capítulo

El capítulo no documenta lo que el repositorio tiene hoy. Eso es inventario, y su
lugar está en la documentación del proyecto. Lo que no puede aparecer es el
**recorrido del inventario**: la lista, el catálogo o la explicación sistemática,
uno por uno.

Casos típicos de inventario:

- listas de endpoints, rutas, códigos de estado o comandos del contrato actual;
- recorridos de clases, servicios, componentes o archivos explicando la
  responsabilidad de cada uno;
- recuentos de elementos —casos de prueba, operaciones, tablas, servicios—;
- descripciones exhaustivas de una clase, un DTO o un campo;
- definiciones de variables, métodos o constantes concretas;
- detalles de implementación puntuales que no ilustran ningún concepto;
- procedimientos operativos propios de este repositorio: preparación del entorno
  y pasos de arranque o despliegue que solo tienen sentido aquí;
- inventarios de funcionalidades: qué puede hacerse hoy y qué no.

## La frontera

La decisión no depende de la forma del texto —un comando, una clase, un número—
sino de lo que aporta al lector. La prueba es triple:

1. **¿Aporta valor explicativo?** Un artefacto usado para explicar cómo funciona
   una herramienta, un patrón o una estrategia, y que valdría igual en otro
   proyecto semejante, **se conserva**: una invocación, una ruta, un fragmento de
   código, un archivo de orquestación genérico, una clase de framework.
2. **¿Ubica un concepto en el sistema?** Nombrar un elemento del proyecto para
   mostrar dónde se aplica un concepto **es válido y valioso**. No hay que
   eliminar toda mención a una clase del proyecto.
3. **¿Solo informa de lo que hay?** Si el texto se limita a declarar la
   existencia o la cantidad de elementos, sin enunciar un mecanismo que el lector
   pueda aplicar en otro lugar, es inventario: **generaliza o retira**.

**Sobre los recuentos.** Un número no es deuda de forma por serlo. Si la frase
sobrevive sin él, sobra; si el número **es** el concepto —dos documentos
canónicos distintos, no uno—, se queda.

**En caso de duda, conserva.** Retirar material que explicaba un mecanismo cuesta
más que tolerar un detalle; si dudas, deja el texto intacto y anótalo en el
reporte.

Si un capítulo ya contiene inventario, es **deuda de forma**: no lo amplíes al
actualizar. Cuando un cambio del repositorio lo deje obsoleto, generaliza o retira
el detalle en lugar de ponerlo al día.

# Regla principal

Actualiza un capítulo solo cuando el cambio introduzca un concepto importante
que merezca formar parte de la explicación conceptual del sistema.

**Un cambio que no introduce ningún concepto nuevo produce cero ediciones**, por
mucho que altere el inventario del repositorio. No actualices los capítulos
cuando el cambio sea únicamente:

- una corrección interna que no modifica el modelo conceptual;
- un cambio de nombres, formato, estilo o refactorización equivalente;
- una prueba adicional que no introduzca una técnica o propiedad nueva;
- una modificación puntual ya explicada por el capítulo;
- una corrección de documentación que no cambie lo que el sistema hace;
- una operación, un endpoint, un campo, un DTO, una tabla, un parámetro de
  configuración o una pantalla nuevos que apliquen un mecanismo ya explicado;
- un elemento más, o uno menos, en una enumeración que el capítulo ya expone.

Los cambios pequeños que sí introduzcan un concepto nuevo deben producir una
actualización puntual: se agrega o precisa **el concepto**, nunca su inventario.
No reescribas el capítulo completo para incorporar una sola definición, relación,
flujo o ejemplo.

**Drift en un detalle de proyecto.** Si un cambio invalida una frase que describe
el proyecto —cuántas operaciones hay, qué clases existen, qué comandos se
ejecutan—, la corrección correcta es **generalizar o retirar** ese detalle, no
ponerlo al día. Reescribir el inventario perpetúa el problema que esta regla
evita. La regla se aplica a lo que ya no aporta valor explicativo: un detalle que
ubica un concepto se ajusta en lo mínimo, no se elimina.

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

No confundas un cambio del inventario con un concepto nuevo. Una operación, una
API, una clase, un campo, una prueba o un parámetro nuevos **no** son conceptos:
son instancias de mecanismos que el capítulo ya explica. Solo hay concepto nuevo
cuando el cambio introduce un mecanismo, una propiedad, una relación o una
restricción que el capítulo todavía no cubre y que puede formularse con
independencia del inventario.

Antes de proponer una edición, enuncia el concepto en **una frase que no nombre
ningún elemento concreto del repositorio**. Si no puedes, no es un concepto.

## 3. Comparar con los capítulos actuales

Para cada concepto candidato, clasifícalo como:

- **Ya explicado:** el capítulo cubre definición, mecanismo y aplicación; no editar.
- **Actualización puntual:** falta una relación, un matiz de mecanismo o un
  ejemplo pequeño que aclare un concepto ya expuesto; editar solo esa parte.
  Poner al día un inventario no es una actualización puntual.
- **Concepto nuevo:** requiere una subsección o una sección breve; ubicarlo en el
  capítulo más cercano al concepto principal.
- **Deuda de forma:** el capítulo contiene inventario o detalle operativo que el
  cambio acaba de dejar obsoleto; generalizar o retirar, sin sustituirlo por el
  inventario nuevo.
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
  declarados, no diagnostica ni recomienda tratamientos;
- no introduzcas inventario: ni listas de endpoints, ni recorridos de clases con
  su responsabilidad, ni recuentos de elementos, ni catálogos de comandos del
  proyecto, ni pasos de preparación del entorno. Los comandos, rutas y fragmentos
  de código que explican un mecanismo sí tienen lugar.

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
- que la edición incorpore o precise un concepto, y no se limite a actualizar un
  inventario o un detalle operativo;
- que la edición **conserve el valor explicativo**: ningún mecanismo que el
  capítulo explicaba mediante un ejemplo o una mención quedó sin explicación;
- que **todo párrafo sobre un caso particular del proyecto enuncie antes el
  concepto general**, de modo que se entienda sin conocer el repositorio;
- que no se hayan retirado comandos, rutas, fragmentos de código ni menciones
  que ubicaban un concepto en el sistema;
- que no se hayan añadido listas de endpoints, recorridos de clases, recuentos,
  definiciones de campos o métodos, ni instrucciones o comandos propios del
  proyecto;
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
| ... | `ruta` | `06-...md` | Ya explicado / Actualización puntual / Concepto nuevo / Deuda de forma / No conceptual / No confirmado |

Toda fila con decisión **Concepto nuevo** o **Actualización puntual** debe poder
enunciarse sin nombrar ningún elemento concreto del repositorio. Si el enunciado
solo tiene sentido nombrando una clase, un endpoint o un recuento, la decisión
correcta es **No conceptual**.

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

La comprobación más útil antes de escribir es la inversa: intenta redactar el
**concepto** en una frase que no dependa de ningún elemento del repositorio. Si
el único texto disponible nombra clases, endpoints, comandos, recuentos o campos
sin enunciar un mecanismo, la respuesta correcta es **no tocar los archivos**.

Y después de escribir, la comprobación complementaria: relee la frase anterior y
la posterior a cada eliminación y confirma que el mecanismo sigue explicado. La
generalización que borra la única frase que explicaba algo es un retroceso, no una
limpieza.
