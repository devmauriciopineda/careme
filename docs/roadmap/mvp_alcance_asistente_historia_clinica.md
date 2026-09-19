# Especificación de alcance — MVP del asistente de historia clínica personal

> **Estado: MVP terminado el 2026-09-18.** Este documento describe el software construido.
> El roadmap que continúa desde aquí está en
> [`roadmap_asistente_historia_clinica.md`](./roadmap_asistente_historia_clinica.md), donde la Fase 4
> recoge el modelo clínico enriquecido, el agente con herramientas y las mediciones.

## 1. Propósito

Este documento formaliza el alcance del MVP del asistente de historia clínica personal.

El objetivo es construir el sistema mínimo que permita completar de forma fiable el siguiente ciclo:

> Te cuento algo → lo guardas → después te pregunto → lo encuentras → me respondes.

El producto se concibe como un sistema de memoria médica personal asistido por un LLM. El LLM interpreta el lenguaje natural y decide cuándo la pregunta depende de la historia registrada; la aplicación es responsable de validar, ejecutar y persistir los datos.

El MVP prioriza simplicidad, trazabilidad y comportamiento predecible sobre sofisticación clínica o arquitectónica.

---

## 2. Objetivo del MVP

El MVP debe permitir a una única persona:

1. Registrar hechos médicos mediante lenguaje natural.
2. Convertir esos hechos en registros clínicos mínimos.
3. Persistir los registros en archivos Markdown e indexarlos en PostgreSQL.
4. Buscar registros mediante búsqueda textual.
5. Hacer preguntas sobre su propia historia clínica.
6. Recuperar primero los registros relevantes cuando la pregunta dependa de la historia del paciente.
7. Generar respuestas basadas exclusivamente en los registros recuperados cuando se trate de información histórica.
8. Representar fechas imprecisas sin inventar una precisión que el usuario no proporcionó.

La prueba fundamental del MVP es:

```text
Lenguaje natural
    ↓
Extracción
    ↓
ClinicalEvent
    ↓
Markdown
    ↓
Índice de búsqueda
    ↓
Búsqueda
    ↓
Contexto recuperado
    ↓
Respuesta
```

---

## 3. Alcance funcional

### 3.1 Registro de información

El usuario podrá expresar hechos médicos de forma conversacional.

Ejemplo:

```text
Me diagnosticaron hipertensión el 10 de agosto.
```

El sistema deberá:

1. Interpretar el mensaje.
2. Determinar si contiene un hecho que deba persistirse.
3. Extraer la información mínima disponible.
4. Construir un `ClinicalEvent`.
5. Validar el evento.
6. Persistirlo como Markdown y actualizar el índice de búsqueda en PostgreSQL.

Otro ejemplo:

```text
Hoy mi presión fue 145 sobre 92.
```

Debe producir un evento de tipo `measurement`.

### 3.2 Consulta de información

El usuario podrá realizar preguntas sobre su historia.

Ejemplo:

```text
¿Cuándo me diagnosticaron hipertensión?
```

El sistema deberá:

1. Identificar que la pregunta depende de la historia del paciente.
2. Ejecutar recuperación de información.
3. Buscar eventos relevantes.
4. Proporcionar los resultados al LLM.
5. Generar una respuesta basada en esos registros.

La conversación reciente no debe considerarse la base de datos principal. Para preguntas históricas, el sistema debe recuperar la información persistida.

### 3.3 Preguntas que no requieren recuperación

No toda pregunta necesita consultar la historia clínica.

Ejemplo:

```text
¿Qué es la hipertensión?
```

Esta pregunta puede responderse como conocimiento general y no requiere recuperar eventos del paciente.

Regla operativa:

> Si la pregunta trata sobre la historia del paciente, recuperar primero. Si no trata sobre su historia, no recuperar innecesariamente.

### 3.4 Capacidades incorporadas durante el MVP

Además del ciclo mínimo, el MVP incorporó capacidades que no estaban previstas en la primera
redacción de este alcance y que forman parte del software construido:

