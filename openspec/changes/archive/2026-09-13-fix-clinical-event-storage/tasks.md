## 1. Configurable source-of-truth location

- [x] 1.1 Add the `careme.events.directory` property with `data/events` as default and resolve the store directory from it; verify Spring still resolves a single constructor and the unit tests that build the store from a temporary path keep passing.
- [x] 1.2 Add a unit test proving documents are written under the configured directory; verify no test depends on the repository working directory.

## 2. Writable data directory in the image

- [x] 2.1 Create `/app/data/events` owned by the backend process user in the backend Dockerfile before switching user; verify a rebuilt image can create and write the directory as that user.

## 3. Durable Markdown source of truth

- [x] 3.1 Mount a named volume at `/app/data` in `docker-compose.yml`; verify the mount point is writable by the container user and that ownership comes from the image rather than from root.

## 4. Diagnosability

- [x] 4.1 Log the swallowed persistence and registration failures with their exception and the `conversationId`, keeping the user-facing Spanish message free of internal detail; verify the container log shows the cause of a failed registration.

## 5. Verification

- [x] 5.1 Rebuild and start the stack, register "Ayer me diagnosticaron hipertensión" from the interface, and verify the response is `registered`, a Markdown document exists under the configured directory, and the derived index holds one row.
- [x] 5.2 Recreate the backend container and verify the registered event is still present, proving the volume and the reconciliation path agree.
- [x] 5.3 Run the backend test suite and OpenSpec strict validation; verify no regression and that the previous change's suite stays green.

## 6. Event code allocation and non-destructive publication

- [x] 6.1 Derive the event codes from the persisted documents instead of an in-memory counter, reserving one code per event of the attempt; verify a registration after a restart uses a free code.
- [x] 6.2 Make Markdown publication create-only so an existing document is never overwritten or deleted; verify a publication aimed at an existing code fails before staging anything.
- [x] 6.3 Add unit tests for allocation after existing documents and for refusing to overwrite; verify the rollback leaves pre-existing documents untouched.
- [ ] 6.4 Rebuild, register two facts in a row, restart the backend, register a third, and verify the three documents and their index rows coexist and remain consultable.
