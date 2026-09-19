# Roadmap del Proyecto — Asistente de Historia Clínica Personal

## 1. Objeto del aplicativo

El proyecto tiene como objetivo construir un asistente basado en IA que permita registrar, organizar, consultar y sintetizar información médica relevante de una persona a lo largo del tiempo.

El usuario podrá informar al asistente hechos médicos mediante lenguaje natural, por ejemplo:

- "Me diagnosticaron hipertensión hace unos dos años."
- "Hoy me midieron la presión y fue 145 sobre 90."
- "Empecé a tomar este medicamento la semana pasada."

El asistente deberá interpretar la información, identificar qué tipo de hecho clínico representa y utilizar herramientas de persistencia para almacenarlo.

Posteriormente, el usuario podrá realizar preguntas como:

- "¿Cuándo me diagnosticaron hipertensión?"
- "¿Qué medicamentos he tomado?"
- "¿Qué problemas médicos he tenido recientemente?"
- "¿Qué valores de presión arterial se han registrado?"

El sistema deberá recuperar la información relevante antes de generar la respuesta y, cuando sea posible, conservar la trazabilidad hacia los registros que sustentan la respuesta.

### Principio arquitectónico fundamental

El LLM debe encargarse principalmente de interpretar la intención y el significado de la información, mientras que la aplicación debe controlar cómo se valida, persiste, recupera y versiona la información.

En otras palabras:

> El LLM decide qué significa la información; la aplicación decide cómo almacenarla.

---

## 2. Alcance funcional proyectado

El producto completo podrá evolucionar hacia las siguientes capacidades.

### 2.1. Registro de información clínica

- Registro mediante lenguaje natural.
- Diagnósticos y problemas médicos.
- Síntomas.
- Medicamentos y tratamientos.
- Mediciones y signos vitales.
- Resultados de laboratorio.
- Estudios diagnósticos.
- Procedimientos y cirugías.
- Hospitalizaciones y urgencias.
- Vacunaciones.
- Alergias e intolerancias.
- Antecedentes personales y familiares.
- Notas clínicas.
- Documentos médicos y sus fuentes.

> **Precisión para la Fase 4.** Los antecedentes personales y familiares se registran como hechos del
> paciente con el tipo dedicado `family_history` y el parentesco en el contenido («madre: diabetes tipo
> 2»); el perfil extendido de la Fase 4.10 los resume. El perfil del paciente (Fase 4.4) no los
> contiene: no son atributos del propio paciente.

### 2.2. Consulta de la historia

- Consulta mediante lenguaje natural.
- Búsqueda por texto.
- Búsqueda semántica.
- Filtros por fecha y tipo de evento.
- Recuperación de información relacionada.
- Consultas cronológicas.
- Consulta de la evolución de una condición.
- Consulta de medicamentos y tratamientos.
- Consulta de mediciones históricas.

### 2.3. Síntesis mediante IA

- Resúmenes de la historia clínica.
- Resúmenes por condición.
- Evolución temporal de problemas médicos.
- Comparación de mediciones.
- Identificación de cambios relevantes.
- Construcción de líneas de tiempo.
- Respuestas contextualizadas a preguntas clínicas del usuario.

### 2.4. Evidencia y trazabilidad

- Asociación de hechos con sus fuentes.
- Conservación de documentos originales.
- Distinción entre información original e información derivada por IA.
- Registro de procedencia.
- Nivel de certeza o estado de la información.
- Versionado de información.
- Posibilidad de reconstruir la fuente de una afirmación generada por el asistente.

### 2.5. Seguridad y operación

- Autenticación y autorización.
- Cifrado.
- Control de acceso.
- Auditoría.
- Backups.
- Recuperación ante desastres.
- Gestión de versiones.
- Protección frente a prompt injection y manipulación de documentos.
- Evaluación de alucinaciones y calidad de las respuestas.
- Requisitos de privacidad y regulación aplicables al ámbito geográfico de operación.

---

# 3. Principios de arquitectura

La arquitectura evolucionará progresivamente, pero se parte de los siguientes principios.