- **Conversación general.** Una pregunta que no depende de la historia se responde como
  conversación, y una petición de diagnóstico, recomendación o interpretación del propio caso se
  rechaza explícitamente (UC-010).
- **Conversación acotada.** El estado se guarda por conversación con caducidad y número máximo
  acotado, y los turnos recientes se usan solo para interpretar preguntas de seguimiento.
- **Idempotencia y duplicados.** Repetir un `messageId` no produce un segundo efecto, y un evento
  repetido dentro de la conversación se responde como duplicado en lugar de registrarse dos veces.
- **Respuesta mixta.** Cuando un mensaje mezcla una parte general y otra que depende de la historia,
  ambas se atienden por separado y la parte que los registros no respaldan se declara en lugar de
  responderse.
- **Ausencia accionable.** Tras declarar que no hay registros, el turno ofrece salidas concretas:
  reformular la pregunta o registrar el hecho.
- **Redirección del seguimiento corporal.** Una pregunta sobre peso o circunferencia abdominal se
  responde con una redirección explícita, sin leer ni escribir la tabla `measurements` (§14).
- **Importación de mediciones desde CSV** (UC-003), con previsualización y validación previa.

---

## 4. Modelo de datos mínimo

El MVP tendrá conceptualmente dos entidades:

```text
Patient
ClinicalEvent
```

Sin embargo, dado que el MVP contempla un único paciente, `Patient` puede permanecer implícito en la primera implementación.

> **Revisión prevista en la Fase 4.4.** `Patient` deja de ser implícito: se materializa como perfil del
> paciente, con nombre, fecha de nacimiento, sexo, ocupación, grupo sanguíneo y Rh, y estatura.
> Sigue habiendo un único paciente; lo que cambia es que deja de ser invisible al sistema.

### 4.1 ClinicalEvent

Campos mínimos:

```text
id            # UUID del evento
code          # identificador legible y estable, evt_NNN
type
date
content
source
created_at
```

Campos de fecha e incertidumbre:

```text
date_precision   # exact | approximate | unknown
date_text        # expresión temporal original del usuario, si la hubo
```

`date` puede quedar vacío cuando la fecha es desconocida; `date_precision` y `date_text` conservan lo que el usuario dijo realmente.

### 4.2 Tipos de evento iniciales

El MVP tendrá únicamente cuatro tipos:

```text
diagnosis
medication
measurement
note
```

No se crearán entidades clínicas especializadas para el MVP.

> **Revisión prevista en la Fase 4.** `measurement` sale del catálogo de eventos clínicos. Las
> mediciones —peso, circunferencia abdominal, presión arterial, colesterol— pasan a un modelo propio
> con dimensión de métrica, referenciado a la consulta. El catálogo de eventos queda en `diagnosis`,
> `medication` y `note`, más los tipos que añade la Fase 4. Ver
> [`roadmap_asistente_historia_clinica.md`](./roadmap_asistente_historia_clinica.md), Fase 4.

### 4.3 Ejemplo de evento

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

Otro ejemplo:

```markdown
---
id: 3b7d2a51-8c4e-4f0a-9e21-5a6b7c8d9e0f
code: evt_002
type: measurement
date: 2026-09-12
date_precision: exact
date_text: "hoy"
source: patient
---

# Blood pressure

Blood pressure was 145/92 mmHg.
```

El contenido del evento debe permanecer suficientemente flexible. El MVP no pretende modelar de forma exhaustiva cada dominio clínico.

---

## 5. Fechas e incertidumbre

Las fechas deben conservar el nivel de precisión que realmente proporciona el usuario.

El sistema debe soportar expresiones como:

```text
Hoy
Ayer
Hace dos semanas
El mes pasado
Hace unos dos años
En 2023
```

No debe convertir automáticamente una fecha aproximada en una fecha exacta inventada.

Por ejemplo:

```text
Hace unos dos años me diagnosticaron hipertensión.
```

No debe convertirse arbitrariamente en:

```text
2024-09-12
```

Se utilizan dos campos:

```text
date_precision: exact | approximate | unknown
date_text: "hace unos dos años"
```

`date_text` conserva la expresión original del usuario y `date_precision` impide que una fecha aproximada se presente como exacta.

Principio:

> La precisión temporal almacenada no puede ser superior a la precisión proporcionada por el usuario sin una justificación explícita.

---

## 6. Persistencia

La fuente de verdad del MVP será el filesystem.

Los eventos se almacenan como archivos Markdown en el directorio que indica
`CAREME_EVENTS_DIRECTORY` (por defecto `data/events`, `application.yml:30`). La ruta es relativa al
directorio de trabajo del proceso, de modo que la ubicación efectiva depende de cómo se arranque el
backend, y el directorio se crea si no existe (`ClinicalEventMarkdownStore.java:42`):

| Arranque | Directorio efectivo | Persistencia |
| --- | --- | --- |
| `podman compose up` / `docker compose up` | `/app/data/events` | volumen nombrado `careme-events` (`docker-compose.yml:37`) |
| `.\mvnw.cmd spring-boot:run` desde `backend/` | `backend/data/events` | sistema de archivos local |
| `spring-boot:run` desde la raíz | `data/events` | sistema de archivos local |

En el flujo con contenedores el contenido clínico **no vive en el árbol del repositorio**: reside en
el volumen nombrado `careme-events`, que sobrevive a la reconstrucción del contenedor y del cual se
reconstruye el índice en cada arranque. La regla `data/` de `.gitignore` cubre las dos rutas locales
(raíz y `backend/`), para que un arranque de desarrollo no deje el contenido clínico al alcance de
git.

```text
<CAREME_EVENTS_DIRECTORY>/         # por defecto data/events
├── evt_001.md
├── evt_002.md
└── ...
```

PostgreSQL se usa únicamente como índice derivado y reconstruible para la búsqueda, nunca como fuente de verdad. El backend (Spring Boot) es quien escribe los ficheros y quien mantiene el índice al día.

Markdown se elige porque:

- es legible por humanos;
- es fácil de inspeccionar;
- es fácil de versionar durante el desarrollo;
- puede ser leído por herramientas posteriores;
- mantiene el contenido clínico en un formato simple.

La aplicación, y no el LLM, será responsable de escribir los archivos.

---

## 7. Búsqueda

El MVP utilizará búsqueda textual, sin embeddings.

La implementación prevista es el full-text nativo de PostgreSQL: una columna `tsvector` con índice GIN y configuración `simple`, que evita depender de un stemmer concreto cuando el contenido mezcla español e inglés.

Conceptualmente, el índice podrá contener:

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

Dado que solo existe un paciente, no hay `patient_id`.

### 7.1 Operación de búsqueda

La operación `search_events` deberá permitir recuperar eventos mediante criterios como:

- texto;
- tipo de evento;
- fecha.

Ejemplos conceptuales:

```text
search_events("hypertension")
search_events(type="measurement")
search_events(text="blood pressure", date_from="2026-01-01")
```

El índice full-text será un mecanismo de recuperación, no la fuente primaria de verdad.

La relación conceptual será:

```text
Markdown = source of truth
Índice full-text de PostgreSQL = retrieval index
```

El índice es derivado y debe poder reconstruirse desde el Markdown: el backend lo reconcilia al arrancar y expone además un reindexado explícito.

---

## 8. Interpretación del LLM y herramientas del backend

El LLM no tiene acceso directo al filesystem, no conoce rutas físicas y **no ejecuta acciones**.
Se invoca contra una API compatible con OpenAI en modo JSON (`response_format: json_object`,
`OpenAiClinicalIntentInterpreter.java:61,66`) y devuelve una **intención** que el backend valida
antes de producir cualquier efecto:

```text
{"kind": "events",       "events": [...]}               # registrar hechos
{"kind": "query",        "search_terms": [...], ...}    # consultar la historia
{"kind": "clarification", "clarification": "..."}      # falta información para registrar
{"kind": "conversation"}                                # no depende de la historia
```

