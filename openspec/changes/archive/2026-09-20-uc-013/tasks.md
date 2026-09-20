## 1. Modelo y almacén de la consulta

- [x] 1.1 Modelar la consulta y sus notas con identidad propia y estado en curso, y verificar con pruebas unitarias que una consulta nace en curso, que una nota no es un hecho y que la identidad distingue dos consultas (`clinical-consultation`, «Abrir una consulta con la conversación»).
- [x] 1.2 Implementar el almacén Markdown de consultas en `data/encounters/` con código `enc_NNN` y front matter versionado, y verificar con una prueba de ida y vuelta que el documento y el modelo coinciden.
- [x] 1.3 Añadir la migración Flyway del índice derivado de consultas con su texto completo y verificar con Testcontainers que el arranque aplica las migraciones sobre una base vacía.
- [x] 1.4 Extender la reconstrucción del índice derivado a las consultas y verificar con una prueba que vaciar el índice y reconstruirlo recupera las consultas desde Markdown (`clinical-consultation`).

## 2. Persistencia de la consulta y de sus notas

- [x] 2.1 Abrir y persistir la consulta al iniciar la conversación, y verificar con una prueba de contrato que un mensaje sin identificador de conversación crea conversación y consulta y devuelve el identificador (`assistant-conversation`, «Process chat messages through a bounded conversation»).
- [x] 2.2 Persistir una nota por cada hecho recogido dentro de la publicación atómica del turno, y verificar que la consulta conserva la nota con su información disponible y su precisión temporal.
- [x] 2.3 Cerrar la consulta anterior al iniciar una conversación nueva y verificar que nunca quedan dos consultas en curso ni se pide a la persona elegir entre ellas (`clinical-consultation`, «Mantener una sola consulta en curso»).
- [x] 2.4 Verificar con una prueba de integración que las notas no aparecen en las consultas a la historia ni en la inspección de eventos mientras la consulta está en curso (`clinical-consultation`, «Mantener las notas fuera de la historia clínica»).

## 3. Recogida de notas en el turno

- [x] 3.1 Retirar `registered` del catálogo de resultados del turno y añadir `noted` con su contenido, y verificar con pruebas de contrato los dos valores y el estado del turno cuando recoge hechos y responde en el mismo mensaje (`assistant-conversation`, «Report the outcome of the turn and the operations it went through»).
- [x] 3.2 Acotar las operaciones del turno a consultar la historia y recoger notas, retirando el registro del camino del turno, y verificar con una prueba que la historia clínica no cambia durante un turno que contiene un hecho (`assistant-agent-turn`, «Acotar las operaciones disponibles»).
- [x] 3.3 Extender la detección de duplicados a las notas de la consulta en curso y verificar que mencionar el mismo hecho varias veces a lo largo de la conversación produce un solo evento en el cierre (`clinical-event-registration`, «Evitar duplicados dentro de la conversación»).
- [x] 3.4 Completar la información de lo recogido: preguntar en español lo que falta, ampliar contexto con lo que ya consta en la historia y pedir aclaración de lo ambiguo; verificado con los recorridos de extremo a extremo contra el proveedor real, donde el asistente pide lo que falta y no completa con suposiciones (`clinical-consultation`, «Completar la información de lo recogido sin inventarla»).
- [x] 3.5 No registrar dentro del turno y no presentar como registrado lo que solo se recogió, y verificar con una prueba de contrato que ninguna confirmación de registro se emite antes del cierre (`assistant-agent-turn`, «Validar y escribir dentro del camino de la operación»).

## 4. Cierre de la consulta

