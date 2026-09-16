## 1. Determinar el motivo de la ausencia

- [x] 1.1 Añadir al repositorio de lectura de la historia los conteos sobre el índice derivado: total de eventos registrados, eventos de un tipo y eventos de un periodo; verificar con pruebas de integración que un índice vacío devuelve cero y que un evento publicado se cuenta en las tres formas.
- [x] 1.2 Incorporar `AbsenceReason` al resultado de la consulta con los cuatro valores (`EMPTY_HISTORY`, `NO_EVENTS_OF_TYPE`, `NO_EVENTS_IN_PERIOD`, `NO_TERM_MATCH`); verificar con pruebas unitarias que el valor solo se construye desde su condición y que está ausente en `ANSWERED` y en `FAILURE`.
- [x] 1.3 Implementar la escalera de motivos en el servicio de consulta, evaluando primero el alcance más amplio; verificar con pruebas unitarias los cuatro casos y su orden de precedencia, incluido el caso de tipo y periodo presentes que cae en `NO_EVENTS_OF_TYPE`.

## 2. Declaración y salida accionable

- [x] 2.1 Redactar el mensaje en español de cada motivo, de modo que describa el alcance real (historia completa, tipo o periodo) y no afirme que el hecho no ocurrió; verificar con pruebas unitarias que cada motivo produce su mensaje y que ninguno afirma la no ocurrencia.
- [x] 2.2 Añadir las acciones sugeridas (`reformulate`, `register`) a la declaración de ausencia; verificar con pruebas unitarias que toda ausencia las incluye y que ninguna declaración crea ni modifica un evento.
- [x] 2.3 Verificar con una prueba que el motivo `EMPTY_HISTORY` se presenta como estado inicial y no como fallo, y que su mensaje no menciona ningún error.

## 3. Exclusión entre ausencia y fallo

- [x] 3.1 Asegurar que `NO_RECORDS` solo se construye después de una búsqueda completada con éxito; verificar con una prueba que un fallo del repositorio devuelve `FAILURE` y que la respuesta no declara ninguna ausencia, según UC-008 `E1` de `docs/use-cases/UC-008-acceptance-criteria.md`.
- [x] 3.2 Verificar la dirección inversa con una prueba: una búsqueda completada sin coincidencias produce la declaración de ausencia y no un fallo.
- [x] 3.3 Verificar con una prueba que repetir la misma pregunta sin registros produce el mismo motivo y la misma declaración, y que no se añade estado de conversación para recordar negativas (UC-008 `A6`).

## 4. Respuesta parcial

- [x] 4.1 Extender `ComposedAnswer` con la parte de la pregunta que no pudo respaldarse; verificar con pruebas unitarias que el campo es opcional y que su ausencia significa que la pregunta quedó cubierta por completo.
- [x] 4.2 Componer el mensaje de respuesta parcial en el servicio: responder la parte respaldada con sus referencias y declarar la parte sin registros; verificar con una prueba que la declaración no introduce hechos ni referencias fuera del conjunto recuperado (UC-008 `A5`).
- [x] 4.3 Extender el compositor `fake` para producir una respuesta parcial; verificar con una prueba en modo `fake` que la respuesta parcial se compone y se declara sin acceso a la red.

## 5. Contrato HTTP

- [x] 5.1 Añadir los campos opcionales `absenceReason` y `suggestedActions` a la respuesta de chat; verificar con MockMvc que un turno `no_records` incluye ambos, y que `answered` y `failed` los omiten o los devuelven vacíos sin cambiar su estado.
- [x] 5.2 Documentar los campos en `backend/README.md` y verificar que el contrato documentado coincide con la respuesta observada en las pruebas de MockMvc.

## 6. Frontend

- [x] 6.1 Extender el esquema de validación del chat con los campos opcionales; verificar con pruebas unitarias que acepta respuestas con los campos presentes, con ellos ausentes y con valores no admitidos rechazados.
- [x] 6.2 Presentar el motivo y la salida ofrecida en un turno `no_records`; verificar con pruebas de componente que el turno se distingue de una respuesta fundamentada y de un error, y que muestra el motivo informado.
- [x] 6.3 Verificar con pruebas de componente que el nuevo contenido tiene nombre accesible y no expone detalles internos ni el texto del proveedor.

## 7. Verificación integrada