`ChatOrchestrator` interpreta esa intención y decide qué operación del backend se ejecuta
(`ChatOrchestrator.java:77`). Un fallo del proveedor no produce ninguna escritura: el turno termina
como `FAILED`.

El backend inyecta la fecha actual y la zona horaria en el contexto de la interpretación, para que
las expresiones relativas ("hoy", "ayer", "hace dos semanas") se resuelvan con el contexto temporal
correcto. En el MVP la respuesta del chat se devuelve como JSON, sin streaming.

Operaciones semánticas del backend:

```text
create_event     # registro
search_events    # recuperación
get_event        # lectura de un evento por su código
list_events      # listado de los eventos registrados
```

`create_event` y `search_events` sostienen el ciclo del MVP. `get_event` y `list_events` no
participan en la conversación: se ofrecen como superficie REST de inspección (§11 y §8.3).

### 8.1 create_event

Responsabilidades:

1. Recibir la representación estructurada del evento que propone la intención.
2. Validar los campos (`ClinicalEventIntentValidator`) y rechazar lo que no sea registrable.
3. Generar el UUID y el `code` legible (`evt_NNN`), sin reutilizar un código ya publicado.
4. Persistir el Markdown en el directorio de eventos, con escritura atómica.
5. Actualizar el índice de búsqueda en PostgreSQL.

El LLM interpreta el significado del mensaje; el backend decide cómo persistirlo.

### 8.2 search_events

Responsabilidades:

1. Recibir los términos y filtros que propone la intención.
2. Convertirlos en una consulta segura sobre el índice FTS (`ClinicalEventQueryRepository.toTsQuery`).
3. Aplicar filtros estructurados por tipo y por periodo cuando corresponda.
4. Recuperar los eventos relevantes, con un máximo configurable (`careme.chat.query.max-results`).
5. Distinguir la ausencia de coincidencias de la ausencia total de registros, contando el índice
   cuando hace falta.

### 8.3 get_event y list_events

Se mantienen como capacidades de lectura del backend para completar el contrato del sistema. No son
herramientas del LLM ni son necesarias para demostrar el ciclo mínimo: se exponen como
`GET /api/v1/clinical-events` y `GET /api/v1/clinical-events/{code}` para inspeccionar lo persistido
(`backend/README.md`), y se cubren con UC-005 y UC-006 como capacidades internas de recuperación.

---

## 9. Responsabilidades del LLM y del backend

La separación de responsabilidades es fundamental.

### LLM

El LLM debe:

- interpretar lenguaje natural;
- identificar hechos clínicos;
- clasificar el tipo de evento;
- extraer fecha y contenido;
- representar incertidumbre;
- decidir cuándo necesita recuperar información;
- formular una respuesta utilizando el contexto recuperado.

### Backend

El backend (servicio Spring Boot) debe:

- validar entradas;
- generar identificadores;
- persistir eventos como Markdown en `data/events/`;
- actualizar y reconciliar el índice de PostgreSQL;
- ejecutar búsquedas;
- inyectar la fecha actual en el contexto del LLM;
- leer la credencial del proveedor LLM de una variable de entorno, sin exponerla al frontend;
- controlar acceso a los datos;
- evitar que el LLM manipule directamente almacenamiento físico.

Principio arquitectónico:

> El LLM decide qué significa la información; la aplicación decide cómo se almacena.

---

## 10. Reglas de comportamiento

El MVP debe cumplir las siguientes reglas.

### 10.1 No inventar

El sistema no debe presentar como hecho información que no esté respaldada por los registros.

### 10.2 No guardar inferencias como hechos

Una frase como:

```text
Creo que podría tener hipertensión.
```

no debe convertirse automáticamente en:

```text
Diagnosis: hypertension
```

El sistema debe distinguir entre información explícitamente proporcionada y una inferencia del modelo.