## 3.1. Markdown como representación canónica inicial

Los hechos clínicos se almacenarán inicialmente como documentos Markdown pequeños, legibles tanto por humanos como por modelos de lenguaje. Viven en el filesystem del backend, en `data/events/`, y son la fuente de verdad; PostgreSQL mantiene un índice derivado y reconstruible para la búsqueda, nunca la verdad.

La **consulta** (Fase 4.1) se representa también en Markdown, en un directorio hermano de `data/events/`, para que la procedencia que los eventos declaran en su front-matter sobreviva a la reconstrucción del índice. PostgreSQL la indexa igual que los eventos.

Ejemplo conceptual:

```markdown
---
id: 9f1c0b7e-4a2d-4c1e-9d3f-6b0a1c2d3e4f
code: evt_001
type: diagnosis
date: 2026-08-10
date_precision: exact
date_text: "el 10 de agosto"
source: patient
---

# Diagnosis

Hypertension was diagnosed.
```

Los archivos deberán contener metadatos mínimos y contenido semánticamente claro. El `id` es el UUID que identifica el evento y el `code` legible (`evt_NNN`) da nombre al archivo.

## 3.2. Los eventos son la fuente primaria

Los eventos clínicos representan hechos ocurridos o reportados.

Los resúmenes generados por IA serán información derivada y deberán poder reconstruirse a partir de los eventos originales.

Conceptualmente:

```text
Source / Evidence
       |
       v
Clinical Event
       |
       v
Derived Summary
```

Esto evita convertir una interpretación del LLM en una nueva fuente de verdad.

## 3.3. Separación entre almacenamiento e inteligencia

El agente no deberá conocer los detalles físicos de almacenamiento.

En lugar de permitir que el LLM manipule directamente rutas o archivos arbitrarios, se expondrán herramientas semánticas como:

- `create_event`
- `get_event`
- `list_events`
- `search_events`

Las herramientas se implementan en el backend (Spring Boot) y, desde la Fase 4.2, se exponen al LLM mediante *function calling* de una API compatible con OpenAI; el modelo nunca ve rutas ni ficheros. La implementación podrá cambiar de filesystem a object storage sin modificar el comportamiento conceptual del agente, y el mecanismo de recuperación podrá pasar de full-text a híbrido sin tocar las herramientas.

## 3.4. Retrieval híbrido

El MVP resuelve la recuperación con el full-text nativo de PostgreSQL (columna `tsvector` con índice GIN y configuración `simple`) más filtros estructurados por tipo y fecha.

El sistema no dependerá exclusivamente de embeddings. La evolución prevista es:

```text
Markdown
   |
   +-- Structured metadata
   |
   +-- Full-text search
   |
   +-- Semantic search
```

Cada mecanismo resolverá problemas diferentes:

- Búsqueda estructurada: fechas, tipos, estados y filtros.
- Full-text search: coincidencias literales.
- Búsqueda semántica: recuperación por significado.

---

# 4. Roadmap

## Fase 0 — Definición del modelo mínimo — ✅ completada

### Objetivo

Definir el concepto mínimo de información que permitirá construir y validar el producto sin introducir complejidad clínica innecesaria.

### Subfases

#### 0.1. Definir entidades mínimas

Inicialmente:

```text
Patient
ClinicalEvent
```

> **Revisión prevista en la Fase 4.4.** `Patient` deja de ser implícito: se materializa como perfil del
> paciente. `ClinicalEvent` se mantiene, con el catálogo de tipos revisado en §0.2.

#### 0.2. Definir tipos de evento

El MVP tendrá solamente unos pocos tipos:

- `diagnosis`
- `medication`
- `measurement`
- `note`

No se intentará representar toda la historia clínica en esta fase.

> **Revisión prevista en la Fase 4.3.** `measurement` sale del catálogo: la presión arterial, el peso,
> la circunferencia abdominal y el colesterol pasan a un modelo de mediciones con dimensión de métrica.
> El catálogo de eventos queda en `diagnosis`, `medication` y `note`, más los tipos que añade la
> Fase 4.5.

