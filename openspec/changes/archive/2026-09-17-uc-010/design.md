## Context

Ver `proposal.md` para la motivación. El contrato observable está en
`specs/assistant-conversation/spec.md`, `specs/assistant-chat-interface/spec.md`,
`specs/llm-clinical-intent-adapter/spec.md` y
`specs/clinical-history-query/spec.md`.

El estado actual que condiciona el diseño:

- `ChatOrchestrator.process()` clasifica el mensaje en `EVENTS`, `QUERY`,
  `CLARIFICATION` o `CONVERSATION`. La rama `CONVERSATION` responde con un texto
  constante —«Puedo ayudarte a registrar hechos médicos de tu historia
  clínica.»— y no compone nada: no hay respuesta conversacional que dar.
- La rama `QUERY` con alcance `measurements` también devuelve
  `GENERAL_CONVERSATION`, pero con un texto determinado que cumple un requisito
  propio de `clinical-history-query` (redirigir el peso y la circunferencia a su
  espacio). Es un caso distinto del conversacional y no depende de un modelo.
- `ClinicalAnswerComposer` ya es el patrón de composición del proyecto: interfaz
  neutral, implementación real (`OpenAiClinicalAnswerComposer`, con
  `prompts/clinical-answer-v3.txt`) e implementación `fake`
  (`FakeClinicalAnswerComposer`) seleccionada por `CAREME_LLM_MODE`, con el modo
  `fake` como valor por defecto.
- `ClinicalEventIntent` es un `record` con validación estricta en el constructor
  compacto: solo `QUERY` puede llevar `query` y solo `CLARIFICATION` puede llevar
  `clarification`. Cualquier campo nuevo debe declarar en qué forma es válido.
- `ConversationStateStore` mantiene, por conversación activa, el mensaje pendiente
  de aclaración, los resultados ya procesados por `messageId` y un búfer acotado
  de turnos recientes (`recentTurns()`), que ya se envía al clasificador.
- `ChatMessageResponse` es aditivo y sin autenticación; `absenceReason` y
  `suggestedActions` ya viajan como campos opcionales, uno por enumerado y otro
  como lista.
- El frontend valida la respuesta con Zod en `schema.ts` y etiqueta los estados
  en `ChatWorkspace.tsx`; `general_conversation` ya tiene etiqueta («Conversación»).

## Goals / Non-Goals

**Goals:**

- Dar a la conversación general una respuesta propia, compuesta, en español, sin
  que el camino conversacional toque la historia clínica.
- Hacer estructuralmente imposible que una afirmación conversacional se presente
  como un hecho registrado: el camino conversacional no recibe hechos.
- Declinar de forma explícita y verificable las peticiones de diagnóstico,
  recomendación o interpretación.
- Separar las partes de un mensaje mixto sin fundirlas en una sola afirmación.
- Mantener verificable la frontera entre `general_conversation`, `answered`,
  `no_records` y `failed`.

**Non-Goals:**

- Implementar UC-011 (inspección de los eventos almacenados).
- Separar un mensaje que **registra** un hecho y además plantea una pregunta
  general en el mismo turno. El canal de registro mantiene su contrato de
  intención única (UC-004) y este cambio no lo altera; UC-010 solo exige separar
  la parte general de la parte que **depende** de la historia, y un registro no es
  una consulta. Se documenta como límite conocido en Riesgos.
- Enseñar al compositor conversacional a responder sobre la historia del usuario:
  la pregunta clínica sigue siendo del cauce de `ClinicalHistoryQueryService`.
- Detectar por significado la intención clínica: la clasificación sigue siendo la
  del modelo con esquema estricto.
- Persistir la conversación o su memoria entre sesiones.
- Cambios de esquema de base de datos o migraciones Flyway.
- Añadir autenticación.

## Decisions

### Un compositor conversacional propio, en paralelo al compositor fundamentado

Se añade `ClinicalConversationComposer` (interfaz neutral), su implementación real
`OpenAiClinicalConversationComposer` con `prompts/conversation-reply-v1.txt`, y
`FakeClinicalConversationComposer` para el modo `fake`, seleccionados por
`CAREME_LLM_MODE` igual que el compositor de respuestas. El compositor recibe
únicamente el mensaje y los turnos recientes de la conversación activa, y devuelve
el texto en español de la réplica.

