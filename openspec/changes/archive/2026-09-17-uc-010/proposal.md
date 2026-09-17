## Why

El chat ya reconoce que un mensaje no depende de la historia clínica, pero lo
cierra con una frase fija: «Puedo ayudarte a registrar hechos médicos de tu
historia clínica». Un saludo, una explicación conceptual, una pregunta general o
una petición de recomendación reciben todos esa misma frase, que no responde
nada. El resultado es que el usuario no obtiene una respuesta útil para la mitad
conversacional del producto y el límite clínico —no diagnosticar, no recomendar,
no interpretar— no se declina de forma explícita.

El vacío es además un riesgo de diseño: sin un cauce conversacional con
respuesta propia, la salida barata es dejar que el compositor fundamentado
conteste preguntas generales con conocimiento general o con hechos registrados.
Eso es exactamente lo que UC-010 prohíbe y lo que rompería la separación entre
una respuesta conversacional y una respuesta fundada en la historia.

## What Changes

- Producir una **respuesta conversacional real en español** para los mensajes que
  no dependen de la historia clínica: pregunta general, explicación de un
  concepto médico, saludo, despedida o fórmula de cortesía.
- **Declinar explícitamente** las peticiones de diagnóstico, recomendación o
  interpretación del caso, explicando que el asistente no diagnostica ni
  recomienda tratamiento, sin sustituirlas por una respuesta inventada.
- **Separar las partes** de un mensaje que mezcla un asunto general con una
  consulta sobre la historia: la parte general se responde como conversación y la
  parte que depende de la historia se atiende por el cauce que le corresponde
  (UC-007 / UC-008), sin fundirlas en una misma afirmación.
- Atender como consulta de la historia una **reformulación coloquial** de algo
  registrado: el registro del lenguaje no convierte una pregunta clínica en
  conversación general.
- **Pedir aclaración** cuando no pueda determinarse si la pregunta depende de la
  historia, en lugar de asumir una interpretación.
- Mantener el límite en todos los casos: **no se busca en la historia clínica**,
  ninguna afirmación conversacional se presenta como un hecho registrado ni se
  respalda con registros, y la historia **no se modifica**.
- Ante un fallo al elaborar la respuesta conversacional, informar de un fallo
  recuperable sin detalles internos, conservando el mensaje para reintentarlo.
- Hacer **verificable** la distinción entre `general_conversation`, `answered` y
  `no_records`, tanto en el contrato como en la presentación.

## Capabilities

### New Capabilities

Ninguna. La conversación general es un comportamiento del contrato de
conversación, del adaptador de intención, de la consulta de la historia y de la
interfaz de chat ya existentes; crear una capacidad paralela duplicaría
requisitos que hoy viven en esas cuatro especificaciones.

### Modified Capabilities

- `assistant-conversation`: el resultado `general_conversation` deja de ser un
  acuse fijo y pasa a transportar una respuesta conversacional útil; se añade la
  declinación explícita, la separación de las partes de un mensaje mixto y la
  ampliación de `clarification_required` a la duda sobre si la pregunta depende
  de la historia.
- `llm-clinical-intent-adapter`: la clasificación distingue la pregunta general
  de la que depende de la historia, conserva la parte general cuando el mensaje
  es mixto, y no responde desde el conocimiento general cuando el cauce es la
  historia.
- `clinical-history-query`: el reconocimiento de la pregunta propia acota la
  búsqueda a la parte que depende de la historia y admite la reformulación
  coloquial de un hecho registrado.
- `assistant-chat-interface`: la presentación de `general_conversation` se
  distingue de una respuesta fundada, de una ausencia y de un error, y muestra la
  declinación y las partes separadas cuando correspondan.

## Impact

- Backend: `ChatOrchestrator` deja de resolver la conversación general con un
  texto constante y pasa a componerla; se añade un compositor conversacional con
  su implementación real y su implementación `fake`, en paralelo a
  `ClinicalAnswerComposer`; `ClinicalEventIntent` incorpora la parte general de un
  mensaje mixto; `OpenAiClinicalIntentInterpreter` y `FakeClinicalIntentInterpreter`
  ajustan la clasificación; se añade una versión nueva de los prompts de intención
  y de conversación.
- Contrato HTTP: la respuesta de chat incorpora un campo opcional para la parte
  conversacional de un turno. Es un cambio aditivo sobre un contrato sin
  autenticación ni consumidores externos; los estados existentes no se renombran
  ni cambian de significado.
- Frontend: `schema.ts`, `types.ts` y `ChatWorkspace` incorporan el campo y
  presentan el turno conversacional de forma distinta a una respuesta fundada, a
  una ausencia y a un error.
- Datos: sin cambios de esquema ni migraciones. El camino conversacional no
  consulta el índice derivado, de modo que no añade lecturas.
- Documentación: `docs/use-cases/UC-010.md` y sus criterios de aceptación son la
  fuente funcional de este cambio; `docs/roadmap/use-cases.md` pasa UC-010 de
  documentado a implementado al cerrar el cambio.
- Fuera de alcance: UC-011 (inspección de los eventos almacenados) y cualquier
  diagnóstico, recomendación o interpretación clínica.