#### 0.3. Definir metadatos

Cada evento deberá tener, como mínimo:

- Identificador: UUID, más un `code` legible (`evt_NNN`) que da nombre al archivo.
- Tipo.
- Fecha (`date`), precisión (`date_precision`: `exact`, `approximate`, `unknown`) y expresión temporal original (`date_text`).
- Contenido.
- Fuente.
- Información de incertidumbre cuando corresponda.

#### 0.4. Definir formato Markdown

Establecer:

- Front matter.
- Convenciones de nombres: `data/events/evt_NNN.md`.
- Identificadores.
- Organización de directorios.
- Enlaces entre documentos.
- Reglas para fechas.
- Reglas para información incierta.

Regla de indexado: **todo evento escrito en Markdown queda indexado en PostgreSQL**. El índice es siempre derivado, de modo que pueda reconstruirse desde los ficheros.

> **Ampliación prevista en la Fase 4.1.** La consulta se representa también en Markdown, con su propio
> directorio y su propio código, y PostgreSQL la indexa igual que los eventos. La regla de indexado
> cubre por tanto eventos y consultas.

### Resultado

Una especificación pequeña y estable para representar hechos clínicos.

---

# Fase 1 — MVP técnico de almacenamiento — ✅ completada

### Objetivo

Crear un sistema capaz de almacenar y recuperar hechos clínicos sin utilizar todavía un LLM.

### Subfases

#### 1.1. Backend mínimo

El paciente es único e implícito. Implementar:

- Creación de evento.
- Consulta de evento.
- Listado de eventos.

#### 1.2. Persistencia

Utilizar inicialmente filesystem local. El directorio `data/events/` vive en la raíz del repositorio, se ignora en git y se monta como volumen en el contenedor del backend.

```text
data/
└── events/
    ├── evt_001.md
    ├── evt_002.md
    └── evt_003.md
```

PostgreSQL mantiene el índice de búsqueda derivado de esos ficheros; no es la fuente de verdad.

#### 1.3. API

Endpoints conceptuales, siguiendo la convención `/api/v1/...` que ya usa `MeasurementController`:

```text
POST /api/v1/clinical-events
GET  /api/v1/clinical-events
GET  /api/v1/clinical-events/{code}
POST /api/v1/clinical-events/reindex
```

La API de peso y circunferencia abdominal (`/api/v1/measurements`) permanece independiente durante el MVP.

#### 1.4. Interfaz mínima

Ampliar el frontend existente (Next.js, App Router) con una vista de eventos clínicos que permita:

- Crear un evento.
- Ver los eventos.
- Ordenarlos cronológicamente.

El seguimiento de peso y circunferencia abdominal sigue en su propia vista, `/measurements`; el chat llega en la Fase 2.

### Resultado

Primer sistema funcional de persistencia, todavía sin IA.

---

# Fase 2 — Integración del LLM como interfaz — ✅ completada

### Objetivo

Permitir que el usuario registre información mediante lenguaje natural.

### Subfases

#### 2.1. Integración del modelo

Incorporar el LLM en el backend (Spring Boot) a través de un proveedor compatible con OpenAI y establecer el flujo conversacional en la ruta `/` del frontend. La clave del proveedor se lee de una variable de entorno del backend y nunca se expone al navegador. El prompt de sistema vive en `backend/src/main/resources/prompts/`. En el MVP la respuesta se devuelve como JSON, sin streaming.

#### 2.2. Operaciones del LLM

El diseño preveía exponer `create_event()`, `get_event()` y `list_events()` al modelo por *function
calling*. Lo construido en el MVP fue distinto: el LLM devuelve una intención JSON y el backend ejecuta
la operación, sin exponer herramientas al modelo. El *function calling* llega en la Fase 4.2.

El backend inyecta la fecha actual en el contexto de la conversación, para que "hoy", "ayer" o "hace dos semanas" se resuelvan sin que el modelo tenga que adivinarla.

#### 2.3. Extracción de información