### 10.3 Retrieval antes de responder

Las preguntas sobre la historia del paciente deben recuperar información antes de generar la respuesta.

Ejemplo:

```text
¿Cuándo me diagnosticaron hipertensión?
```

Debe producir un flujo equivalente a:

```text
Question
  ↓
search_events()
  ↓
Relevant events
  ↓
LLM
  ↓
Answer
```

### 10.4 No encontrar información

Si no existen registros relevantes, el sistema debe indicarlo explícitamente.

Ejemplo:

```text
No encuentro ningún registro de diabetes.
```

No debe rellenar la ausencia de información mediante conocimiento general o suposiciones.

### 10.5 Información reciente

Una pregunta como:

```text
¿Qué acabas de guardar?
```

puede responderse utilizando el contexto inmediato de la conversación cuando el dato esté inequívocamente disponible.

Esto no sustituye la recuperación para consultas históricas.

---

## 11. Interfaz de usuario

El punto de entrada del MVP es el chat del asistente, en la ruta `/` del frontend. El seguimiento de peso y circunferencia abdominal, ya implementado, pasa a una vista anexa en `/measurements`.

```text
┌─────────────────────────────┐
│            Chat             │
├─────────────────────────────┤
│ User: ...                   │
│                             │
│ Assistant: ...              │
│                             │
│ User: ...                   │
│                             │
│ Assistant: ...              │
└─────────────────────────────┘
```

La inspección de los eventos almacenados corresponde a UC-011 y **forma parte del MVP**: la ruta
`/clinical-events` lista los eventos, los filtra por tipo y por periodo y muestra el detalle de cada
uno, siempre en modo solo lectura. Es una vista de verificación de lo persistido, no un dashboard
clínico.

> **Revisión prevista en la Fase 4.** `/measurements` deja de ser la vista de una tabla propia y pasa
> a ser una vista derivada del modelo de mediciones, que el asistente podrá leer y escribir en
> lenguaje natural desde el chat.

No se construirá un dashboard clínico.

---

## 12. Arquitectura

La arquitectura mínima será:

```text
                          ┌──────────────┐
                          │     User     │
                          └──────┬───────┘
                                 │
                                 ▼
                    frontend Next.js (puerto 3000)
                     │                        │
                     │ /  (chat)              │ /measurements (anexo)
                     ▼                        ▼
        ┌────────────────────────┐   ┌──────────────────────────┐
        │  backend Spring Boot    │   │  backend Spring Boot      │
        │  (puerto 8080)         │   │  /api/v1/measurements   │
        └───────────┬────────────┘   └────────────┬─────────────┘
                    │                             │
                    ▼                             ▼
        ┌────────────────────────┐   ┌──────────────────────────┐
        │  LLM (compatible con  │   │  tabla measurements      │
        │  OpenAI, modo JSON)    │   │  (PostgreSQL, Flyway)   │
        └───────────┬────────────┘   └──────────────────────────┘
                    │ intención (events | query |
                    │ clarification | conversation)
                    ▼
        ┌────────────────────────┐
        │  backend Spring Boot    │  valida y ejecuta
        └───────────┬────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
        ▼                       ▼
  create_event            search_events
        │                       │
        ▼                       ▼
  ┌───────────┐           ┌─────────────────┐
  │  Markdown │           │     Índice      │
  │  files    │◄──────────│   full-text     │
  │ (verdad)   │           │  (PostgreSQL)   │
  └───────────┘           └─────────────────┘
```

La arquitectura se mantiene sin microservicios ni componentes distribuidos: dos servicios desplegados juntos por `docker-compose.yml` y una única base de datos, que durante el MVP aloja tanto la tabla `measurements` ya existente como el índice de eventos clínicos. La ruta `/clinical-events` del frontend es de solo lectura y consume `GET /api/v1/clinical-events`.

---

## 13. Estructura de proyecto

Una estructura inicial razonable es:

```text
careme/
├── backend/                      # Spring Boot
│   └── src/main/
│       ├── java/com/careme/backend/
│       │   ├── controller/       # chat, índice clínico y mediciones
│       │   ├── service/          # orquestación del LLM, registro, consulta, inspección, mediciones
│       │   ├── repository/       # JPA de mediciones + acceso JDBC al índice clínico
│       │   ├── entity/           # ClinicalEvent, Measurement
│       │   ├── dto/
│       │   ├── config/
│       │   └── exception/
│       └── resources/
│           ├── db/migration/     # V1__create_measurements_table.sql, V2__create_clinical_event_index.sql
│           └── prompts/          # clinical-intent-v3, clinical-answer-v3, conversation-reply-v1
├── frontend/                     # Next.js
│   └── src/
│       ├── app/                  # / (chat), /measurements (anexo), /clinical-events (inspección)
│       ├── features/chat/        # chat
│       ├── features/clinical-events/
│       ├── features/measurements/# seguimiento corporal
│       └── services/             # cliente HTTP del backend
└── data/
    └── events/                   # ficheros Markdown: fuente de verdad. Ver §6
```

No existe un paquete `assistant/`: la orquestación del LLM vive en `service/`, junto al resto de los
servicios. La estructura es orientativa y no debe convertirse en una razón para introducir
abstracciones innecesarias.

---

## 14. Exclusiones explícitas del MVP

Para proteger el alcance, quedan fuera las siguientes capacidades.

### Datos clínicos

- síntomas como entidad independiente;
- alergias;
- vacunas;
- procedimientos;
- hospitalizaciones;
- antecedentes;
- condiciones longitudinales;
- resultados de laboratorio estructurados;
- estudios diagnósticos estructurados.

> **Revisión prevista en la Fase 4.** La fase levanta buena parte de esta lista al añadir los tipos de
> evento correspondientes (§4.5: `symptom`, `procedure`, `investigation`, `hospitalization`,
> `vaccination`, `allergy` y `family_history`), las condiciones longitudinales (§4.6) y los estados
> (§4.7). Los resultados de laboratorio y los estudios estructurados quedan a la espera del modelo de
> mediciones de §4.3.

### Documentos y fuentes externas

- PDF;
- imágenes;
- OCR;
- audio;
- documentos externos;
- extracción automática desde documentos;
- integración con historias clínicas externas.

### IA avanzada

- embeddings;
- vector databases;
- semantic search;
- RAG semántico;
- agentes multi-step;
- workflows autónomos;
- resúmenes longitudinales;
- inferencia clínica;
- recomendaciones médicas;
- detección automática de enfermedades.

### Producto

- múltiples pacientes;
- cuentas;
- roles;
- médicos;
- instituciones;
- dashboards;
- aplicación móvil;
- integraciones externas.

### Integración con el seguimiento corporal

- unificar los eventos clínicos con la tabla `measurements`;
- hacer que el asistente lea o escriba el peso y la circunferencia abdominal;
- registrar el peso o la circunferencia por chat.

El seguimiento corporal mantiene su tabla, su API y su vista anexa durante el MVP; la integración se abordará en una fase posterior del roadmap. El asistente reconoce una pregunta sobre peso o circunferencia y responde con una redirección explícita, sin leer ni escribir la tabla `measurements` (§3.4).

> **Revisión prevista en la Fase 4.** Esta exclusión se levanta: el modelo de mediciones sustituye a la
> tabla `measurements` y el asistente registra y consulta el peso y la circunferencia abdominal por
> lenguaje natural, igual que la presión arterial o el colesterol.

### Infraestructura

- SQLite como índice o como almacenamiento;
- S3/Object Storage;
- Kubernetes;
- microservicios;
- colas;
- arquitectura distribuida.

PostgreSQL no es una exclusión: ya forma parte del repositorio y en el MVP se usa como índice derivado de la búsqueda, nunca como fuente de verdad de los eventos clínicos.

Estas capacidades pueden evaluarse posteriormente, pero no forman parte del criterio de éxito del MVP.

---

