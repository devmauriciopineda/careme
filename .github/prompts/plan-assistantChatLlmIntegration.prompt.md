# Plan: Cerrar contratos del asistente clínico

Cerrar el hueco previo a UC-004 mediante una especificación transversal de conversación y un contrato técnico versionado; conservar UC-004 como capacidad de registro. No crear en bloque UC-005 a UC-011: detallar solo los siguientes casos que dependan del mismo contrato cuando se planifique su implementación.

## Hallazgos

- `docs/use-cases/UC-004.md` y `docs/use-cases/UC-004-acceptance-criteria.md` definen el comportamiento observable de registro, pero presuponen chat y no definen protocolo, estado ni límite LLM/aplicación.
- El backend ya contiene parte del núcleo previsto: `ClinicalEventIntent`, `ClinicalEventIntentValidator`, `ClinicalEventRegistrationService` y `ClinicalEventConversationRegistry`. Faltan endpoint/orquestador de chat, adaptador de proveedor LLM, manejo de turnos/aclaraciones y UI de chat.
- El diseño archivado en `openspec/changes/archive/2026-09-13-uc-004/design.md` define una intención neutral de proveedor y persistencia, pero es diseño de un cambio terminado; no sustituye una especificación de integración vigente para chat ni contrato HTTP.
- `docs/roadmap/use-cases.md` solo enumera UC-005 a UC-011. No se deben convertir todos en documentos formales ahora porque consulta/búsqueda/RAG introducen decisiones independientes.

## Pasos

1. Crear una decisión de arquitectura o especificación transversal del asistente: topología `frontend chat -> API backend -> orquestador -> proveedor LLM -> resultado estructurado -> registro`, responsabilidades y límites de confianza. Bloquea los pasos 2 a 5.
2. Especificar el contrato HTTP del chat: ruta, request/response JSON, `conversationId`, `messageId`/idempotencia, estados de respuesta (`registered`, `clarification_required`, `general_conversation`, `duplicate`, `failed`), códigos de error y compatibilidad con JSON no-streaming. Reutilizar el dominio `ClinicalEventIntent` solo como contrato interno, sin exponerlo obligatoriamente al navegador. Depende del paso 1.
3. Especificar el contrato LLM-adaptador: esquema JSON validable para intención, prompt/versionado, inyección de fecha/zona horaria, política ante respuesta inválida/timeout, y prohibición de acceso directo del modelo a persistencia. Mapear explícitamente a `ClinicalEventIntent.Kind` y sus candidatos. Depende del paso 1; puede ir en paralelo con el paso 2.
4. Especificar el estado conversacional MVP: creación/propagación de `conversationId`, correlación de una aclaración con el mensaje pendiente, deduplicación acotada a la conversación, expiración/retención y comportamiento tras reinicio. Sustituir o formalizar el registro en memoria actual según la decisión de persistencia. Depende del paso 1; puede ir en paralelo con los pasos 2 y 3.
5. Completar UC-004 con escenarios de integración no cubiertos: reintentos idempotentes, JSON LLM malformado o schema inválido, timeout/indisponibilidad del proveedor, continuación de aclaración, cambio de fecha/zona del servidor, y fallo después de recibir una respuesta estructurada. Mantener las reglas clínicas existentes sin duplicarlas. Depende de los pasos 2 a 4.
6. Crear una especificación de interfaz de chat mínima: composición de `/`, envío/estado de carga, lista de mensajes, reintento que conserva el texto, representación accesible de aclaraciones/errores, y contrato del cliente con API. No diseñar todavía búsqueda ni visualizador de eventos. Depende de los pasos 2 y 4.
7. Definir seguridad y operación para información clínica: secretos solo backend, minimización de datos enviados al proveedor, proveedor/modelo/región aprobados, logging redactado, trazabilidad/correlación, límites de tasa/tamaño, retención y aviso de que no ofrece diagnóstico. Tratar las obligaciones regulatorias aplicables como decisión explícita del producto, no como supuesto técnico. Depende del paso 1.
8. Documentar la transición de persistencia: formato canónico de evento, política de numeración `evt_NNN`, atomicidad Markdown/índice, arranque/reindexado y volumen de `data/events/`. Validar que el código actual y la documentación vigente coinciden; corregir el que sea fuente secundaria. Depende del paso 1.
9. Preparar un cambio OpenSpec nuevo, `assistant-chat-llm-integration`, que conecte la UI/API/orquestador/LLM con el núcleo de UC-004 ya implementado. Su diseño debe tratar los servicios actuales de registro como dependencia y no volver a modelar la persistencia clínica. Depende de los pasos 1 a 8.
10. Redactar UC-005 y UC-006 antes de implementar recuperación; redactar UC-007 después de fijar la política de evidencia/citas de recuperación; redactar UC-008 junto con UC-005 a UC-007 como regla transversal de ausencia. UC-009 no requiere documento nuevo: ya es flujo alterno de UC-004. UC-010 puede esperar hasta definir el comportamiento de conversación general. UC-011 puede ser una interfaz administrativa/debug y no debe bloquear el chat MVP.