El LLM deberá identificar:

- Tipo de evento.
- Contenido.
- Fecha.
- Incertidumbre.
- Fuente.

#### 2.4. Control de incertidumbre

El sistema no deberá inventar datos.

Por ejemplo, si el usuario dice:

> "Hace unos dos años me diagnosticaron hipertensión."

No se deberá convertir automáticamente en una fecha exacta.

#### 2.5. Validación

El backend deberá validar los datos generados por el LLM antes de persistirlos.

### Resultado

El usuario puede decir:

> "Me diagnosticaron hipertensión hace dos años."

y el sistema puede convertirlo en un evento persistido como Markdown e indexado en PostgreSQL.

---

# Fase 3 — Consulta mediante lenguaje natural — ✅ completada

### Objetivo

Completar el ciclo fundamental del MVP:

```text
Usuario
   |
   v
LLM
   |
   +----> escribir información
   |
   +----> buscar información
               |
               v
             Datos
               |
               v
              LLM
               |
               v
            Respuesta
```

### Subfases

#### 3.1. Búsqueda textual

Incorporar el full-text nativo de PostgreSQL: columna `tsvector` con índice GIN y configuración `simple`, para no depender de un stemmer concreto con contenido mezclado en español e inglés.

#### 3.2. Índice

Mantener metadatos como:

```text
event_id
code
type
date
date_precision
path
content
content_hash
```

El paciente es único e implícito, así que no hay `patient_id`. El índice se deriva del Markdown y debe poder reconstruirse: el backend lo reconcilia al arrancar y expone además un reindexado explícito.

#### 3.3. Búsqueda de eventos

Implementar la operación `search_events`, ejecutada por el backend a partir de la intención del modelo
—no expuesta como herramienta al modelo; el *function calling* llega en la Fase 4.2—, con filtros
básicos por:

- Texto.
- Tipo.
- Fecha.

#### 3.4. Retrieval controlado

El LLM deberá decidir cuándo necesita consultar la historia.

#### 3.5. Respuestas basadas en evidencia

El asistente deberá responder a partir de los registros recuperados y no únicamente de su memoria conversacional.

#### 3.6. Persistencia del historial de conversación (diferida)

En el MVP la conversación vive en el navegador y no se persiste: cada petición lleva consigo los turnos recientes. Persistir el historial se evaluará cuando el producto necesite recuperar conversaciones anteriores, auditar qué se preguntó o mantener el contexto entre dispositivos.

La Fase 4.1 resuelve la parte que corresponde al registro clínico —la consulta como sesión con
identificador y procedencia de los hechos—; persistir el historial completo de la conversación sigue
pendiente de evaluación.

### Resultado

Este es el primer MVP de producto.

Debe permitir:

1. Contarle un hecho médico al asistente.
2. Persistirlo.
3. Preguntar posteriormente por él.
4. Recuperarlo.
5. Generar una respuesta basada en el registro.

### Criterio de éxito del MVP

El producto no necesita todavía:

- Vector DB.
- PDFs.
- OCR.
- Imágenes.
- Decenas de tipos clínicos.
- Dashboards.
- Resúmenes complejos.
- Agentes multi-step.
- Integraciones externas.

El objetivo es demostrar que el ciclo fundamental funciona de forma fiable.

### Cierre del MVP

El MVP se da por terminado el **2026-09-18**. El ciclo fundamental funciona y está verificado de forma
automatizada: 36 clases de prueba en `backend/src/test/` y 14 archivos de prueba en `frontend/src/`.

El alcance cerrado está en
[`mvp_alcance_asistente_historia_clinica.md`](./mvp_alcance_asistente_historia_clinica.md). La Fase 4
continúa desde aquí.

---

# Fase 4 — Modelo clínico enriquecido

### Objetivo

Convertir el asistente en un agente con herramientas, sostener la conversación como una consulta con
procedencia y ampliar la representación clínica, sin romper los principios de almacenamiento.

### Subfases

#### 4.1. Consulta

