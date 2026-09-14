---
description: "Use when creating or updating OpenSpec artifacts (proposal, spec, design, tasks) under openspec/, or when planning a non-trivial feature. Covers the mandatory spec-driven workflow."
---

# Spec-driven development — Careme

This repository uses OpenSpec (`openspec/`) as the mandatory workflow for non-trivial features.

- Start with a change under `openspec/changes/` and walk it through proposal → apply → archive.
  Use the `openspec-*` skills in `.github/skills/` (`openspec-propose`, `openspec-apply-change`,
  `openspec-archive-change`, `openspec-sync-specs`, `openspec-update-change`, `openspec-explore`).
- Edit main specs in `openspec/specs/` only through the change workflow or an explicit sync;
  don't hand-edit them alongside an active change.
- Keep artifacts coherent with each other and with the code that already exists — read the
  relevant specs and `docs/architecture.md` before writing.
- Trivial fixes (typos, small clearly-scoped bug fixes) don't need a change.