**Por qué:** la alternativa barata era ampliar la salida del clasificador con un
campo de texto y pedir al mismo prompt que clasifique y redacte. Se descarta
porque mezcla dos responsabilidades con requisitos opuestos: el clasificador debe
ser determinista y no responder (`temperature 0`, esquema cerrado y la regla
explícita «you must not answer it»), mientras que la réplica es redacción libre
sujeta a los límites clínicos. Además, el compositor de respuestas ya estableció
el patrón —interfaz, implementación real, implementación `fake`, prompt
versionado—, de modo que un compositor paralelo no introduce arquitectura nueva.

**Consecuencia asumida:** un turno conversacional pasa a costar una llamada al
proveedor. Hoy esa rama no llama al proveedor porque no responde nada; la llamada
es justamente la funcionalidad.

**Alternativa descartada:** responder con plantillas deterministas por categoría
(saludo, concepto, cortesía). No cubre A1 (explicar un concepto general) ni A6
(pregunta general apoyada en lo conversado) sin un catálogo abierto de casos, que
es peor que un prompt acotado.

### El prompt conversacional lleva sus límites, no una instrucción de buscar

`prompts/conversation-reply-v1.txt` recibe el mensaje y los turnos recientes, y
declara: responde en español como conversación; explica conceptos generales sin
aplicarlos al caso del usuario, sin interpretar sus datos y sin recomendar
tratamiento; si el usuario pide diagnóstico, recomendación o interpretación,
declina y explica que no diagnostica ni recomienda; no afirma nada sobre la
historia clínica del usuario ni lo presenta como registrado; no inventa datos
clínicos.

**Por qué:** los límites clínicos son la parte contractual de esta réplica y deben
vivir donde se redacta. El clasificador no los aplica porque no redacta.

**Alternativa descartada:** validar la réplica después de generarla con un filtro
de contenido clínico. Un filtro sobre texto libre es frágil y no sustituye a la
instrucción; el modo `fake` además nunca pasaría por él.

### El clasificador distingue el cauce y conserva la parte general

`clinical-intent-v3.txt` (versión nueva, el adaptador real apunta a ella) añade a
las reglas actuales:

- clasificar como `query` todo mensaje en el que **alguna** parte dependa de la
  historia clínica, incluida una reformulación coloquial de algo registrado;
- cuando el mensaje contenga además una parte que no depende de la historia,
  devolver esa parte en un campo nuevo `general_part` de la consulta, en las
  palabras del usuario y fuera de `search_terms`;
- usar `clarification` también cuando no pueda determinarse si el mensaje depende
  de la historia clínica;
- mantener `conversation` solo para lo que ni se registra ni depende de la
  historia.

**Por qué:** la separación de A2 no puede resolverse en el servicio, que no lee el
mensaje: el cauce lo decide la clasificación. Y el lenguaje coloquial (A3) es una
regla de clasificación, no de recuperación.

**Alternativa descartada:** un quinto `Kind` (`MIXED`) en `ClinicalEventIntent`.
Obliga a revisar todos los `switch` y a duplicar la validación de `query` y
`general_part` por forma, y el cauce primario seguiría siendo la búsqueda.

### La parte general viaja en la consulta, no en el intent en general

`ClinicalEventIntent.Query` gana un campo opcional `generalPart`. El constructor
compacto de `ClinicalEventIntent` exige que solo `QUERY` pueda llevar una consulta,
así que colgar `generalPart` de `Query` mantiene esa invariante y hace
estructuralmente imposible que la rama de registro transporte texto conversacional
o que la rama conversacional transporte criterios de búsqueda.

**Por qué:** el límite de Non-Goals —un mensaje que registra y pregunta a la vez no
se separa— queda expresado en el tipo, no solo en la prosa.

**Alternativa descartada:** `generalPart` en la raíz del intent, válido en
cualquier forma. Abriría la rama de registro a llevar réplica conversacional, que
es alcance de UC-004 y no de este cambio.

