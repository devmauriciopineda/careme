## Context

See `proposal.md` for the motivation and functional scope. The current model leaves `Patient` implicit;
`docs/data-model.md` defines the target profile as a Markdown patient document in a directory sibling to
`data/events/`, with seven personal fields, derived age, optional interview, overwrite-on-update semantics,
and no clinical provenance or version history. The active consultation already collects information and
closes it in one lifecycle, while clinical facts and measurements have separate registration paths.

## Goals / Non-Goals

**Goals:**

- Add one authoritative current-value profile for the single patient.
- Integrate profile extraction and confirmation with the active consultation without changing clinical-event
  or measurement registration semantics.
- Persist profile updates only at consultation close and keep failed updates atomic from the user's view.
- Support an initial interview that can be skipped and resumed without blocking ordinary conversation.
- Keep unknown, ambiguous, and unconfirmed values absent or unchanged.

**Non-Goals:**

- Authentication, authorization, multiple patients, or third-party access.
- A version history or provenance model for profile values.
- The derived extended profile, clinical conditions, family history, allergies, or laboratory facts.
- A separate profile-management screen or independent profile API unless the existing product surfaces require
  one during implementation.

## Decisions

### D1 — Store one canonical patient profile document

The profile will use one Markdown document in a patient-profile directory sibling to `data/events/` and
`data/encounters/`, with the current values and absence represented explicitly. The document is the source
of truth for profile attributes; any search index remains derived.

**Why:** this follows the existing Markdown source-of-truth model and the target data model, while keeping
profile attributes separate from dated clinical events.

**Alternative rejected:** represent profile fields as clinical events. That would create false dates,
incorrect provenance expectations, and duplicate the same current value across two domains.

### D2 — Update only at consultation close

The active consultation will collect profile candidates alongside its other turn information, but the profile
will be updated only during close after validation and confirmation. The close operation will apply valid
profile changes without making the profile depend on whether a clinical fact or measurement was also present.

**Why:** the user explicitly chose close-time persistence, matching the existing consultation boundary and
allowing corrections within one conversation before any profile value changes.

**Alternative rejected:** update after every message. That would make corrections visible before the
consultation ends and complicate retry and atomicity behavior.

### D3 — Use last-confirmed-value replacement

For each field, the close operation will select the last clear and confirmed value from the consultation.
Updating a field overwrites the prior value; no profile revision record is created. Fields not supplied stay
absent, and fields with unresolved ambiguity retain their previous value.

**Why:** the profile represents current attributes, not longitudinal clinical facts. Versioning is explicitly
delayed to the production/versioning phase.

**Alternative rejected:** append profile revisions. This would make the profile a second historical source of
truth and contradict the current-value requirement.

### D4 — Keep interview state in the profile/consultation contract

The assistant will offer the interview when the profile is empty or incomplete, record which fields remain
absent, and allow the person to skip or resume it. A skipped interview is not an error and does not block
other conversation intents.

**Why:** the roadmap requires a gradual onboarding path rather than a mandatory form, and the missing-field
state must survive long enough to support a later resume.

**Alternative rejected:** require a complete first-run form. It would make the profile a gate to the product
and violates the stated non-blocking behavior.

### D5 — Validate profile values before a single publication

The application will validate field types, allowed representations, contradictions, and confirmation state
before publishing an update. The publication will replace the profile document as one complete current
snapshot. If publication fails, the previous document remains authoritative and the user receives a retryable
outcome.

**Why:** the existing project requires deterministic validation and no partial writes; a complete snapshot
also makes absence and replacement unambiguous.

**Alternative rejected:** write each field independently. Partial updates could leave a profile mixing old
and new values and would make retry semantics difficult to explain.

### D6 — Keep profile behavior outside clinical provenance

Profile values will not receive the clinical-event provenance block used for facts and measurements. The
consultation may be the conversational context that supplied a value, but the profile's current-value
contract will not expose or retain that context as clinical provenance.

**Why:** the roadmap explicitly limits provenance to clinical facts and defers profile versioning. This avoids
promising traceability that the profile model does not support.

## Risks / Trade-offs

- **[A profile datum is mistaken for a clinical fact]** → Use a dedicated profile candidate and persistence
  path; keep it out of event and measurement catalogs and test mixed messages.
- **[A correction overwrites a value the person intended to keep]** → Require a clear confirmation before
  replacement and ask for clarification when values conflict.
- **[A failed close leaves a partial profile]** → Publish a complete snapshot and retain the prior snapshot
  until the replacement succeeds.
- **[Interview state becomes a blocking onboarding flow]** → Treat skip as a successful branch and keep all
  non-profile conversation available.
- **[Unknown data is filled by the assistant]** → Represent absence explicitly and reject unconfirmed values
  in deterministic validation.
- **[Future profile versioning conflicts with overwrite semantics]** → Keep the storage boundary isolated so
  the later versioning phase can add history without changing the current-value behavior now specified.

## Migration Plan

1. Deploy the profile capability with an empty profile state; existing clinical events, consultations, and
   measurements remain unchanged.
2. On the first eligible conversation, offer the optional interview and publish only confirmed fields.
3. When the profile document is absent or incomplete, treat missing fields as unknown rather than deriving
   them from existing clinical facts.
4. Roll back by disabling profile extraction and profile-close publication; existing clinical persistence
   remains usable because no clinical event or measurement schema is changed.

## Open Questions

None that affect the specification or task breakdown. The concrete profile-directory name and the exact
user-facing wording can follow the repository's existing storage and Spanish conversation conventions during
implementation without changing the behavior contract.