- [x] 7.1 Añadir una prueba de integración de extremo a extremo que recorra los cuatro motivos (historia vacía, tipo sin eventos, periodo sin eventos, términos sin coincidencia) y verificar que en todos ellos la historia clínica y el índice permanecen sin cambios.
- [ ] 7.2 Ejecutar `.\mvnw.cmd verify` desde `backend/` y verificar que todas las pruebas pasan y que se supera la puerta de cobertura de JaCoCo.
  - Diferido por decisión del 2026-09-16: la cobertura se resolverá en otro change. Las 174 pruebas pasan, pero la puerta no se cumple. Medido en un worktree limpio en `HEAD` `0cea4a6`, sin este cambio: branches 0.685 y lines 0.892, ambas por debajo del 0.90 exigido, de modo que el incumplimiento es previo. Con `uc-008`: branches 0.699 (sigue por debajo) y lines 0.912 (ahora cumple). Las ramas sin cubrir están en clases ajenas a este cambio: `FakeClinicalIntentInterpreter` (40), `OpenAiClinicalIntentInterpreter` (27), `ClinicalEventIntentValidator` (16) y `ClinicalEventMarkdownStore` (12).
- [x] 7.3 Ejecutar `pnpm test`, `pnpm lint` y `pnpm typecheck` desde `frontend/` y verificar que todos pasan.
- [x] 7.4 Comprobar el funcionamiento del asistente con el modelo de lenguaje configurado (no en modo `fake`): sobre una historia con eventos, verificar que una pregunta sin coincidencia de términos declara la ausencia con su motivo y su salida, que una pregunta de un tipo no registrado acota la ausencia a ese tipo, que una pregunta parcialmente respaldada responde solo la parte respaldada y que una pregunta de carácter general no activa búsqueda ni declara ausencia.
  - **Primera pasada, 2026-09-16, antes de la ampliación:** historia vacía `no_records`/`empty_history`; registro `registered`; tipo no registrado `no_records`/`no_events_of_type`; pregunta respaldada `answered` con el hecho y su fecha; pregunta parcial `answered` con la declaración; pregunta general `general_conversation` sin búsqueda. **No cumplió** el caso de términos: "¿He tenido migrañas?" sobre una historia no vacía devolvió `answered` con un hecho que no respondía a la pregunta. Origen del grupo 8.
  - **Segunda pasada, 2026-09-16, tras la ampliación**, con `CAREME_LLM_MODE=openai`, `deepseek-flash` y PostgreSQL 17 real. Historia vacía: `no_records`/`empty_history` con `reformulate,register`. Registro "Ayer me diagnosticaron hipertensión": `registered` con 1 evento. **Términos que no coinciden: `no_records`/`no_term_match`, sin eventos y con ambas acciones**; el registro del servicio confirma la rama nueva ("The retrieved events do not support the clinical history question"). Tipo no registrado: `no_records`/`no_events_of_type`. Pregunta respaldada: `answered` con el hecho y la fecha. Pregunta parcial: `answered` con la parte respaldada y la declaración, con un solo punto final. Pregunta de carácter general: `general_conversation` con 0 eventos y sin declarar ausencia. Repetición de la ausencia: mismo motivo y misma declaración. Los ocho casos cumplen.
- [x] 7.5 Ejecutar `openspec validate "uc-008" --strict` y verificar que el cambio sigue siendo válido tras la implementación.
- [x] 7.6 Actualizar `docs/roadmap/use-cases.md` para reflejar UC-008 como implementado y verificar que el control de avance y los enlaces al documento y a los criterios de aceptación coinciden.

## 8. Ampliación: la ausencia cuando la búsqueda sí devuelve hechos

Origen: la comprobación con el modelo real (tarea 7.4) mostró que una búsqueda
que devuelve hechos irrelevantes producía `answered` con un hecho no relacionado
como apoyo, en lugar de declarar la ausencia.

- [x] 8.1 Extender `ComposedAnswer` con la cobertura de la pregunta (`FULL`, `PARTIAL`, `NONE`); verificar con pruebas unitarias que el constructor corto equivale a `FULL` y que una parte no respaldada equivale a `PARTIAL`.
- [x] 8.2 Declarar la ausencia cuando el compositor informa `NONE`: recorrer la escalera de motivos, descartar el texto del compositor y no adjuntar ningún hecho; verificar con pruebas unitarias que el turno sale como `no_records` con su motivo y sin eventos, y que no reutiliza el texto del compositor (UC-008 `A4`).
- [x] 8.3 Añadir `prompts/clinical-answer-v3.txt` con la cobertura de la respuesta y apuntar el adaptador real a esa versión; verificar con las pruebas del compositor real que interpreta `full`, `partial` y `none`, y que un valor ausente o desconocido no rompe la composición.
- [x] 8.4 Extender el compositor `fake` con la cobertura configurable; verificar con una prueba en modo `fake` que produce `NONE` sin acceso a la red.
- [x] 8.5 Añadir una prueba de integración del caso verificado con el modelo real: una búsqueda que devuelve un hecho que no responde a la pregunta; verificar que el turno declara la ausencia, no adjunta hechos y deja la historia y el índice sin cambios.
- [x] 8.6 Volver a ejecutar los siete casos contra el modelo real (no en modo `fake`) y verificar que el caso de términos que no coinciden declara la ausencia con su motivo y su salida; anotar la evidencia en la tarea 7.4 y marcarla como completa.