La conversación deja de ser un estado en memoria y pasa a ser una **consulta** con identificador y
ciclo de vida: el asistente conversa, pregunta lo que falta y **toma notas** de lo que el usuario
menciona. Los hechos que el usuario aporta se registran **con su procedencia**, referenciando la
consulta de la que salieron, en lugar de registrarse frase a frase.

La consulta se representa en Markdown, en un directorio hermano de `data/events/` (por ejemplo
`data/encounters/` con código `enc_NNN`, en paralelo a `evt_NNN`), de modo que la procedencia que los
eventos declaran en su front-matter sobreviva a la reconstrucción del índice. PostgreSQL la indexa
igual que los eventos.

El modelo admite los dos momentos de registro —al cierre de la conversación o a medida que avanza— y el
momento se decide al implementar.

La consulta puede conservar además un **resumen** propio, aparte del desglose en eventos. Si se guarda,
se trata como información derivada según §3.2: marcado como tal y regenerable desde los eventos de la
consulta, nunca fuente de verdad.

#### 4.2. Agente con herramientas

El asistente pasa a decidir qué necesita hacer y a llamar herramientas para hacerlo. En el MVP el LLM
devuelve una intención JSON y el backend decide la operación; aquí el modelo elige y llama las
herramientas por *function calling* de la API compatible con OpenAI.

El backend conserva la validación y la escritura dentro de la ruta de la llamada, de modo que ningún
hecho se persiste sin validar. El conjunto de herramientas acota lo que el agente puede hacer, que es
la garantía estructural de que el asistente no diagnostica ni recomienda tratamiento: solo puede
consultar y registrar hechos.

El proveedor LLM real —no el modo `fake` de desarrollo— pasa a ser el comportamiento por defecto del
asistente.

#### 4.3. Modelo de mediciones

Hoy conviven dos representaciones de una medición: el evento clínico `measurement`, que guarda el valor
como texto libre, y la tabla `measurements`, con dos columnas fijas y una fila por día. Ninguna sirve
para comparar valores en el tiempo, y ni la presión arterial ni el colesterol caben como dato numérico.

Esta subfase introduce un modelo de mediciones con **dimensión de métrica** —métrica, valor, unidad y
fecha con su precisión— que cubre peso, circunferencia abdominal, presión arterial, colesterol y
cualquier otra métrica que aparezca. Cada medición referencia la consulta de la que procede.

Las mediciones no se convierten en eventos clínicos: tienen su propio almacén, y `measurement` sale del
catálogo de tipos de evento de §0.2. La vista `/measurements` pasa a ser una vista derivada de ese
modelo, y el asistente deja de redirigir las preguntas sobre peso y circunferencia.

Con las mediciones en un modelo numérico, la comparación de valores que prevé la Fase 7.3 pasa a ser
posible.

#### 4.4. Perfil del paciente

Materializa `Patient`, que el MVP dejó implícito (§0.1). El perfil reúne los atributos del paciente que
no son hechos fechados:

- Nombre y apellidos.
- Fecha de nacimiento. La edad se deriva de ella y nunca se almacena.
- Sexo.
- Ocupación.
- Grupo sanguíneo y Rh.
- Estatura.

**Entrevista inicial.** La primera vez que el usuario interactúa con el asistente, este propone la
entrevista. El usuario puede saltarla y retomarla más adelante, y el asistente vuelve a pedir el dato
que falta cuando la conversación lo necesita. No es un muro de entrada.

**Actualización.** Cuando el usuario reporta un cambio, el perfil se actualiza.

**Solo valores vigentes.** El perfil conserva únicamente el valor actual de cada dato: el documento se
sobrescribe al actualizar y los valores del perfil no llevan historial propio. Corregir un dato descarta
el anterior, y la procedencia de §4.9 y §2.4 aplica a los hechos clínicos, no a los valores del perfil.
El versionado de registros de la Fase 8.4 es el sitio natural si más adelante se quiere conservar.

**Representación.** Un documento Markdown del paciente en un directorio hermano de `data/events/`, con
el mismo principio que la consulta (§4.1) y la regla de indexado de §0.4.

