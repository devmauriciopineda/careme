## 1. API reachability for a browser client

- [x] 1.1 Widen the API CORS mapping to the verbs a browser client uses, keeping the origin list explicit and configured; verify the configured origin's preflight for a write succeeds and an unexposed verb is refused.
- [x] 1.2 Add a test that resolves the CORS configuration for the API mapping and asserts the allowed origin, the exposed verbs, and a refused verb.

## 2. Server-side chat transport

- [x] 2.1 Add the chat payload and response schemas; verify a malformed or changed response is rejected at the boundary instead of reaching the interface.
- [x] 2.2 Make the chat API client server-only: validate the response and collapse a transport failure, a non-2xx status and a contract mismatch into one error that carries no transport detail; verify nothing client-side imports its module.
- [x] 2.3 Add the chat server action returning a typed result with named failure codes, mirroring the measurements action; verify an invalid payload is refused before any request leaves the server.
- [x] 2.4 Drop the public API base URL from the client configuration while keeping the development fallback; verify no value resolves in the browser and the deployment override still applies.

## 3. Interface

- [x] 3.1 Consume the action from the chat surface with a transition-driven busy state; verify duplicate submission is still prevented while a turn is pending.
- [x] 3.2 Word a failed turn from the named failure code, preserving the message for retry; verify the transport text never appears in the conversation.
- [x] 3.3 Update the component tests to mock the action instead of the global fetch, and add a case asserting the failure copy and that the retry uses a new message identifier.

## 4. Verification

- [x] 4.1 Run the backend and frontend suites plus lint and type checks; verify no regression.
- [x] 4.2 Build the frontend and confirm the server/client boundary holds, meaning no server-only module reached the client bundle.
- [x] 4.3 Send a turn in the running stack and verify the browser makes no request to the backend address and the outcome renders in the conversation.
