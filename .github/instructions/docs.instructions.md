---
description: "Use when writing or updating documentation under docs/. Covers language, evidence and link-instead-of-duplicate conventions."
applyTo: docs/**/*.md
---

# Documentation — Careme

- Write in English for standards and architecture documents; the domain and roadmap documents
  (`docs/concepts/`, `docs/roadmap/`, `docs/use-cases/`) are in Spanish — match the file you edit.
- Ground every architectural claim in the file that supports it (`path:line` when useful); if
  something can't be verified, mark it as not detected instead of inventing it.
- Link instead of duplicating: `README.md`, `backend/README.md` and `frontend/README.md` already
  describe the stack and the API contract — reference them rather than copying content.
- Use Mermaid for diagrams and tables for anything comparative or enumerable.
- Keep documents current; when behavior changes, update the affected doc in the same change.