### El servicio compone la parte general y el resultado de la historia por separado

En `ChatOrchestrator`:

| Forma del intent | Comportamiento |
| ---------------- | -------------- |
| `CONVERSATION` | Se invoca el compositor conversacional con el mensaje y los turnos recientes; el resultado es `general_conversation` con la réplica en `message`. No se invoca `ClinicalHistoryQueryService`. |
| `QUERY` con `generalPart` | Se invoca el compositor conversacional con la parte general y, por separado, `ClinicalHistoryQueryService.answer()`. El estado es el de la historia (`answered`, `no_records` o `failed`) y la réplica general viaja en `generalReply`. |
| `QUERY` sin `generalPart` | Sin cambios. |
| `QUERY` con alcance `measurements` | Sin cambios en su mensaje determinado: la redirección al espacio de seguimiento no pasa por el compositor. Si el mensaje traía además una parte general, esa parte sí se compone y viaja en `generalReply`, para no descartar lo que el usuario escribió. |

**Por qué:** el estado del turno debe seguir describiendo el resultado clínico —una
ausencia declarada es una ausencia, no una conversación—, y la réplica general
necesita un sitio propio para que la interfaz no la pinte como respuesta fundada.
Es el mismo criterio con el que UC-008 añadió `absenceReason` y `suggestedActions`
en vez de enriquecer la prosa.

**Alternativa descartada:** fusionar ambas partes en el campo `message`. Impide que
la interfaz distinga las dos clases de respuesta y arrastra la réplica general al
estilo de una respuesta fundada.

**Alternativa descartada:** responder la parte general con un estado propio
(`general_conversation`) y relegar la historia a un campo secundario. Un turno
`no_records` presentado como conversación incumple el requisito de que la ausencia
se distinga de una respuesta conversacional.

### La declinación es conversación, no un estado nuevo

A5 se resuelve dentro de `general_conversation`: la réplica declina y explica que el
asistente no diagnostica ni recomienda tratamiento.

**Por qué:** el contrato enumerado de resultados no cambia y la declinación sigue
siendo una respuesta que no procede de la historia. El requisito observable —una
negativa explícita y comprensible— se cumple con el contenido.

**Alternativa descartada:** un estado `declined`. Añade un valor al enumerado, al
esquema del cliente y a los `switch`, sin cambiar lo que el usuario percibe.

### El camino conversacional no puede leer la historia por construcción

El compositor conversacional no recibe hechos, no recibe referencias y no tiene
acceso a `ClinicalHistoryQueryService` ni al repositorio de consulta. La rama
`CONVERSATION` de `ChatOrchestrator` no invoca el servicio de consulta.

**Por qué:** el requisito «no se busca en la historia» se verifica mejor por
ausencia de camino que por inspección del texto generado. Una prueba con doble de
`ClinicalHistoryQueryService` puede exigir cero interacciones en esa rama, y una
prueba de integración puede comprobar que el número de eventos y el índice no
cambian.

### El fallo de composición es el fallo del turno

Una `LlmIntegrationException` del compositor conversacional se mapea al mismo
resultado `failed` que ya usa el clasificador, con mensaje reintentable, y el
mensaje original se conserva. En un mensaje mixto, si falla la composición de la
parte general, el turno sale con el estado de la historia y sin `generalReply`:
la parte clínica, que ya se resolvió, no se pierde por un fallo de la parte
conversacional. La parte general no se reintenta por separado.

**Por qué:** el requisito de E1 es un fallo recuperable y sin detalles internos;
presentar como fallido un turno cuya consulta clínica sí se completó sería una
pérdida de información mayor que la ausencia de la réplica general.

**Riesgo asumido:** en un mensaje mixto, la ausencia de `generalReply` no distingue
«no había parte general» de «la parte general falló». Se acepta: el estado clínico
sigue siendo correcto y reintentar el mensaje vuelve a intentar ambas partes.

### La aclaración por cauce indeterminado reutiliza el estado pendiente existente

`clarification_required` ya guarda el mensaje como pendiente y el siguiente turno
se interpreta junto con él. La aclaración por cauce indeterminado usa el mismo
camino, sin estado nuevo.