**Un dato puede ser desconocido.** Se registra como ausente y nunca se inventa.

**Lo que el perfil no contiene.** Alergias, vacunas, antecedentes, condiciones y resultados de
laboratorio son hechos clínicos: viven en los tipos de evento de §4.5 y en las condiciones de §4.6, y se
resumen en el perfil extendido de §4.10. Duplicarlos aquí crearía dos fuentes de verdad para lo mismo.

#### 4.5. Nuevos tipos de evento

Añadir progresivamente, junto a `diagnosis`, `medication` y `note`:

- `symptom`
- `procedure`
- `investigation`
- `hospitalization`
- `vaccination`
- `allergy`
- `family_history`

#### 4.6. Condiciones longitudinales

Introducir el concepto de `Condition`.

Una condición puede relacionar múltiples eventos:

```text
Condition: Hypertension
    |
    +-- Diagnosis
    +-- Measurement
    +-- Medication
    +-- Follow-up
```

#### 4.7. Estados

Incorporar estados como:

- Active.
- Resolved.
- Suspected.
- Ruled out.
- Recurrent.
- Unknown.

#### 4.8. Relaciones

Permitir relaciones entre:

- Condiciones.
- Eventos.
- Medicamentos.
- Estudios.
- Procedimientos.

#### 4.9. Provenance

Añadir:

- Quién proporcionó la información.
- Cuándo fue registrada.
- De qué consulta procede (§4.1).
- De qué documento procede.
- Si fue extraída automáticamente.
- Si fue confirmada.
- Nivel de confianza.

#### 4.10. Perfil extendido

Composición automática de un documento derivado: el perfil del paciente más sus hechos principales,
entre ellos las condiciones activas, las alergias, la medicación vigente, las últimas mediciones y los
antecedentes familiares.

Es información derivada en el sentido de §3.2: marcada como tal y reconstruible desde los hechos, nunca
una fuente de verdad. Se apoya en las condiciones de §4.6 y en las relaciones de §4.8, así que la
riqueza que añade esta misma fase es la que le da contenido.

#### 4.11. Requisitos de ingeniería de la fase

No son casos de uso: son condiciones de calidad que la fase debe cumplir.

- **Suite de extremo a extremo.** Recorrer con Playwright los siete casos conversacionales del alcance
  §15 a través de la interfaz real, con backend, PostgreSQL y proveedor LLM en ejecución.
- **Corpus de evaluación.** Un conjunto de 20 a 100 hechos clínicos ficticios, suficientemente variado,
  con un conjunto independiente de preguntas de prueba, para medir que los hechos se almacenan, se
  recuperan, que las consultas temporales funcionan y que la información inexistente no produce
  invenciones.

El cómo de ambas cosas está en `docs/standards/next-standards.md` y
`docs/standards/java-springboot-standards.md`.

### Resultado

El asistente es un agente que sostiene consultas, registra hechos clínicos y mediciones con procedencia,
y el sistema representa una historia clínica longitudinal más rica.

---

# Fase 5 — Documentos y evidencia

### Objetivo

Incorporar documentos médicos originales y relacionarlos con los hechos registrados.

### Subfases

#### 5.1. Entidad Document

Introducir:

```text
Document
```

para representar PDFs, informes, imágenes y otros archivos.

#### 5.2. Object storage

Migrar el almacenamiento de archivos desde filesystem hacia una solución de object storage cuando sea necesario.

Opciones posibles:

- S3.
- Cloudflare R2.
- Google Cloud Storage.
- Azure Blob Storage.
- MinIO.

#### 5.3. Extracción

Procesar documentos para obtener texto.

#### 5.4. Representación Markdown

Generar documentos Markdown derivados de documentos originales cuando sea útil para el retrieval.

#### 5.5. Relación evidencia-hecho

Permitir:

```text
Clinical Event
     |
     +-- Evidence
             |
             +-- Original Document
```

### Resultado

El sistema puede distinguir entre información reportada por el usuario, información extraída de documentos y hechos respaldados por evidencia documental.

