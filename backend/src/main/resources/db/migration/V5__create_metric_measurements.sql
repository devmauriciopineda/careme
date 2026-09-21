-- Fase 4.3: the measurement model gains a metric dimension.
-- A measurement carries a metric, its value or values, the metric's reference unit,
-- an exact date and the consultation it came from. One measurement per metric and day.
-- The legacy table (one row per day, weight and waist) is migrated and then retired.

ALTER TABLE measurements RENAME TO measurements_legacy;

CREATE TABLE measurements (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    metric         VARCHAR(40)  NOT NULL,
    unit           VARCHAR(10)  NOT NULL,
    date           DATE         NOT NULL,
    encounter_code VARCHAR(20),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_measurements_metric_date UNIQUE (metric, date)
);

CREATE TABLE measurement_values (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    measurement_id UUID          NOT NULL REFERENCES measurements (id) ON DELETE CASCADE,
    component      VARCHAR(20)   NOT NULL,
    value          NUMERIC(12, 4) NOT NULL,
    CONSTRAINT uq_measurement_values_component UNIQUE (measurement_id, component),
    CONSTRAINT ck_measurement_values_positive CHECK (value > 0)
);

CREATE INDEX ix_measurement_values_measurement ON measurement_values (measurement_id);

CREATE TABLE admitted_metrics (
    metric      VARCHAR(40) PRIMARY KEY,
    admitted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO admitted_metrics (metric) VALUES ('weight'), ('waist'), ('blood_pressure'), ('cholesterol');

-- Weight of every legacy day, in the metric's reference unit.
INSERT INTO measurements (metric, unit, date)
SELECT 'weight', 'kg', date FROM measurements_legacy;

INSERT INTO measurement_values (measurement_id, component, value)
SELECT m.id, 'value', l.weight_kg
FROM measurements_legacy l
JOIN measurements m ON m.metric = 'weight' AND m.date = l.date;

-- Abdominal circumference of every legacy day, in the metric's reference unit.
INSERT INTO measurements (metric, unit, date)
SELECT 'waist', 'cm', date FROM measurements_legacy;

INSERT INTO measurement_values (measurement_id, component, value)
SELECT m.id, 'value', l.waist_cm
FROM measurements_legacy l
JOIN measurements m ON m.metric = 'waist' AND m.date = l.date;

DROP TABLE measurements_legacy;