- [x] 4.1 Implementar el cierre: registrar todos los hechos recogidos —que ya superaron la comprobación determinista al anotarse— y conservar el resumen derivado, y verificar con pruebas unitarias y de integración que el cierre registra lo recogido y que un cierre sin hechos no escribe nada (`clinical-event-registration`, «Registrar hechos clínicos propios»).
- [x] 4.2 Declarar la procedencia de cada hecho registrado —quién lo aportó, cuándo se registró y de qué consulta procede— en el front matter del evento y en el índice derivado, y verificar con una prueba que reconstruir el índice desde Markdown la conserva (`clinical-fact-provenance`, «Conservar la procedencia como parte de la fuente de verdad»).
- [x] 4.3 No crear un duplicado al cerrar: el cierre depura las notas repetidas por huella antes de registrar, verificado con una prueba unitaria que dos notas del mismo hecho producen un solo evento (`clinical-event-registration`, «Evitar duplicados dentro de la conversación»).
- [x] 4.4 Generar el motivo y guardar el resumen marcado como información derivada, y verificar con una prueba que el resumen se reconstruye desde los hechos de la consulta y que el motivo no se expone a la persona (`clinical-consultation`, «Conservar el resumen de la consulta»).
- [x] 4.5 Hacer reintentable el cierre: verificar con una prueba que un fallo no da la consulta por cerrada, conserva lo recogido y permite reintentar el cierre sin dejar nada a medias (`clinical-consultation`, «Consulta que permanece abierta por un cierre que no puede completarse»).
- [x] 4.6 Cerrar una consulta sin hechos clínicos y declararlo de forma distinta a un fallo, y verificar con una prueba de contrato que la historia permanece exactamente como estaba (`clinical-consultation`, «No confundir la ausencia de hechos con un fallo»).
- [x] 4.7 Cerrar la consulta ante una interrupción y al terminar el proceso, y verificar con una prueba que lo recogido sobrevive, que se registra lo admisible y que la consulta no queda abierta esperando a la persona (`clinical-consultation`, «Cerrar la consulta ante una interrupción»).
- [x] 4.8 Informar del cierre: la respuesta declara los hechos registrados con su procedencia y, cuando el registro no puede completarse, se devuelve `failed` sin cerrar la consulta, verificado con pruebas unitarias (`assistant-conversation`, «Close the consultation in progress»).

## 5. Contrato y superficie

- [x] 5.1 Añadir la operación de cierre idempotente al contrato de chat y verificarla con MockMvc: cierre de una consulta en curso, repetición del mismo cierre sin efecto nuevo y rechazo de una solicitud inválida (`assistant-conversation`, «Close the consultation in progress»).
- [x] 5.2 Añadir la acción de terminar la consulta a la superficie del chat con su estado ocupado y su prevención de envío duplicado, y verificar con pruebas de componente (`assistant-chat-interface`, «End the consultation from the chat surface»).
- [x] 5.3 Mostrar el resultado del cierre distinguiendo lo registrado de lo no registrado y sin exponer el identificador, el resumen ni el motivo, y verificar con pruebas de componente.
- [x] 5.4 Actualizar los recorridos de extremo a extremo de Playwright a la nueva secuencia conversación → recogida → cierre y verificar que pasan con el stack real en ejecución: `e2e/playwright/uc-013.spec.ts` nuevo y `uc-012.spec.ts` actualizado, 9/9 en verde contra el stack real con el proveedor real (`assistant-chat-interface`).

## 6. Documentación y verificación

- [x] 6.1 Actualizar `backend/README.md` con el contrato nuevo y verificar que documenta el resultado `noted`, la operación de cierre y el momento del registro (`backend/README.md` es la fuente de verdad del contrato).
- [x] 6.2 Actualizar `docs/data-model.md` sacando la consulta del bloque «Fase 4 target model» y `docs/architecture.md` con el ciclo de vida de la consulta, y verificar que ambos coinciden con las specs del cambio.
- [x] 6.3 Reconciliar los casos de uso afectados (`UC-012` §11, y la disponibilidad del registro en `UC-004`) con el comportamiento implementado, y verificar que no queda ninguna nota de revisión pendiente sin resolver.
- [x] 6.4 Ejecutar `.\mvnw.cmd verify` en `backend/` y `pnpm lint`, `pnpm typecheck` y `pnpm test` en `frontend/`, y verificar que el gate de cobertura y todas las pruebas pasan: `BUILD SUCCESS` con el gate JaCoCo, 140 pruebas de frontend y los 9 recorridos de Playwright en verde.
