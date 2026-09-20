# Data model — Careme

This document describes the Careme data model: every entity with its fields,
validation rules and relationships, plus an entity-relationship diagram.

The document distinguishes two states:

- **Implemented**: what exists today in the code and in the database.
- **Proposed**: the model defined in the documentation
  (`docs/roadmap/mvp_alcance_asistente_historia_clinica.md`) that does not exist
  in the code yet.

Body tracking (`Measurement`), clinical-event registration (`ClinicalEvent`,
UC-004) and the history query through the derived index (UC-007) are implemented
today; `Patient` remains implicit.

---

## 1. Implemented model

The implemented model covers body tracking (UC-001, UC-002 and UC-003),
clinical-event registration (UC-004) and the history query through lexical
retrieval from the derived index (UC-007).

### 1.1 Measurement

Represents a body measurement recorded on a specific day. It is the persisted
entity of body tracking; it coexists with the assistant's derived table
`clinical_event_index` (see §2).

**Fields:**

- `id`: Unique identifier of the measurement (Primary Key, UUID)
- `date`: Day of the measurement (required, unique)
- `weightKg`: Weight in kilograms (required, positive)
- `waistCm`: Abdominal circumference in centimetres (required, positive)

**Validation rules:**

- The date is a calendar day, without time, and cannot be in the future.
- At most one measurement per day is allowed; the date is unique.
- Both values are required and must be strictly positive.
- Both values are expressed with a single decimal (`NUMERIC(5, 2)`).
- Weight cannot exceed 500 kg and abdominal circumference cannot exceed 400 cm.
- Registering a day that already has a measurement replaces its values; it does
  not duplicate it.

**Database constraints:**

- `uq_measurements_date`: unique on `date`.
- `ck_measurements_weight_kg_positive`: `weight_kg > 0`.
- `ck_measurements_waist_cm_positive`: `waist_cm > 0`.

**Relationships:** none. It is an independent entity; there is no user or owner
in the current model.

> **Superseded in Fase 4.** The `measurements` table and the "one measurement per
> day" rule give way to the metric model of §6.2: a measurement carries a metric,
> a value, a unit and its date precision, so blood pressure, cholesterol, weight
> and abdominal circumference share one model. `/measurements` becomes a derived
> view over it.

### 1.2 MeasurementDraft

Represents a measurement that has no database identity yet. It is used when
several measurements are written at once, as in the file import.

**Fields:**

- `date`: Day of the measurement
- `weightKg`: Weight in kilograms
- `waistCm`: Abdominal circumference in centimetres

**Validation rules:** the same as `Measurement` (date present and positive
values), but without `id`.

**Relationships:** none. It is not persisted on its own; it becomes a
`Measurement` when it is stored.

### 1.3 API models

They are not persisted; they describe the HTTP contract.

- **MeasurementRequest**: `date`, `weightKg`, `waistCm`. Applies the registration
  form rules (date not in the future, positive values, one decimal, limits of
  500 kg and 400 cm).
- **MeasurementResponse**: `id`, `date`, `weightKg`, `waistCm`. Representation
  returned to the client.
- **ImportPreviewRow**: `date`, `weightKg`, `waistCm`, `replacesExisting`.
  A measurement read from a file and what would happen if it were loaded.
- **ImportPreviewResponse**: `rows`, `totalRows`, `newCount`, `replacedCount`,
  `ignoredCount`. What the load would do, without running it.
- **ImportResultResponse**: `createdCount`, `replacedCount`, `ignoredCount`,
  `totalRows`. Result of the load.
- **ApiResponse**: `success`, `data`, `messageCode`, `message`. The common
  envelope of every response.
- **ChatMessageRequest**: `message` (max. 4000 characters), `conversationId`
  (optional), `messageId`. Message sent to the chat.
- **ChatMessageResponse**: `conversationId`, `messageId`, `status`
  (`noted`, `answered`, `no_records`, `clarification_required`,
  `general_conversation`, `duplicate`, `failed`), `message` and `events`. `noted`
  means the turn collected one or more clinical facts as notes of the open
  consultation without registering them; `registered` is not a turn status and
  appears only in the consultation close outcome, which reports the facts that
  were registered or that there was nothing to register. In an `answered`
  response, `events` holds the facts that support it; in `no_records`
  it holds no facts and, instead, the response reports the reason for the absence
  (`absenceReason`: `empty_history`, `no_events_of_type`,
  `no_events_in_period` or `no_term_match`) and the actions offered
  (`suggestedActions`: `reformulate`, `register`). The response also reports the
  operations the turn went through (`operations`), each with its own status and
  support, so a client can tell what was completed from what was not. Those fields
  are optional and additive.
