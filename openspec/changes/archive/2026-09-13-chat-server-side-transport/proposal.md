## Why

El chat llegaba a la API desde el navegador. `CorsConfig` solo permitía `GET`, así que el preflight del `POST` se rechazaba y el navegador lo reportaba como `TypeError: Failed to fetch`, sin respuesta legible: la persona veía "No se pudo completar" y el turno nunca llegaba al backend. Además `apiConfig` resolvía la dirección de la API en el bundle del cliente, donde una variable sin prefijo `NEXT_PUBLIC_` es `undefined` y caía a `http://localhost:8080`.

## What Changes

- El frontend deja de llamar a la API desde el navegador: el chat sale por una server action de Next, con validación del payload y un resultado tipado que cruza la frontera servidor/cliente.
- `chatService` queda server-only: valida la respuesta con Zod, normaliza cualquier fallo de transporte y no devuelve detalle técnico a quien lo invoca.
- La interfaz consume el resultado de la action, comunica el fallo con un mensaje en español sin detalle técnico y conserva el reintento con un `messageId` nuevo.
- `apiConfig` deja de resolver una base URL pública; se mantiene el valor de respaldo para desarrollo.
- El CORS de la API permite los verbos que un cliente del navegador usa (`GET` y `POST`) desde el origen declarado, y sigue cerrado para el resto.
- Fuera de alcance: cambios al contrato HTTP del chat, a los DTOs, al almacenamiento clínico, a las mediciones y al adaptador LLM.

## Capabilities

### New Capabilities

_(ninguna)_

### Modified Capabilities

- `assistant-chat-interface`: la interfaz MUST alcanzar el backend desde el servidor y el navegador MUST NOT resolver ni transportar la dirección de la API; un turno fallido MUST comunicarse desde un código de fallo con nombre, no desde el error de transporte.
- `assistant-conversation`: la operación de chat MUST aceptar el preflight del origen declarado para los verbos que un cliente del navegador usa, y MUST seguir cerrada para el resto.

## Impact

- Frontend Next.js: `features/chat/actions.ts` y `features/chat/lib/schema.ts` nuevos; `services/chatService.ts` pasa a server-only con validación de respuesta; `services/apiConfig.ts` deja de resolver una base URL pública; `features/chat/components/ChatWorkspace.tsx` consume la action y pasa a `useTransition`.
- Backend Spring Boot: `config/CorsConfig.java` amplía los verbos permitidos del mapeo `/api/**`.
- Pruebas: `ChatWorkspace.test.tsx` deja de stubbear `fetch` y mockea la action, con un caso que verifica que el texto de transporte no se filtra; `config/CorsConfigTest.java` nuevo.
- Documentación: `frontend/.env.example` reencuadra `API_BASE_URL` como valor de servidor.
- Sin cambios en la API, los DTOs, las migraciones, el esquema ni el almacenamiento clínico.