## 15. Casos de prueba conversacionales

El MVP debe validarse principalmente mediante escenarios de extremo a extremo.

El tipo de evento `measurement` es genérico (por ejemplo, presión arterial) y no se corresponde con el seguimiento de peso y circunferencia abdominal, que mantiene su propia tabla y su propia vista durante el MVP.

### Caso 1 — Crear diagnóstico

Entrada:

```text
Me diagnosticaron hipertensión el 10 de agosto.
```

Resultado esperado:

- se crea un `ClinicalEvent`;
- `type = diagnosis`;
- la fecha se registra con la precisión disponible (`date_precision`, `date_text`);
- el evento se persiste como Markdown en `data/events/`;
- el evento queda indexado y disponible para búsqueda.

### Caso 2 — Crear medición

Entrada:

```text
Hoy mi presión fue 145 sobre 92.
```

Resultado esperado:

- se crea un evento `measurement`;
- el contenido conserva el valor `145/92`;
- la fecha corresponde a "hoy" según la fecha que el backend inyecta en el contexto del LLM.

### Caso 3 — Consultar mediciones

Entrada:

```text
¿Qué valores de presión tengo registrados?
```

Resultado esperado:

- se ejecuta retrieval;
- se recuperan los eventos de medición relevantes;
- la respuesta utiliza esos registros.

### Caso 4 — Consulta temporal

Entrada:

```text
¿Cuándo me diagnosticaron hipertensión?
```

Resultado esperado:

- se ejecuta `search_events()`;
- se recupera el diagnóstico;
- la respuesta refleja la fecha almacenada.

### Caso 5 — Información inexistente

Entrada:

```text
¿Cuándo tuve diabetes?
```

Resultado esperado:

```text
No encuentro ningún registro de diabetes.
```

El sistema no debe inventar un diagnóstico ni una fecha.

### Caso 6 — Fecha aproximada

Entrada:

```text
Hace unos dos años me diagnosticaron hipertensión.
```

Resultado esperado:

- el evento se almacena;
- la fecha permanece aproximada;
- no se genera una fecha exacta artificial.

### Caso 7 — Pregunta general

Entrada:

```text
¿Qué es la hipertensión?
```

Resultado esperado:

- no se necesita recuperar la historia clínica;
- se puede responder como conocimiento general.

---

## 16. Definition of Done

El MVP se considera terminado cuando se cumplen todos los siguientes puntos:

```text
[x] Existe un único paciente.
[x] Se pueden registrar eventos.
[x] Los eventos se almacenan como Markdown en el directorio de eventos.
[x] El índice de búsqueda vive en PostgreSQL y puede reconstruirse desde los ficheros.
[x] El chat es el punto de entrada (/) y el seguimiento corporal vive en una vista anexa (/measurements).
[x] Existen los cuatro tipos de evento definidos.
[x] El usuario puede registrar eventos hablando normalmente con un proveedor LLM real
    (CAREME_LLM_MODE=openai); el modo fake por defecto es una heurística de desarrollo.
[x] El backend registra los eventos que la intención del LLM propone, tras validarlos.
[x] Existe búsqueda textual con el full-text de PostgreSQL y configuración simple.
[x] El backend recupera los eventos que la intención del LLM requiere, con filtros por tipo y fecha.
[x] Las preguntas sobre la historia utilizan retrieval.
[x] Las respuestas históricas se basan en los registros encontrados.
[x] Las fechas aproximadas no se convierten en fechas inventadas.
[x] La información inexistente produce una respuesta de ausencia de registro.
[x] No existen embeddings.
[x] No existe vector DB.
[x] No existe SQLite.
[x] No existen documentos externos.
[x] No existen múltiples pacientes.
```

Los dos puntos que en la redacción original describían `create_event()` y `search_events()` como
herramientas del LLM se reformularon para reflejar el diseño construido: el LLM propone una
intención y el backend ejecuta la operación (§8).

