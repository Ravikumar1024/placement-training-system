ALTER TABLE trainings
    ADD COLUMN IF NOT EXISTS delivery_mode VARCHAR(20) NOT NULL DEFAULT 'ONSITE',
    ADD COLUMN IF NOT EXISTS location VARCHAR(200),
    ADD COLUMN IF NOT EXISTS meeting_url VARCHAR(500);

ALTER TABLE trainings
    ADD CONSTRAINT trainings_delivery_mode_check CHECK (delivery_mode IN ('ONSITE', 'ONLINE'));