## Archivos relevantes

- `docs/use-cases/UC-004.md`: reglas de negocio y escenarios clínicos base.
- `docs/use-cases/UC-004-acceptance-criteria.md`: ampliar cobertura de integración.
- `docs/roadmap/use-cases.md`: backlog y orden de casos futuros.
- `docs/roadmap/mvp_alcance_asistente_historia_clinica.md`: límites LLM/backend, herramientas y persistencia objetivo.
- `openspec/changes/archive/2026-09-13-uc-004/design.md`: reutilizar decisiones aún válidas, no como contrato vigente de chat.
- `backend/src/main/java/com/careme/backend/dto/ClinicalEventIntent.java`: límite interno proveedor-neutral existente.
- `backend/src/main/java/com/careme/backend/service/ClinicalEventRegistrationService.java`: núcleo de registro que recibirá la intención desde el orquestador.
- `backend/src/main/java/com/careme/backend/service/ClinicalEventConversationRegistry.java`: deduplicación en memoria que requiere contrato de ciclo de vida.

## Verificación

1. Revisar trazabilidad: cada condición de UC-004 debe mapear a una respuesta HTTP, a un resultado del orquestador y a una presentación en chat.
2. Validar ejemplos JSON del contrato contra un JSON Schema o DTOs de backend, incluidos todos los resultados y fallos.
3. Probar con una matriz de mensajes: evento único/múltiple, fecha exacta/aproximada/relativa/ausente, sospecha, conversación general, ambigüedad y repetición.
4. Probar fallos de proveedor, payload inválido, reintento del mismo `messageId`, reinicio del backend durante una aclaración y fallo de persistencia; verificar ausencia de eventos parciales o duplicados.
5. Confirmar que pruebas de mediciones siguen verdes y que ningún secreto ni contenido clínico completo aparece en logs de frontend.

## Decisiones

- Decidido: no generar ahora UC-005 a UC-011 completos. Son capacidades de recuperación/conversación con decisiones que UC-004 no resuelve.
- Decidido: el siguiente incremento integra chat real y proveedor LLM con UC-004, ya implementado como núcleo de registro.
- Decidido: no habrá memoria conversacional persistente. `conversationId` y aclaraciones viven en la sesión activa; los eventos que se registran se persisten como Markdown.
- Decidido: Markdown en `data/events/` es la fuente de verdad de eventos clínicos; PostgreSQL contiene únicamente el índice derivado, reconstruible desde Markdown.
- Documentos mínimos adicionales: contrato de conversación/API, contrato LLM-adaptador, ciclo de vida de estado conversacional efímero, especificación de UI de chat, y política de seguridad/operación de datos clínicos.
- UC-004 necesita una adenda de escenarios de integración; no una reescritura funcional.
- Excluido: autenticación, multiusuario, streaming, RAG/embeddings, edición/borrado y selección concreta de proveedor/modelo hasta que producto los decida.

## Consideraciones adicionales

1. Seleccionar el proveedor/modelo LLM, la región y el mecanismo de credencial de backend antes de construir el adaptador.
2. Definir el marco de privacidad/regulatorio y la residencia de datos antes de transmitir datos clínicos al proveedor.
3. La especificación de integración debe exigir publicación atómica: un evento solo queda visible tras escribir Markdown y actualizar el índice, con reconciliación/reindexado para reparar divergencias.
