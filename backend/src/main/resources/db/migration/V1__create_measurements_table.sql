-- Body measurements: one row per recorded day.
-- `date` is unique because the application records a single measurement per day.
CREATE TABLE measurements (
    id        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    date      DATE          NOT NULL,
    weight_kg NUMERIC(5, 2) NOT NULL,
    waist_cm  NUMERIC(5, 2) NOT NULL,
    CONSTRAINT uq_measurements_date UNIQUE (date),
    CONSTRAINT ck_measurements_weight_kg_positive CHECK (weight_kg > 0),
    CONSTRAINT ck_measurements_waist_cm_positive CHECK (waist_cm > 0)
);