---

# Fase 6 — Retrieval semántico y RAG

### Objetivo

Mejorar la capacidad de recuperación cuando las consultas y los documentos no utilizan las mismas palabras.

### Subfases

#### 6.1. Embeddings

Generar embeddings de documentos o fragmentos relevantes.

#### 6.2. Vector search

Incorporar una solución vectorial, inicialmente potencialmente mediante `pgvector`.

#### 6.3. Retrieval híbrido

Combinar:

```text
Lexical Search
      +
Metadata Filters
      +
Semantic Search
```

#### 6.4. Ranking

Implementar mecanismos para ordenar los resultados por relevancia.

#### 6.5. Evaluación

Crear un conjunto de preguntas reales y medir:

- Recall.
- Precision.
- Relevancia.
- Información omitida.
- Información incorrectamente recuperada.

### Resultado

El asistente puede recuperar información relevante incluso cuando existe variación considerable en la terminología utilizada.

---

# Fase 7 — Inteligencia longitudinal

### Objetivo

Pasar de recuperar datos a comprender la evolución temporal de la historia.

### Subfases

#### 7.1. Timeline

Generar líneas de tiempo clínicas.

#### 7.2. Resúmenes por condición

Ejemplo:

> "Resume la evolución de mi hipertensión."

#### 7.3. Comparación de mediciones

Ejemplo:

> "¿Cómo han cambiado mis valores de presión arterial?"

#### 7.4. Correlación temporal

Relacionar:

- Síntomas.
- Diagnósticos.
- Medicamentos.
- Estudios.
- Resultados.

#### 7.5. Síntesis con evidencia

Generar respuestas que indiquen qué registros sustentan cada conclusión importante.

### Resultado

El asistente puede construir una visión longitudinal de la historia médica, en lugar de limitarse a recuperar documentos individuales.

---

# Fase 8 — Seguridad, auditoría y producción

### Objetivo

Preparar el sistema para un entorno real donde se procesan datos médicos sensibles.

### Subfases

#### 8.1. Identidad y acceso

- Autenticación.
- Autorización.
- Control de acceso por paciente.
- Gestión de sesiones.

#### 8.2. Protección de datos

- Cifrado en tránsito.
- Cifrado en reposo.
- Gestión segura de secretos.
- Políticas de backup.

#### 8.3. Auditoría

Registrar:

- Quién accedió.
- Qué información consultó.
- Qué información modificó.
- Qué operaciones ejecutó el agente.
- Qué documentos utilizó una respuesta.

#### 8.4. Versionado

Conservar versiones de los registros clínicos.

#### 8.5. Seguridad del agente

Evaluar:

- Prompt injection.
- Manipulación de documentos.
- Tool abuse.
- Acceso no autorizado.
- Alucinaciones.
- Exfiltración de información.

#### 8.6. Privacidad y regulación

Analizar los requisitos legales y regulatorios aplicables a la jurisdicción y al uso previsto del sistema.

### Resultado

Sistema preparado para una operación controlada en producción.

---

# 5. Arquitectura evolutiva

La arquitectura no debe introducir toda la infraestructura desde el primer día.

## MVP — completado el 2026-09-18

El alcance cerrado está en
[`mvp_alcance_asistente_historia_clinica.md`](./mvp_alcance_asistente_historia_clinica.md).

```text
           User
             |
             v
   frontend (Next.js)
     |              |
     |              +--> /measurements ------> backend --> measurements (PostgreSQL)
     |              |
     |              +--> /clinical-events ---> backend --> índice clínico (solo lectura)
     |
     +--> / (chat)
            |
            v
   backend (Spring Boot) --> LLM (compatible con OpenAI, modo JSON)
            |                          |
            |                          v
            |                  intención: events | query |
            |                  clarification | conversation
            |
    +-------+--------+
    |                |
    v                v
create_event    search_events     (ejecutadas por el backend)
    |                |
    v                v
 Markdown   <->  índice full-text
 data/events/      (PostgreSQL)
```