- **AgentOperation**: the closed set of operations the assistant may ask for —
  `consult_history` and `record_note` — declared to the provider and executed by
  the application, which validates before writing. Registering is not a turn
  operation: the turn collects notes and only the consultation close registers.
- **ClinicalEventIntent**: the structured contract the application builds for a
  registration or a query; `kind` (`events`, `query`, `clarification`,
  `conversation`), with the search criteria in the query. It is what the
  application validates before persisting or retrieving.

**Import file format:** required columns `date`, `weight_kg` and
`abdominal_circumference_cm`, with a header. Default limit of 10000 rows.

### 1.4 Cross-cutting import rules

- Every file is validated completely before anything is written (all-or-nothing
  load).
- Rows with a repeated date are deduplicated: the last one replaces the previous
  one.
- A row without a date is an error; a row with a date but no value at all is
  ignored.
- Errors are reported as `line|field|reason`, not as user-facing sentences.

---

## 2. Clinical history assistant model (implemented)

The model defined for the MVP of the personal clinical history assistant. The
registration of clinical facts (`ClinicalEvent`) is implemented by UC-004 and the
history query by UC-007; editing and deletion are still proposed.

### 2.1 Patient

Represents the owner of the clinical history. The MVP has a single history, the
user's own, so `Patient` remains **implicit**: no entity or table is created for
it.

> **Materialised in Fase 4.** The patient profile of §6.3 materialises `Patient`.
> It is still a single patient; what changes is that the system stops being blind
> to who it belongs to.

**Relationships:**

- `events`: one-to-many relationship with the ClinicalEvent model

### 2.2 ClinicalEvent

Represents a medical fact registered by the user in their own words. Implemented
by UC-004 (`backend/src/main/java/com/careme/backend/entity/ClinicalEvent.java`).

**Fields:**

- `id`: Unique identifier of the event (Primary Key, UUID)
- `code`: Readable and stable identifier (`evt_NNN`, file-name Primary Key)
- `type`: Type of clinical fact
- `date`: Date of the fact (may be missing when it is unknown)
- `date_precision`: Degree of precision of the date
- `date_text`: The user's original temporal expression, when there was one
- `content`: Content of the clinical fact
- `source`: Origin of the fact
- `created_at`: Date and time the record was created

**Validation rules:**

- The type must be one of the four supported ones: `diagnosis`, `medication`,
  `measurement`, `note`.
- Every event has a type, content and date with its degree of precision; the date
  may be missing when the user does not state it.
- `date_precision` must be `exact`, `approximate` or `unknown`.
- The stored temporal precision cannot be higher than the one the user gave: an
  approximate date never becomes exact.
- The content preserves the values as the user expressed them (for example, a
  blood pressure of 145/92).
- Beliefs, suspicions and inferences are not registered as if they were facts.
- Only medical facts are registered; questions and general conversation do not
  create events.
- The same fact is not registered twice.

> **Reviewed in Fase 4.** `measurement` leaves the catalogue, so the remaining
> supported types are `diagnosis`, `medication` and `note`, plus the clinical
> types Fase 4 adds. Events also reference the encounter they came from (§6.1).

**Persistence notes:**

- The source of truth is Markdown documents in `data/events/`, on the backend
  filesystem (`ClinicalEventMarkdownStore.java`), with versioned front matter and
  a file name of `evt_NNN.md`.
- PostgreSQL keeps a derived, rebuildable index (the `clinical_event_index`
  table, full-text with `tsvector` and a GIN index) which is never the truth. It
  is defined in `V2__create_clinical_event_index.sql` and rebuilt with
  `POST /api/v1/clinical-events/reindex`.

**Relationships:**

- `patient`: many-to-one relationship with the Patient model (implicit in the
  MVP)
- `encounter`: the consultation the close registered the event in, declared as
  provenance in the event's front matter and in the derived index (UC-013).

### 2.3 Encounter (consulta)

The conversation as a record with its own identity and life cycle, implemented by
UC-013 (`backend/src/main/java/com/careme/backend/entity/Encounter.java`). It opens
with the conversation, collects in **notes** what the person mentions, and at its
close registers the admissible notes as clinical events with their provenance.

**Fields:**

- `id`: Unique identifier of the consultation (Primary Key, UUID)
- `code`: Readable and stable identifier (`enc_NNN`, file-name Primary Key)
- `conversation_id`: The conversation the consultation belongs to, so it survives a restart
- `status`: `open` or `closed`
- `notes`: The clinical facts collected while it is open
- `motive`: The title the close derives from the registered facts
- `summary`: The close's **derived** summary of the registered facts; it may be absent
- `created_at`: Date and time the consultation opened
- `closed_at`: Date and time the consultation closed; required when, and only when, it is closed

