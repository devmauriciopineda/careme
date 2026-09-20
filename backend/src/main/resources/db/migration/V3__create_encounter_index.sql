CREATE TABLE encounter_index (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL,
    motive TEXT,
    summary TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    -- The notes are not indexed: they are not facts and MUST NOT surface in any
    -- retrieval over the clinical history. What is indexed is the consultation
    -- itself, so an isolated index can still be searched by its own content.
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(code, '') || ' ' || coalesce(motive, '') || ' ' || coalesce(summary, ''))
    ) STORED,
    CONSTRAINT ck_encounter_index_status CHECK (status IN ('open', 'closed')),
    CONSTRAINT ck_encounter_index_closed CHECK ((status = 'closed') = (closed_at IS NOT NULL))
);

CREATE INDEX idx_encounter_index_search_vector
    ON encounter_index USING GIN (search_vector);
