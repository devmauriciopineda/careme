-- Provenance of a registered fact: the consultation it was collected in.
-- Nullable on purpose: events registered before the consultation existed, or
-- outside one, keep a valid provenance with no consultation, and no origin is
-- invented for them.
ALTER TABLE clinical_event_index ADD COLUMN encounter_code VARCHAR(32);

CREATE INDEX idx_clinical_event_index_encounter_code
    ON clinical_event_index (encounter_code);