**EncounterNote** carries `type`, `content`, `date`, `date_precision` and
`date_text`. It is not a registered fact: it carries the temporal fidelity the
person gave so the close can register it, but it never reaches the clinical
history on its own and never carries an event code.

**Life cycle:**

- A consultation opens with the conversation and is persisted from that moment, so
  an interruption keeps what the person already narrated.
- Only one consultation is open at a time: opening a conversation closes the previous one.
- The close validates each note, registers the admissible ones as clinical events with
  their provenance and keeps the derived summary. A close that cannot complete leaves
  the consultation open so it can be retried; a repeated close registers nothing new.
- An interruption or a process shutdown closes the consultations left open and
  registers what they collected.

**Validation rules:**

- The code matches `enc_NNN`, the conversation is not blank and the status is one of the two.
- A closed consultation always records when it was closed; an open one never does.
- A note needs a type, content and date precision; an exact date needs a date.
- A closed consultation is never reopened: it accepts neither more notes nor another close.

**Persistence notes:**

- The source of truth is Markdown documents in `data/encounters/`
  (`EncounterMarkdownStore.java`), with versioned front matter including the
  conversation, the motive and the summary, and a file name of `enc_NNN.md`.
- The summary is marked as derived information in the document
  (`## Resumen (información derivada)`) so it is never mistaken for the source of truth.
- PostgreSQL keeps a derived, rebuildable index (the `encounter_index` table), defined
  in `V3__create_encounter_index.sql` and rebuilt with
  `POST /api/v1/clinical-events/reindex`.

**Relationships:**

- `conversation`: the conversation the consultation belongs to
- `events`: the clinical events registered at the close, which declare the consultation
  they came from as their provenance

---

## 3. Entity-relationship diagram

```mermaid
erDiagram
    Measurement {
        UUID id PK
        DATE date UK
        DECIMAL weight_kg
        DECIMAL waist_cm
    }

    ClinicalEvent {
        UUID id PK
        String code UK
        String type
        DATE date
        String date_precision
        String date_text
        String content
        String source
        DATETIME created_at
        String encounter_code FK
    }

    Encounter {
        UUID id PK
        String code UK
        String conversation_id
        String status
        String motive
        String summary
        DATETIME created_at
        DATETIME closed_at
    }

    EncounterNote {
        String type
        String content
        DATE date
        String date_precision
        String date_text
    }

    Patient ||--o{ ClinicalEvent : "owns"
    Encounter ||--o{ EncounterNote : "collects"
    Encounter ||--o{ ClinicalEvent : "registers"
```

> The `Measurement` block corresponds to the implemented body tracking. The
> `Patient` / `ClinicalEvent` block corresponds to the clinical history
> assistant, implemented for fact registration (UC-004) and its query (UC-007);
> `Patient` remains implicit. The `Encounter` block is implemented by UC-013 —the
> provenance its close registers is UC-013b— and its notes are never events. In
> PostgreSQL only the derived indexes `clinical_event_index` and
> `encounter_index` exist, never as the source of truth.

---

## 4. Design principles

1. **Stable identity**: every record has a unique identifier; for measurements it
   is a UUID and for clinical events a readable `code` is added, which also names
   the file.
2. **One measurement per day**: the date identifies the measurement within the
   day, so registering a day again updates instead of duplicating. The rule
   describes the implemented model; §6.2 replaces it with a metric model where
   several metrics and several readings per day are possible.
3. **Domain invariants**: the domain type (`Measurement`) rejects any invalid
   value when it is constructed, so an invalid record cannot exist.
4. **Contract separated from storage**: the API DTOs and the persistence entity
   are kept apart, so the HTTP contract does not change when the table changes.
5. **Temporal fidelity**: dates are treated as calendar days, without time, and
   an approximate date is never presented as exact.
6. **All or nothing**: an import validates the whole file before writing anything,
   so it never leaves half-written records.
7. **Explicit source of truth**: clinical events are the primary source; derived
   summaries must be rebuildable from them.

---

## 5. Notes

- `Measurement` and `ClinicalEvent` belong to two stages of the product: body
  tracking and the clinical history assistant. The assistant exposes registration
  and query; it does not expose editing or deletion yet.
- The implemented model has no users: the person is not identified and
  measurements of different people are not distinguished.
- Required fields guarantee the core information, and optional fields allow
  flexible input without losing that core.
