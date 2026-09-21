## Why

Hoy una medición tiene dos representaciones que no sirven para comparar valores en el tiempo: el evento
clínico `measurement`, que guarda el valor como texto libre, y la tabla `measurements`, con dos columnas
fijas —peso y circunferencia abdominal— y una fila por día. Ni la presión arterial ni el colesterol caben
como dato numérico, y el seguimiento solo puede alimentarse desde la pantalla de registro. La Fase 4.3
del roadmap introduce un **modelo de mediciones con dimensión de métrica** y hace que la persona pueda
dejar constancia de una medición contándola en la consulta, con su métrica, su valor, su unidad y su
fecha exacta, declarando de qué consulta procede.

## What Changes

- Aparece un **catálogo de métricas** extensible: cada métrica admite un tipo de valor, tiene una
  **unidad de referencia** y sus equivalencias de unidad son únicas y no dependen del criterio del
  asistente (peso en kg, circunferencia abdominal en cm, presión arterial en mmHg —compuesta, con
  sistólica y diastólica— y colesterol en mg/dL, entre otras).
- Al **cerrar la consulta** se registran, además de los hechos clínicos, las **mediciones** mencionadas
  durante la conversación: una medición por métrica y día, con su valor en la unidad de la métrica, su
  fecha exacta y la consulta de la que procede.
- **BREAKING**: la fecha de una medición es **exacta**. Una fecha aproximada o desconocida no se
  registra como exacta: el sistema pide la fecha exacta y, si no la obtiene, no registra la medición.
  Cuando la persona no indica ninguna fecha, usa el día en que se atiende el mensaje, o pide confirmación
  antes de guardar. Es un matiz de RN-005 y RN-006: los hechos clínicos admiten precisión aproximada,
  las mediciones no, porque su valor solo cobra sentido con un día concreto.
- **BREAKING**: se admite **una sola medición por métrica y día**. Volver a registrar esa métrica y ese
  día **reemplaza** el valor e informa de que quedó actualizado, en lugar de crear una segunda medición.
- La unidad no se supone: cuando la persona no la indica y no puede deducirse con seguridad del valor y
  de la métrica, el sistema pide que la concrete y no registra nada mientras siga sin estar clara. Un
  valor expresado en otra unidad de la misma métrica se **convierte** a la unidad de referencia antes de
  guardarse, sin alterarlo más allá del cambio de unidad.
- El catálogo se amplía **con la confirmación de la persona**: si menciona una métrica que el sistema
  todavía no admite, este le informa de que esa medición no está quedando registrada y le pregunta si
  quiere empezar a registrar esa métrica; solo con su confirmación la métrica pasa a admitirse.
- **BREAKING**: `measurement` **sale del catálogo de tipos de hecho clínico**, que queda en `diagnosis`,
  `medication` y `note`. Las mediciones tienen su propio seguimiento y no se convierten en hechos
  clínicos.
- **BREAKING**: un mensaje que contiene una medición se atiende por el **cauce de las mediciones**, no
  por el de los hechos clínicos; las operaciones del turno pasan a recoger también mediciones en notas
  de la consulta.
- Al cerrarse la consulta, la persona recibe una **confirmación breve** de lo registrado —mediciones y
  hechos—, distinguiendo lo que quedó registrado de lo que no.
- El **cambio migra las mediciones ya registradas** al modelo de métrica, para que el seguimiento no
  pierda lo que la persona ya tenía. La vista `/measurements` y las vías manual (`UC-002`) e importación
  (`UC-003`) siguen vigentes y pasan a alimentar y a leer el mismo seguimiento, **sin cambiar su contrato**:
  su revisión visual y su formulario por métrica los entrega el cambio hermano `metric-tracking-view`.
- **Hueco acotado y declarado:** el contrato legado exige peso y circunferencia a la vez, así que la
  pantalla antigua no muestra un día con una sola de las dos métricas —por ejemplo, un peso mencionado en
  la conversación— hasta que la revisión de la vista lo sustituya. La medición no se pierde: queda íntegra
  en el seguimiento. Cambio hermano y este se aplican antes de la próxima entrega.

## Capabilities

### New Capabilities

- `clinical-metric-catalog`: el catálogo de métricas admitidas —qué mide cada métrica, su unidad de
  referencia, las equivalencias únicas entre las unidades de una misma métrica, la métrica compuesta de
  la presión arterial y la extensión del catálogo con la confirmación de la persona.
- `clinical-measurement-registration`: el registro de una medición al cerrarse la consulta —métrica,
  valor, unidad, fecha exacta y procedencia—, la regla de una medición por métrica y día con reemplazo,
  el cauce de las mediciones frente al de los hechos clínicos, la admisibilidad previa, la atomicidad y
  la confirmación breve de lo registrado.

### Modified Capabilities

- `clinical-consultation`: el cierre de la consulta registra también las mediciones recogidas, y sus
  notas pueden recoger una medición mencionada, además de los hechos clínicos.
- `clinical-event-registration`: `measurement` deja de ser un tipo admitido de hecho clínico; registrar
  una medición ya no es registrar un evento clínico.
- `clinical-history-query`: la consulta de la propia historia clínica deja de recuperar valores de
  medición, porque las mediciones ya no son hechos clínicos y tienen su propio seguimiento.
- `llm-clinical-intent-adapter`: el contrato estructurado y el conjunto de operaciones ofrecido al
  proveedor cubren las mediciones —métrica, valor, unidad, valor compuesto— y enrutan por el cauce de
  las mediciones un mensaje que contiene una.
- `assistant-agent-turn`: las operaciones del turno recogen también las mediciones que la persona
  menciona, como notas de la consulta, sin escribir en el seguimiento durante el turno.

## Impact

- `backend/`: modelo y persistencia de mediciones con dimensión de métrica, catálogo de métricas y
  conversión de unidades, camino de cierre de la consulta, contrato de chat y contrato de mediciones,
  validación determinista del registro, migración Flyway que traslada las mediciones existentes y las
  integraciones con la tabla `measurements` de `UC-002` y `UC-003`.
- `frontend/`: la vista `/measurements` y sus servicios pasan a leer el modelo de métrica sin cambiar su
  alcance visual; la superficie del chat refleja la confirmación combinada de mediciones y hechos.
- Contrato documentado en `backend/README.md`, arquitectura en `docs/architecture.md` y modelo de datos
  en `docs/data-model.md` —cuyo §6.2 pasa de objetivo de Fase 4.3 a modelo implementado—, y
  `docs/use-cases/UC-014.md`, `UC-004.md` y `docs/use-cases/reglas-de-negocio.md` (RN-019 se matiza con
  la regla de una medición por métrica y día).
- Pruebas de backend (dominio, catálogo y conversión, persistencia con Testcontainers, contrato de chat
  y de mediciones), de frontend y de extremo a extremo.
- `UC-014b` (consultar las mediciones) y la revisión de `UC-001` quedan fuera de este cambio: esta
  propuesta registra y migra, no añade la consulta conversacional de mediciones ni las gráficas nuevas.
  La revisión de la vista y del formulario por métrica se entrega en el cambio hermano
  `metric-tracking-view`, que se aplica en la misma Fase 4.3 y antes de la próxima entrega.
