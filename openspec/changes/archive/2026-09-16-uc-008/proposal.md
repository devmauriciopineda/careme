## Why

UC-007 ya devuelve `no_records` cuando la búsqueda no encuentra hechos, pero el
contrato observable se agota en un mensaje fijo. La persona no puede distinguir
si su historia está vacía, si nunca registró ese tipo de hecho o si simplemente
usó otras palabras, y no recibe ninguna salida accionable. Esta ambigüedad es
justo el punto donde un asistente clínico pierde credibilidad: una negativa
opaca se confunde con un error y empuja al usuario a dejar de confiar en lo que
sí está registrado.

## What Changes

- Ampliar la declaración de ausencia para cubrir los cuatro motivos que hoy
  producen el mismo mensaje:
  - la historia no contiene ningún hecho en absoluto;
  - no contiene hechos del tipo consultado;
  - no contiene hechos en el periodo consultado;
  - contiene hechos, pero ninguno coincide con los términos de la pregunta.
- Acotar la declaración al alcance real de la búsqueda: la ausencia en un periodo
  se declara como ausencia **en ese periodo**, no en toda la historia.
- Responder la parte respaldada y declarar explícitamente la parte que no tiene
  registros cuando una misma pregunta mezcla ambas.
- Mantener la declaración ante la insistencia del usuario, sin fabricar
  información para complacerlo.
- Ofrecer siempre una salida accionable: reformular la pregunta o registrar el
  hecho que no consta.
- Distinguir de forma verificable la ausencia de registros de un fallo de
  búsqueda, tanto en la respuesta como en su presentación.
- Aclarar en el mensaje que la ausencia se refiere a lo que consta en la
  historia y no a lo que haya ocurrido.

## Capabilities

### New Capabilities

Ninguna. La ausencia de registros es comportamiento de la consulta de la
historia y del contrato de conversación ya existentes; crear una capacidad
paralela duplicaría requisitos que hoy viven en `clinical-history-query`.

### Modified Capabilities

- `clinical-history-query`: el requisito de declarar la ausencia de registros
  pasa de un mensaje único a una declaración con motivo, alcance y salida
  accionable; se añade la respuesta parcial y la distinción frente al fallo.
- `assistant-conversation`: el resultado `no_records` debe transportar el motivo
  de la ausencia y la salida ofrecida, para que el cliente pueda presentarlos sin
  reinterpretar el texto.
- `assistant-chat-interface`: la presentación de `no_records` debe mostrar el
  motivo, distinguirse de una respuesta fundamentada y de un error, y ofrecer la
  acción de continuar.
- `llm-clinical-intent-adapter`: la composición fundamentada debe informar de la
  parte de la pregunta que no pudo respaldar, en lugar de resolverla en silencio.

## Impact

- Backend: `ClinicalHistoryQueryService` deja de resolver la ausencia con un
  mensaje constante y pasa a determinar el motivo; `ClinicalAnswerResult` gana la
  información necesaria para describirlo; `ClinicalAnswerComposer` y su
  implementación `fake` deben informar de las partes no respaldadas.
- Contrato HTTP: el cuerpo de la respuesta de chat incorpora los datos de la
  ausencia para el resultado `no_records`. Es un cambio aditivo sobre un contrato
  sin autenticación ni consumidores externos; los estados existentes no se
  renombran.
- Frontend: `ChatWorkspace` y el esquema de validación de la respuesta de chat
  incorporan los nuevos campos y la acción de continuar.
- Datos: sin cambios de esquema. La determinación del motivo requiere poder
  distinguir una historia vacía de una búsqueda sin coincidencias; se resuelve
  con consultas de lectura sobre el índice existente.
- Documentación: `docs/use-cases/UC-008.md` y sus criterios de aceptación son la
  fuente funcional de este cambio; `docs/roadmap/use-cases.md` pasa UC-008 de
  documentado a implementado al cerrar el cambio.
- Fuera de alcance: UC-010 (conversación general que no consulta la historia) y
  UC-011 (inspección de los eventos almacenados).
