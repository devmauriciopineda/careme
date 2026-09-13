## Why

UC-004 ya dispone de validación y persistencia de eventos clínicos, pero el repositorio no tiene el flujo que recibe un mensaje, consulta un LLM y entrega una intención estructurada al registro. Sin un contrato de conversación, el núcleo no puede utilizarse desde la aplicación y las aclaraciones, reintentos y fallos no tienen comportamiento definido.

## What Changes

- Añadir un endpoint JSON no-streaming para enviar mensajes al asistente.
- Añadir un orquestador que coordine mensaje, estado conversacional efímero, adaptador LLM y registro de eventos.
- Añadir un adaptador de proveedor LLM con respuesta estructurada y validación determinista.
- Definir `conversationId`, `messageId`, idempotencia, aclaraciones pendientes y resultados conversacionales.
- Conectar la pantalla principal a un chat mínimo accesible con estados de envío, aclaración, éxito, duplicado y error.
- Mantener Markdown como fuente de verdad de eventos y PostgreSQL únicamente como índice derivado y reconstruible.
- Mantener la conversación fuera de la persistencia: el estado de turnos vive en memoria y se pierde al reiniciar.
- Mantener fuera de alcance consultas, búsqueda, RAG, autenticación, multiusuario, streaming, edición y borrado.

## Capabilities

### New Capabilities

- `assistant-conversation`: contrato HTTP, ciclo de vida conversacional efímero, orquestación, resultados y límites entre frontend, backend y LLM.
- `llm-clinical-intent-adapter`: contrato proveedor-neutral para convertir mensajes en intenciones clínicas estructuradas y validar respuestas del proveedor.
- `assistant-chat-interface`: interfaz mínima de chat para enviar mensajes y representar resultados del asistente.

### Modified Capabilities

- `clinical-event-registration`: ampliar el contrato de integración para que el registro se invoque desde una conversación, manteniendo sus invariantes clínicas y su persistencia atómica.

## Impact

- Backend Spring Boot: nuevos DTOs, controlador, orquestador, estado conversacional en memoria y adaptador LLM configurable por variables de entorno.
- Frontend Next.js: sustitución de la pantalla raíz por el chat y cliente HTTP tipado.
- Configuración: proveedor/modelo/credencial solo en backend; límites y observabilidad redactada.
- Documentación: contrato de conversación, seguridad operativa, persistencia Markdown/índice y criterios adicionales de UC-004.
- Pruebas unitarias, de integración HTTP y de frontend; las pruebas de mediciones existentes deben permanecer sin cambios funcionales.
