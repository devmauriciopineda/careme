CREATE TABLE clinical_event_index (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    event_date DATE,
    date_precision VARCHAR(16) NOT NULL,
    date_text TEXT,
    content TEXT NOT NULL,
    source VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(code, '') || ' ' || coalesce(type, '') || ' '
            || coalesce(date_text, '') || ' ' || content)
    ) STORED,
    CONSTRAINT ck_clinical_event_index_type CHECK (type IN ('diagnosis', 'medication', 'measurement', 'note')),
    CONSTRAINT ck_clinical_event_index_date_precision CHECK (date_precision IN ('exact', 'approximate', 'unknown')),
    CONSTRAINT ck_clinical_event_index_source CHECK (source = 'patient'),
    CONSTRAINT ck_clinical_event_index_exact_date CHECK (date_precision <> 'exact' OR event_date IS NOT NULL)
);

CREATE INDEX idx_clinical_event_index_search_vector
    ON clinical_event_index USING GIN (search_vector);
