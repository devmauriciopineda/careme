## graphify

For any question about this repo's architecture, structure, components, or how to add/modify/find
code, your first action should be `graphify query "<question>"` when `graphify-out/graph.json`
exists. Use `graphify path "<A>" "<B>"` for relationship questions and `graphify explain "<concept>"`
for focused-concept questions. These return a scoped subgraph, usually much smaller than the full
report or raw grep output.

Triggers: "how do I…", "where is…", "what does … do", "add/modify a <component>",
"explain the architecture", or anything that depends on how files or classes relate.

If `graphify-out/wiki/index.md` exists, use it for broad navigation. Read `graphify-out/GRAPH_REPORT.md`
only for broad architecture review or when query/path/explain do not surface enough context. Only read
source files when (a) modifying/debugging specific code, (b) the graph lacks the needed detail, or
(c) the graph is missing or stale.

Type `/graphify` in Copilot Chat to build or update the graph.


# context-mode — MANDATORY routing rules

context-mode MCP tools available. Rules protect context window from flooding. One unrouted command dumps 56 KB into context.

## Think in Code — MANDATORY

Analyze/count/filter/compare/search/parse/transform data: **write code** via `ctx_execute(language, code)`, `console.log()` only the answer. Do NOT read raw data into context. PROGRAM the analysis, not COMPUTE it. Pure JavaScript — Node.js built-ins only (`fs`, `path`, `child_process`). `try/catch`, handle `null`/`undefined`. One script replaces ten tool calls.

## BLOCKED — do NOT attempt

### curl / wget — BLOCKED
Terminal `curl`/`wget` intercepted and blocked. Do NOT retry.
Use: `ctx_fetch_and_index(url, source)` or `ctx_execute(language: "javascript", code: "const r = await fetch(...)")`

### Inline HTTP — BLOCKED
`fetch('http`, `requests.get(`, `requests.post(`, `http.get(`, `http.request(` — intercepted. Do NOT retry.
Use: `ctx_execute(language, code)` — only stdout enters context

### WebFetch / fetch — BLOCKED
Use: `ctx_fetch_and_index(url, source)` then `ctx_search(queries)`

## REDIRECTED — use sandbox

### Terminal / run_in_terminal (>20 lines output)
Terminal ONLY for: `git`, `mkdir`, `rm`, `mv`, `cd`, `ls`, `npm install`, `pip install`.
Otherwise: `ctx_batch_execute(commands, queries)` or `ctx_execute(language: "javascript", code: "...")`. Use `language: "shell"` only when code matches the host shell.

### read_file (for analysis)
Reading to **edit** → read_file correct. Reading to **analyze/explore/summarize** → `ctx_execute_file(path, language, code)`.

### grep / search (large results)
Use `ctx_execute(language: "javascript", code: "...")` in sandbox for portable filtering/counting.

## Tool selection

0. **MEMORY**: `ctx_search(sort: "timeline")` — after resume, check prior context before asking user.
1. **GATHER**: `ctx_batch_execute(commands, queries)` — runs all commands, auto-indexes, returns search. ONE call replaces 30+. Each command: `{label: "header", command: "..."}`.
2. **FOLLOW-UP**: `ctx_search(queries: ["q1", "q2", ...])` — all questions as array, ONE call (default relevance mode).
3. **PROCESSING**: `ctx_execute(language, code)` | `ctx_execute_file(path, language, code)` — sandbox, only stdout enters context.
4. **WEB**: `ctx_fetch_and_index(url, source)` then `ctx_search(queries)` — raw HTML never enters context.
5. **INDEX**: `ctx_index(content, source)` — store in FTS5 for later search.

### Parallel I/O batches
Pass `concurrency: 4-8` to `ctx_batch_execute` and `ctx_fetch_and_index` for network/API batches. Keep `concurrency: 1` for CPU-bound work (test, build, lint). GitHub gh: cap at 4.

## Output

Write artifacts to FILES — never inline. Return: file path + 1-line description.
Descriptive source labels for `ctx_search(source: "label")`.

## Session Continuity

Skills, roles, and decisions persist for the entire session. Do not abandon them as the conversation grows.

## Memory

Session history is persistent and searchable. On resume, search BEFORE asking the user:

| Need | Command |
|------|---------|
| What were we working on? | `ctx_search(queries: ["summary"], source: "compaction", sort: "timeline")` |
| What did we decide? | `ctx_search(queries: ["decision"], source: "decision", sort: "timeline")` |
| What NOT to repeat? | `ctx_search(queries: ["rejected"], source: "rejected-approach")` |
| What constraints exist? | `ctx_search(queries: ["constraint"], source: "constraint")` |

Note: user-prompt history not available.

DO NOT ask "what were we working on?" — SEARCH FIRST.
If search returns 0 results, proceed as a fresh session.

## ctx commands

| Command | Action |
|---------|--------|
| `ctx stats` | Call `ctx_stats` MCP tool, display full output verbatim |
| `ctx doctor` | Call `ctx_doctor` MCP tool, run returned shell command, display as checklist |
| `ctx upgrade` | Call `ctx_upgrade` MCP tool, run returned shell command, display as checklist |
| `ctx purge` | Call `ctx_purge` MCP tool with confirm: true. Warns before wiping knowledge base. |

After /clear or /compact: knowledge base and session stats preserved. Use `ctx purge` to start fresh.


---

# Project Guidelines — Careme

Careme is a full-stack personal clinical assistant and body-tracking application: a
Spanish-language chat that records patient-stated clinical facts, plus day-to-day tracking of
weight (kg) and abdominal circumference (cm). It is an early MVP with **no authentication**.
The assistant records facts only — it **never diagnoses or recommends treatment**. Do not add
behavior that crosses that line.

## Architecture

- `frontend/` — Next.js 16 App Router (React 19, TypeScript strict). The chat workspace is the
  entry point at `/`; body tracking lives at `/measurements`. Server Components fetch at request
  time; forms are client islands that submit through Server Actions.
- `backend/` — Spring Boot 3.5 REST service (Java 21), layered `controller → service → repository`.
  It is the source of truth for the API contract. LLM access is opt-in via `CAREME_LLM_MODE`;
  the default `fake` mode makes no network call.
- **Storage** — PostgreSQL 17 with Flyway-owned migrations, plus Markdown clinical-event documents
  as the source of truth for clinical events, with a derived, rebuildable PostgreSQL full-text index.
- `openspec/` — spec-driven development (SDD): main specs and archived changes.

## Sources of truth — link, do not duplicate

- API contract and backend layers: `backend/README.md`
- UI architecture, scripts and testing: `frontend/README.md`
- System architecture and data model: `docs/architecture.md`, `docs/data-model.md`
- Coding standards: `docs/standards/java-springboot-standards.md`, `docs/standards/next-standards.md`
- Domain concepts and use cases: `docs/concepts/`, `docs/use-cases/`
- Roadmap and MVP scope: `docs/roadmap/`

## Build and test

- Backend, from `backend/`: `.\mvnw.cmd spring-boot:run` (dev), `.\mvnw.cmd test`,
  `.\mvnw.cmd verify` (tests + JaCoCo coverage gate). Needs Java 21; tests need a reachable container runtime.
- Frontend, from `frontend/` (pnpm only): `pnpm dev`, `pnpm build`, `pnpm test`, `pnpm lint`, `pnpm typecheck`.
- Whole stack: `podman compose up --build -d` (preferred); use `docker compose` when Podman is not installed.

## Conventions

- **Language**: code, identifiers and documentation in English; user-facing UI strings and the
  clinical domain vocabulary stay in Spanish.
- Backend: keep controllers thin, delegate to services, keep persistence behind repository interfaces.
- Frontend: Server Components and Server Actions by default; validate external payloads with Zod;
  use client components only where interactivity requires it.
- Behavior changes need tests; the backend enforces the coverage gate through `.\mvnw.cmd verify`.
- No authentication or authorization — explicitly out of scope for this MVP.

## Spec-driven development (mandatory for non-trivial features)

- For any non-trivial feature or behavior change, create an OpenSpec change under `openspec/` first
  and work through proposal → apply → archive, using the `openspec-*` skills in `.github/skills/`.
- Capture behavior in specs under `openspec/specs/` through the change workflow; do not edit main
  specs directly except for explicit sync operations.
- Trivial fixes (typos, small clearly-scoped bug fixes) do not require a change.