**Cierre del MVP: 2026-09-18.** Los 19 puntos están cumplidos, tal como quedaron redactados al
reconciliar este documento con el software construido. La Fase 4 revisa dos de ellos —el catálogo de
tipos de evento (§4.2) y el papel de `/measurements` (§11)— y asume los requisitos de ingeniería de
validación (§17) y el agente con herramientas.

---

## 17. Criterio técnico de éxito

El éxito del MVP no se medirá por la cantidad de funcionalidades ni por la sofisticación de la infraestructura.

La métrica principal es la fiabilidad del circuito:

```text
natural language
    →
event extraction
    →
ClinicalEvent
    →
Markdown persistence
    →
text retrieval
    →
retrieved context
    →
grounded answer
```

El sistema debe funcionar correctamente con un conjunto de hechos médicos ficticios suficientemente variado.

El objetivo es comprobar:

- que los hechos se almacenan correctamente;
- que los eventos pueden recuperarse;
- que las consultas temporales funcionan;
- que las preguntas sobre información inexistente no producen invenciones;
- que el LLM propone la intención apropiada;
- que la respuesta final está respaldada por los registros recuperados.

### Validación

El MVP se cerró con la verificación automatizada existente: 36 clases de prueba en `backend/src/test/`
y 14 archivos de prueba en `frontend/src/`.

La medición del circuito sobre un conjunto de hechos variado y el recorrido de extremo a extremo en el
navegador no forman parte de este alcance. La Fase 4 del
[`roadmap_asistente_historia_clinica.md`](./roadmap_asistente_historia_clinica.md) los recoge como
requisitos de ingeniería de la fase.

---

## 18. Principios de diseño

El MVP seguirá estos principios:

1. **Simplicidad antes que escalabilidad prematura.**
2. **Markdown como fuente de verdad.**
3. **El índice de PostgreSQL es derivado y reconstruible desde los ficheros.**
4. **El LLM propone intenciones; el backend ejecuta las operaciones. Sin acceso directo a la infraestructura.**
5. **Retrieval antes de responder preguntas históricas.**
6. **No inventar datos ni precisión temporal.**
7. **Los hechos clínicos son primarios; los resúmenes futuros deberán ser derivados y reconstruibles.**
8. **Añadir complejidad únicamente cuando un caso de uso real del producto la justifique.**

---

## 19. Evolución posterior

Este MVP constituye la base para futuras fases, pero ninguna de ellas debe incorporarse anticipadamente.

Una evolución razonable sería:

```text
MVP
 ↓
Modelo clínico enriquecido
 ↓
Documentos y evidencia
 ↓
Retrieval semántico / RAG
 ↓
Inteligencia longitudinal
 ↓
Seguridad, auditoría y producción
```

La arquitectura futura podrá evolucionar hacia:

```text
LLM Agent
    ↓
Semantic Tools
    ↓
PostgreSQL (índice FTS + vectorial)
    ↓
Object Storage + documentos
```

El MVP ya resuelve la búsqueda como full-text sobre PostgreSQL; la evolución añade la búsqueda vectorial y mueve los documentos originales al almacenamiento de objetos.

---

## 20. Resumen ejecutivo

El MVP es deliberadamente pequeño:

```text
1 paciente
4 tipos de evento
Markdown (fuente de verdad)
PostgreSQL (índice full-text derivado)
LLM (compatible con OpenAI, desde el backend, modo JSON)
Chat en /, seguimiento corporal en /measurements e inspección en /clinical-events
```

La Fase 4 revisa dos de esos elementos —el catálogo de tipos de evento y el modelo de las mediciones—
según lo anotado en §4.2 y §11.

Su única promesa esencial es:

> El usuario puede contar un hecho médico, el sistema lo guarda, y posteriormente puede preguntarle por ese hecho y obtener una respuesta basada en el registro almacenado.

Si este circuito funciona de forma fiable, el proyecto tendrá una base sólida para incorporar posteriormente mayor riqueza clínica, documentos, provenance, búsqueda semántica e inteligencia longitudinal.
