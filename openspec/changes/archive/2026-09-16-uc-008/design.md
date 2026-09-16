## Context

Ver `proposal.md` para la motivación. El contrato observable está en
`specs/clinical-history-query/spec.md`, `specs/assistant-conversation/spec.md`,
`specs/assistant-chat-interface/spec.md` y
`specs/llm-clinical-intent-adapter/spec.md`.

El estado actual que condiciona el diseño:

- `ClinicalHistoryQueryService.answer()` ya resuelve la consulta: valida la
  intención, busca en el índice y compone la respuesta. Cuando la búsqueda no
  devuelve nada, construye un `NO_RECORDS` con un **mensaje constante**, sin
  motivo ni salida ofrecida.
- `ClinicalEventQueryRepository.search()` busca **primero por texto** y, solo si
  el texto no devuelve nada y existe un filtro de metadatos, busca **solo por
  metadatos**. Ambas consultas aplican `LIMIT` (`careme.chat.query.max-results`,
  por defecto 10), por lo que su resultado **no puede probar una ausencia**.
- `ClinicalAnswerComposer.ComposedAnswer` transporta `text` y `references`. No
  tiene forma de informar de una parte de la pregunta que no pudo respaldar.
- `ChatMessageResponse` es un contrato aditivo y sin autenticación; ya expone
  `answered` y `no_records` como estados distintos, y `failed` para el fallo.
- `CAREME_LLM_MODE` por defecto es `fake`; `FakeClinicalAnswerComposer` permite
  probar la composición sin red.

## Goals / Non-Goals

**Goals:**

- Determinar el **motivo** de la ausencia de forma determinista y exponerlo como
  un valor estable, no como texto que el cliente tenga que interpretar.
- Aceptar que una ausencia puede deberse a un tipo o a un periodo y **acotar la
  declaración** a ese alcance.
- Informar de la parte de la pregunta que no quedó respaldada cuando la respuesta
  es parcial.
- Garantizar que la ausencia nunca se comunica como fallo, ni el fallo como
  ausencia.
- Mantener intacto el camino feliz: sin búsquedas ni consultas adicionales
  cuando la pregunta sí tiene respuesta.

**Non-Goals:**

- Responder preguntas que no dependen de la historia clínica (UC-010).
- Implementar UC-011 (inspección de los eventos almacenados).
- Detectar ausencia por significado cuando las palabras del usuario no coinciden
  con las del registro: se declara la falta de coincidencia, no se corrigen los
  términos.
- Registrar el hecho que falta desde este flujo; el registro sigue siendo el de
  UC-004 y solo ocurre si la persona lo expresa.
- Añadir estado de conversación para recordar negativas anteriores.
- Cambios de esquema de base de datos o migraciones Flyway.

## Decisions

### La razón se determina con una escalera de conteos, no con el resultado de la búsqueda

Se añaden al repositorio de lectura conteos sobre `clinical_event_index`:

1. total de eventos registrados;
2. eventos del tipo consultado, si la intención trae filtro de tipo;
3. eventos del periodo consultado, si la intención trae filtro de fechas.

El servicio recorre la escalera solo cuando la búsqueda no devolvió hechos
o cuando el compositor informa que los hechos recuperados no responden a la
pregunta:

| Condición (en orden)                                | Motivo             |
| --------------------------------------------------- | ------------------ |
| total de eventos = 0                                | `EMPTY_HISTORY`    |
| filtro de tipo presente y eventos del tipo = 0      | `NO_EVENTS_OF_TYPE`|
| filtro de periodo presente y eventos del periodo = 0| `NO_EVENTS_IN_PERIOD` |
| en cualquier otro caso                              | `NO_TERM_MATCH`    |

**Por qué:** `search()` aplica `LIMIT` y alterna entre búsqueda por texto y por
metadatos, así que un resultado vacío no distingue "no hay nada registrado" de
"no hay nada que coincida". Un conteo sí lo prueba, y la escalera evalúa primero
el alcance más amplio para no atribuir a un tipo o a un periodo una ausencia que
en realidad es de toda la historia.

**Alternativa descartada:** derivar el motivo de las mismas filas que devuelve
`search()`. No permite distinguir los cuatro casos y hace depender el motivo del
límite de resultados.

### El motivo y la salida viajan como valores enumerados, no como prosa

`ClinicalAnswerResult` incorpora un `AbsenceReason` (opcional; solo presente en
`NO_RECORDS`) y la lista de acciones sugeridas (`reformulate`, `register`). El
mensaje en español sigue existiendo y describe el motivo con palabras, pero el
cliente no lo analiza para decidir qué mostrar.

**Por qué:** la especificación de `assistant-conversation` exige que el cliente
pueda presentar el motivo y la acción sin reinterpretar el texto en español. Un
enumerado es estable, verificable y no se rompe al reescribir un mensaje.

**Alternativa descartada:** enriquecer solo el mensaje en español. Obligaría al
frontend a inferir el estado desde la prosa y a los tests a afirmar sobre textos.

### El contrato HTTP crece de forma aditiva

`ChatMessageResponse` gana dos campos opcionales: `absenceReason` (enumerado o
nulo) y `suggestedActions` (lista, vacía cuando no aplica). No se renombra ningún
estado existente ni se cambia el significado de `answered`, `no_records` o
`failed`.

**Por qué:** un cliente que no conozca los campos nuevos sigue funcionando; el
cambio no requiere coordinación de despliegue entre backend y frontend.

**Alternativa descartada:** un objeto anidado `absence`. Aporta jerarquía que no
se usa y complica el esquema de validación del cliente sin ganancia observable.

### El compositor declara cuánto de la pregunta queda respaldado