Markdown es la fuente de verdad; el índice de PostgreSQL se deriva de él y puede reconstruirse.

## Etapa intermedia

```text
             User
               |
               v
     frontend (Next.js)
               |
               v
     backend (Spring Boot) --> LLM
               |
      +--------+---------+
      |                  |
      v                  v
  Clinical tools    Retrieval (FTS + filtros + vector)
      |                  |
      v                  v
   Markdown          PostgreSQL
 data/events/    (índice + pgvector)
      |
      v
  Object Storage
```

PostgreSQL ya está presente desde el MVP como índice; en esta etapa incorpora además la búsqueda vectorial. Las herramientas clínicas y el agente no son novedad de esta etapa: llegan en la Fase 4.

## Arquitectura avanzada

```text
                         User
                           |
                           v
                       LLM Agent
                           |
             +-------------+-------------+
             |                           |
          Write                       Retrieve
             |                           |
             v                           v
       Clinical Tools             Retrieval Layer
             |                           |
             v                  +--------+--------+
        PostgreSQL              |        |        |
        Metadata              FTS   Vector    Filters
             |                           |
             +-------------+-------------+
                           |
                           v
                     Object Storage
                  MD / PDFs / Images
```

El almacenamiento de objetos pasa a alojar también los documentos Markdown, que siguen siendo la fuente de verdad de los hechos clínicos. El agente y las herramientas clínicas ya existen desde la Fase 4; lo propio de esta etapa es la recuperación híbrida y el almacenamiento de objetos.

---

# 6. Priorización

La prioridad de desarrollo será:

```text
P0 — Imprescindible para MVP (completado)
    - ClinicalEvent (paciente único e implícito)
    - Markdown en data/events/
    - Índice full-text derivado en PostgreSQL
    - Operaciones create_event, get_event, list_events y search_events,
      ejecutadas por el backend (no expuestas al modelo como herramientas)
    - LLM (proveedor compatible con OpenAI, desde el backend)
    - Vista de peso y circunferencia abdominal sobre su propia tabla

P1 — Primeras mejoras (Fase 4 y Fase 5)
    - Fase 4: agente con herramientas y proveedor LLM real por defecto
    - Fase 4: consulta con identificador y procedencia de los hechos
    - Fase 4: modelo de mediciones con dimensión de métrica
    - Fase 4: perfil del paciente y perfil extendido
    - Fase 4: más tipos de eventos
    - Fase 4: Condition, estados y relaciones
    - Fase 4: provenance
    - Fase 4: requisitos de ingeniería de la fase (suite e2e y corpus de evaluación)
    - Fase 5: documentos
    - Fase 5: object storage

P2 — Inteligencia avanzada
    - Embeddings
    - Vector search
    - Hybrid retrieval
    - Timelines
    - Resúmenes longitudinales

P3 — Producción
    - Seguridad
    - Auditoría
    - Versionado
    - Backups
    - Compliance
```

---

# 7. Criterio general de evolución

Cada fase deberá producir una capacidad funcional verificable.

No se deberá avanzar a una nueva fase simplemente porque la arquitectura "está preparada".

La regla será:

> Construir la mínima infraestructura necesaria para validar la siguiente capacidad del producto.

Especialmente durante las primeras fases, se evitará introducir componentes cuya utilidad todavía no haya sido demostrada.

Por tanto, el proyecto deberá evolucionar aproximadamente así:

```text
1. Guardar hechos
       ↓
2. Hablar con el sistema para guardarlos
       ↓
3. Preguntar por ellos
       ↓
4. Modelar mejor la información clínica
       ↓
5. Incorporar documentos y evidencia
       ↓
6. Mejorar el retrieval
       ↓
7. Comprender evolución longitudinal
       ↓
8. Preparar producción
```

El punto de validación más importante es el final de la Fase 3.

Si el sistema ya permite registrar hechos mediante lenguaje natural, recuperarlos posteriormente y responder preguntas correctamente basándose en ellos, existe un núcleo funcional sobre el cual tiene sentido invertir en las fases posteriores.
