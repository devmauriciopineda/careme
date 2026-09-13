## Context

`ClinicalEventMarkdownStore` writes the Markdown source of truth for clinical events, resolved from `Path.of("data", "events")` relative to the process working directory. The backend image runs `java -jar /app/app.jar` with `WORKDIR /app` and `USER careme`, and `/app` is owned by root, so `Files.createDirectories` fails with `AccessDeniedException`. `ClinicalEventRegistrationService` catches every persistence failure and returns a `FAILURE` result with a friendly Spanish message, and both catches discarded the exception, so nothing about the cause reached the logs.

`docker-compose.yml` declares no volume for the events directory. Because `ClinicalEventIndexStartupReconciler` rebuilds the derived index from Markdown on every start, an ephemeral Markdown directory means each container recreation erases the clinical history from the index with nothing left to rebuild from.

## Goals / Non-Goals

**Goals:**

- Let the deployment choose where the Markdown source of truth lives, without touching code.
- Make the containerized backend able to write that directory as a non-root user.
- Make the Markdown documents survive container recreation.
- Make a persistence failure diagnosable from the server log while the person still sees a message with no internal detail.

**Non-Goals:**

- Changing the derived index, its schema, or the reconciliation strategy.
- Moving clinical storage to object storage or a database.
- Adding per-user or multi-tenant storage paths.
- Gitignoring the development-time data directory or committing it.
- Changing the HTTP contract, the DTOs, or the conversational outcomes.

## Decisions

### The events directory becomes configuration

`ClinicalEventMarkdownStore` reads `careme.events.directory` (default `data/events`) instead of hardcoding the path. The default keeps local runs, existing tests, and the documented `data/events/` convention unchanged, while a deployment can point at any writable, persistent location.

The class keeps two constructors: one Spring-injected that takes the property, and the package-private one that takes a `Path` and is used by tests. Because Spring cannot pick between two constructors on its own, the injected one carries `@Autowired`.

Alternative rejected: relax permissions on `/app` in the image. Writing inside the image layer loses the documents on every recreation and still requires a rebuild to change the location.

### The data directory is created with the process user as owner

The Dockerfile pre-creates `/app/data/events` and gives it to `careme` before switching user, so `Files.createDirectories` is not the operation that has to succeed against a root-owned parent.

### The volume mounts at `/app/data`, not at `/app/data/events`

A named volume is initialized from the image path it covers, including ownership. Mounting at `/app/data` lets the volume inherit the `careme` ownership the image already set up. Mounting at `/app/data/events` would create a root-owned mount point on top of it and reproduce the original permission failure.

Operational consequence: a volume created before this change is root-owned and must be removed once (`podman volume rm`) so it is re-created with the correct ownership.

### The swallowed failures are logged, not surfaced

Both catches in `ClinicalEventRegistrationService` log the exception at ERROR with the `conversationId`, and keep returning the same Spanish message. The spec requires the person to see no internal detail, and the diagnosis requires the cause to be recorded somewhere.

### Event codes are derived from the persisted documents

`NEXT_CODE` was an in-memory `AtomicLong` that restarted at 1 in every process, while the code is persisted both in the document name and in the `clinical_event_index.code` unique column. After the directory became durable, the first registration of every new process regenerated `evt_001`, overwrote the existing document, and then failed on the unique constraint.

The store now allocates codes from the documents it already holds: it lists the directory, reads the highest `evt_NNN` present, and reserves the next consecutive codes for the attempt. The code is the document name, so the store is the only component that can answer this without guessing, and a restart no longer restarts the sequence.

Codes for a multi-event message are reserved once per attempt, because nothing is published until the whole batch is written; allocating inside the loop would hand every event of the batch the same code.

### Publication is create-only

`writeAtomically` used `ATOMIC_MOVE`, which silently replaces an existing document. It now refuses to publish onto a code that already exists, before staging any temporary file.

This is what makes the rollback safe. The rollback deletes the documents of the failed attempt by code, which is only correct if every document it published was created by that attempt. Combined with the allocator, a collision can no longer happen; combined with the create-only guard, a collision can no longer destroy an existing event. An allocation mistake now costs a clean failure instead of a lost clinical record.

## Risks / Trade-offs

- A pre-existing `careme-events` volume keeps root ownership and the container still cannot write. Mitigated by removing the volume once; the verification task covers a fresh volume.
- A derived-index row whose document is missing - an orphan left by the destructive rollback this change removes - makes the next allocation reuse that code and fail on the unique constraint. The startup reconciliation rebuilds the index from the documents, which drops orphans, so a restart clears it.
- Allocation is synchronized within one process and derived from the directory, so it assumes a single backend instance. Several instances sharing one directory would need a different allocation strategy.
- A configurable path can point at a directory that does not exist. `writeAtomically` already creates directories, so this only needs the parent to be writable.
- Logging the exception could capture clinical content if the exception message quoted it. The current failures are filesystem and SQL errors that carry paths and constraints, not message text; the existing log-redaction task from the previous change stays in force.
- The volume makes the Markdown durable, which also makes the startup reconciliation destructive if someone deletes the documents. That behavior already exists and is out of scope here.

## Migration Plan

1. Deploy the new image and compose file.
2. Remove any pre-existing events volume so it is re-created with `careme` ownership.
3. Restart once so the startup reconciliation drops any index row whose document is missing.
4. Register a clinical event and confirm the derived index row and the Markdown document.
5. Recreate the backend container and confirm the event is still registered.
6. Register a second event after that restart and confirm it receives a new code instead of failing.

No database migration. Rollback is the previous image plus the previous compose file; the Markdown documents remain readable either way because the format is unchanged.

## Open Questions

- Whether the development-time `data/events` directory should be gitignored or committed as fixture data. Left out of this change.