`ComposedAnswer` incorpora una cobertura explícita —`FULL`, `PARTIAL` o `NONE`— y,
cuando no es completa, el texto de la parte que no pudo respaldarse. El servicio
decide con ella:

| Cobertura | Resultado |
| --------- | --------- |
| `FULL`    | `answered` con el texto y las referencias del compositor |
| `PARTIAL` | `answered` con el texto del compositor más la frase que declara la parte sin registros |
| `NONE`    | `no_records` con el motivo que resuelve la escalera; se descarta el texto del compositor y no se adjunta ningún hecho |

**Por qué:** solo el compositor, que redacta la respuesta y conoce el conjunto
recuperado, puede juzgar si esos hechos responden a la pregunta. La búsqueda es
léxica y acotada; no puede decidir respaldo.

**Corrección de una premisa del diseño inicial:** se partía de que «ausencia =
búsqueda vacía». La comprobación con el modelo real mostró que es falso. El modelo
elige los términos de búsqueda y la recuperación puede devolver hechos que no
responden a la pregunta; en ese caso el turno se presentaba como `answered` con un
hecho irrelevante como apoyo y sin declarar ausencia, incumpliendo el requisito de
declararla. Por eso la escalera de motivos se recorre no solo cuando la búsqueda
vuelve vacía, sino también cuando el compositor informa `NONE`.

**Descartar el texto del compositor en `NONE`:** su negativa podría describir los
hechos recuperados o insinuar una respuesta. La declaración de ausencia es del
servicio, y así el turno no puede presentar ningún hecho.

**Alternativa descartada:** deducir la cobertura en el servicio comparando las
referencias con la pregunta. El servicio no puede juzgar cobertura semántica y ya
tiene la responsabilidad de no inventar.

**Riesgo de la decisión:** el compositor puede informar menos cobertura de la que
realmente tiene y convertir en ausencia una pregunta que sí tenía respuesta.
Mitigación: `NONE` solo puede retirar una respuesta, nunca añadir contenido; la
declaración de ausencia no incluye hechos, de modo que el peor caso es una
negativa explícita y accionable en lugar de una respuesta inventada; y se cubre
con pruebas dedicadas en modo real y en modo `fake`.

### El fallo y la ausencia son excluyentes por construcción

El servicio ya devuelve `FAILURE` ante una excepción o un rechazo de validación.
El diseño lo hace explícito: `NO_RECORDS` solo se construye tras una búsqueda
completada con éxito, y `failure()` nunca se convierte en una declaración de
ausencia. Se añade una prueba de regresión para cada dirección.

**Por qué:** una ausencia no verificada es una afirmación clínica falsa; el
usuario debe poder reintentar en lugar de creer que su historia está vacía.

### La insistencia no necesita estado

Repetir la pregunta vuelve a recorrer la misma escalera y produce el mismo
motivo. No se añade memoria de negativas al estado de conversación.

**Por qué:** el determinismo de la escalera ya satisface el requisito y añadir
estado introduciría comportamiento dependiente del orden de los turnos.

**Alternativa descartada:** recordar en la conversación que ya se declaró una
ausencia. Duplicaría el estado efímero existente sin cambiar lo observable.

### El modo `fake` cubre también la respuesta parcial

`FakeClinicalAnswerComposer` se extiende para poder producir una respuesta
parcial además de una completa o vacía.

**Por qué:** es el modo por defecto y el único que no llama a la red; sin él, la
respuesta parcial solo sería verificable con un proveedor real.

## Risks / Trade-offs

- **Index desactualizado produce una ausencia falsa.** La escalera lee el índice
  derivado, igual que la consulta actual. → El índice es reconstruible y se
  reconcilia al arranque; la declaración de historia vacía se apoya en el mismo
  índice que responde las preguntas, de modo que nunca contradice una respuesta
  que el usuario haya podido recibir.
- **Declaración de parcialidad en exceso.** → La declaración no puede añadir
  hechos y se valida contra el conjunto recuperado; se prueba en ambos modos.
- **Consultas de conteo adicionales.** → Solo se ejecutan cuando la búsqueda no
  devolvió nada, de modo que el camino feliz no cambia su coste; la escalera
  corta en la primera condición que se cumple.
- **Motivo acotado a un periodo inferido y no expresado por el usuario.** →
  `NO_EVENTS_IN_PERIOD` solo se declara cuando la intención trae filtro de fechas;
  en cualquier otro caso la escalera cae en `NO_TERM_MATCH`, que no acota el
  alcance.
- **Cambio de contrato en un cliente que no lo conoce.** → Los campos son
  opcionales y `no_records` conserva su significado; el cliente antiguo sigue
  mostrando el mensaje en español.
- **Ampliación del alcance hacia UC-010.** La tentación de "responder igualmente"
  cuando no hay registros cruza la frontera. → La declaración de ausencia no
  añade conocimiento general; el caso de la pregunta general se mantiene en
  UC-010 y la especificación de `clinical-history-query` ya lo separa.

## Migration Plan

No hay migración de datos ni de esquema: el cambio es aditivo sobre el contrato y
solo añade consultas de lectura.

1. Backend: motivo, escalera de conteos y declaración de parcialidad, con las
   pruebas de la escalera, la exclusión entre ausencia y fallo, y la respuesta
   parcial.
2. Contrato: añadir los campos opcionales a la respuesta de chat y documentarlos
   en `backend/README.md`.
3. Frontend: extender el esquema de validación y presentar el motivo y la salida
   ofrecida en `no_records`, distinguiéndolo de una respuesta y de un error.
4. Documentación: cerrar UC-008 en `docs/roadmap/use-cases.md` al archivar.

Reversión: revertir el cambio no altera datos persistidos, porque ninguna parte
de este cambio escribe en la historia clínica.