**Por qué:** el turno de aclaración del usuario es exactamente el caso que el
estado pendiente ya resuelve —un mensaje incompleto que se completa con el
siguiente—, y no requiere distinguir el motivo de la aclaración para funcionar.

**Alternativa descartada:** guardar el motivo de la aclaración para tratarlo
distinto al recibir la respuesta. Duplicaría estado efímero sin cambiar lo
observable.

### El contrato HTTP crece con un campo opcional

`ChatMessageResponse` gana `generalReply` (`String`, nulo cuando el turno no tiene
parte general). No se renombra ningún estado ni se cambia el significado de
`answered`, `no_records`, `general_conversation` o `failed`.

**Por qué:** un cliente que no conozca el campo sigue funcionando; el cambio no
requiere coordinación de despliegue.

**Alternativa descartada:** un objeto anidado `conversation`. Aporta jerarquía que
no se usa y complica el esquema de validación sin ganancia observable.

### La interfaz trata la réplica general como un tipo de respuesta propio

`ChatWorkspace` presenta un turno con `generalReply` como dos bloques: la
conversación y el resultado clínico. El esquema Zod añade el campo como opcional y
nulo, y los turnos `general_conversation` no muestran hechos ni motivo de ausencia.

**Por qué:** la separación estricta es un requisito no funcional de UC-010 y se
resuelve en la presentación, no en el texto.

## Risks / Trade-offs

- **Mensaje que registra un hecho y además pregunta algo general.** El clasificador
  elige una sola forma y la parte general podría perderse. → Es el comportamiento
  actual (hoy también elige una sola forma) y UC-010 no lo exige; se declara como
  Non-Goal para que la decisión sea visible y no un olvido.
- **La réplica general puede afirmar algo clínico sobre el usuario.** → El prompt la
  instruye a no afirmar nada sobre su historia, el compositor no recibe hechos y se
  prueba en modo real que una pregunta clínica no se responde por este cauce.
- **Coste y latencia de una llamada adicional por turno conversacional.** → Solo
  aplica a los turnos que hoy no responden nada; el camino de consulta y el de
  registro no cambian su coste.
- **Falso negativo de clasificación: una pregunta clínica tratada como conversación
  general.** Es la dirección peligrosa, porque fabricaría una respuesta. → El
  prompt ordena clasificar como `query` toda parte que dependa de la historia,
  incluida la coloquial; se añade prueba dedicada en modo real (A3) y una prueba de
  integración que exige cero lecturas de la historia en el cauce conversacional.
- **Falso positivo: una pregunta general tratada como consulta**, que produce una
  ausencia innecesaria. → Es el comportamiento actual y menos grave que el anterior
  (una ausencia explícita y accionable en lugar de una respuesta inventada).
- **Pérdida de la réplica general en un mensaje mixto si falla la composición.** →
  El estado clínico del turno se conserva y el usuario puede reintentar; se prueba
  que el fallo parcial no altera el resultado de la historia ni la historia misma.
- **Transcripción de la declinación a otros idiomas.** → La coherencia de idioma se
  resuelve en el prompt; las pruebas afirman sobre el modo `fake` y sobre el
  comportamiento del contrato, no sobre textos de un proveedor concreto.

## Migration Plan

No hay migración de datos ni de esquema: el cambio añade un camino de composición y
un campo opcional del contrato, y no escribe en la historia clínica.

1. Backend: compositor conversacional (interfaz, real y `fake`), prompt
   `conversation-reply-v1.txt` y rama `CONVERSATION` del orquestador.
2. Backend: `generalPart` en la consulta, `general_part` en el prompt de intención
   (versión nueva `clinical-intent-v3.txt`) y composición separada en el orquestador.
3. Contrato: campo opcional `generalReply` y su documentación en `backend/README.md`.
4. Frontend: esquema Zod, tipos y presentación de las dos partes.
5. Documentación: cerrar UC-010 en `docs/roadmap/use-cases.md` al archivar.

Reversión: revertir el cambio deja la historia clínica intacta, porque ninguna parte
de este cambio escribe en ella; el campo opcional desaparece del contrato sin
afectar a los turnos de registro ni de consulta.