- The schemas are defined by the Flyway migrations:
  `V1__create_measurements_table.sql` (the `measurements` table),
  `V2__create_clinical_event_index.sql` (the derived index
  `clinical_event_index`), `V3__create_encounter_index.sql` (the derived index
  `encounter_index`) and `V4__add_provenance_to_clinical_event_index.sql` (the
  consultation a registered fact came from); the JPA mapping is validated against
  them.
- §6 records the target model of the roadmap's Fase 4. It is **not implemented**:
  nothing in §1 to §5 depends on it, and no table or migration exists for it yet.

---

## 6. Fase 4 target model

The model the roadmap's Fase 4 introduces. Most of it is **not implemented**: it is
recorded here so the implemented model above and the target one are not confused.
The exception is the encounter (§6.1): UC-013 implemented it, and it now lives in
§2.3 with its own life cycle.

### 6.1 Encounter (consulta)

**Implemented by UC-013**, and documented in §2.3: the conversation stopped being
in-memory state and became a record with an identifier and a life cycle. The facts
the person reports are registered with their **provenance**, referencing the
consultation they came from, and the registration happens at the close.

What remains target-only is the link between the consultation and the metric model
of §6.2.

**Relationships:**

- `events`: one-to-many with ClinicalEvent
- `measurements`: one-to-many with the metric model of §6.2

### 6.2 Measurement (metric model)

One model for weight, abdominal circumference, blood pressure, cholesterol and any
other metric.

**Fields:**

- `id`: Unique identifier
- `metric`: Which quantity was measured (`weight`, `waist`, `systolic`,
  `diastolic`, `cholesterol`…)
- `value`: Numeric value
- `unit`: Unit of the value
- `date`: Date of the measurement, with the same precision rules as an event
- `date_precision`: `exact`, `approximate` or `unknown`
- `date_text`: The user's original temporal expression, when there was one
- `encounter_id`: The encounter it came from
- `created_at`: Date and time the record was created

**Validation rules:**

- The value is numeric and strictly positive.
- Both `value` and `unit` are required.
- The temporal fidelity rules of ClinicalEvent apply unchanged.
- Several metrics, and several readings of the same metric, may exist on the same
  day: the uniqueness of `date` disappears.

**Relationships:**

- `encounter`: many-to-one with Encounter

### 6.3 Patient profile

Materialises `Patient`, which today is implicit (§2.1).

**Fields:**

- `name`: Name and surnames
- `date_of_birth`: Date of birth. Age is derived from it and never stored
- `sex`
- `occupation`
- `blood_group` and `rh`
- `height_cm`: Height

**Behaviour:**

- The assistant proposes an **interview** on the first interaction. The user may
  skip it and resume it later, and the assistant asks again for a missing datum
  when the conversation needs it.
- The profile is updated when the user reports a change.
- **Only current values are kept.** The document is overwritten on update and the
  profile values carry no history of their own: correcting a datum discards the
  previous one. The provenance of the clinical model applies to clinical facts,
  not to these values; record versioning is Fase 8.4.
- A datum may be **unknown**: it is stored as absent and never invented.
- Representation: a Markdown patient document in a directory sibling to
  `data/events/`, under the same principle as the encounter (§6.1).

It does **not** contain allergies, vaccinations, family history, conditions or
laboratory results. Those are clinical facts, covered by the event types and the
conditions, and they are summarised in §6.4.

### 6.4 Extended profile

A **derived** document composed automatically from the patient profile plus the
main facts: active conditions, allergies, current medication, recent measurements
and family history.

In the sense of design principle 7 it is derived information: marked as such and
rebuildable from the facts, never a source of truth.

### 6.5 What changes with respect to the implemented model

| Implemented | Fase 4 |
| --- | --- |
| `Patient` is implicit: no entity and no table | materialised as the profile of §6.3 |
| No patient attributes to read | the assistant has the profile and the extended profile of §6.4 |
| Family history has no category of its own | event type `family_history`, with the relative in the content |
| `measurements` table with `weight_kg` and `waist_cm`, both required | metric model of §6.2 |
| One row per day (`uq_measurements_date`) | several metrics and readings per day |
| `measurement` is a clinical event type | it leaves the catalogue; the remaining types are `diagnosis`, `medication` and `note`, plus the new ones |
| `/measurements` reads its own table | `/measurements` is a derived view over the metric model |
| The assistant redirects weight and waist questions | it registers and queries them like any other metric |
| The conversation lives in memory with a TTL | **implemented by UC-013**: the consultation is a persisted record and gives provenance |
| No numeric comparison across time | Fase 7.3 can compare values over time |
