## Context

The chat interface is a client component. It called the backend directly with `fetch(POST ${API_BASE_URL}/api/v1/chat/messages)`, and `API_BASE_URL` resolved from `process.env.API_BASE_URL`, which is never inlined into the browser bundle, so the client silently fell back to `http://localhost:8080`. A `POST` with a JSON body triggers a preflight, and `CorsConfig` only listed `GET`, so the browser rejected the request before the backend saw it and surfaced `TypeError: Failed to fetch`. `chatService` then read the backend's message out of a response that never arrived, and rethrew the transport error, which the interface rendered as the assistant's own text.

The measurements feature never had this problem because it reaches the API from server components and server actions, where the API address is available and no preflight exists.

## Goals / Non-Goals

**Goals:**

- Keep the API address on the server, where it is configured.
- Make the interface's failure wording independent of the transport.
- Stop leaking a raw transport error into the conversation as if it were an assistant answer.
- Keep the API callable from a browser client at a configured origin without opening it to every origin.

**Non-Goals:**

- Changing the chat HTTP contract, the DTOs, or the response envelope.
- Changing clinical registration, the LLM adapter, or the storage layer.
- Adding authentication, authorization, or a request rate limiter.
- Removing the CORS mapping. The interface no longer needs it, but other clients may.

## Decisions

### The chat goes through a server action, not a route handler

`features/measurements/actions.ts` already establishes server actions as the way this app mutates state, and the repository has no route handler under `app/api`. Reusing that shape keeps one transport pattern, and it means the browser never learns the API address: the action revalidates the payload with Zod and calls `chatService` on the server.

### The failure is a named code, not an exception

The action returns `{ ok: true, response }` or `{ ok: false, errorCode }` with `INVALID_INPUT` or `UPSTREAM_FAILED`, mirroring `RegisterMeasurementResult`. The interface maps the code to Spanish copy it owns. Previously the copy came from whatever `error.message` happened to hold, which is why a browser transport error became an assistant sentence.

### chatService validates the response and normalizes its failures

The service parses the payload with a Zod schema before returning it, so a changed or malformed contract fails at the boundary, and it converts a fetch rejection, a non-2xx status, and a schema mismatch into one `ChatUnavailableError`. The caller does not need to know which of the three happened.

### apiConfig keeps the local fallback but loses the public branch

`process.env.NEXT_PUBLIC_API_BASE_URL` is removed, so no value can resolve in the browser; nothing client-side imports the module. `http://localhost:8080` stays as the development fallback, which the deployment overrides with `API_BASE_URL` (`http://backend:8080` inside Compose).

### The CORS mapping lists the verbs a browser client uses

`allowedMethods("GET", "POST")`. `addMapping` starts from `applyPermitDefaultValues`, which already allows `Content-Type` and a preflight max age, so the explicit `GET`-only call was the single thing rejecting the browser's write. `OPTIONS` needs no listing: Spring handles the preflight for the verbs that are allowed.

## Risks / Trade-offs

- A server action adds a hop through the Next.js server. Acceptable: the browser has no other way to reach the API without exposing its address, and the measurements feature already pays the same hop.
- `ChatUnavailableError` collapses three causes into one. The distinction is preserved in the server log, which is where an operator can act on it, and not in the user-facing copy.
- Relaxing CORS to `POST` widens what a browser client at a configured origin may attempt. The origin list stays explicit and configured per environment, and unused verbs stay closed, which `CorsConfigTest` asserts.
- The action lives in the client bundle as a reference; the server-only modules it imports must not be pulled into it. The production build is the check, because it fails when a client component imports server-only code.

## Migration Plan

1. Deploy the backend with the widened mapping and the frontend with the action.
2. Send a chat turn and confirm the browser makes no request to the backend address.
3. Confirm a failed turn shows Spanish copy without a transport error, and that the retry keeps the original message.

No data migration. Rollback is the previous image; the API contract does not change.

## Open Questions

- Whether the interface should keep polling or retrying automatically on `UPSTREAM_FAILED`. The current behavior leaves the retry to the person, which the spec requires.
