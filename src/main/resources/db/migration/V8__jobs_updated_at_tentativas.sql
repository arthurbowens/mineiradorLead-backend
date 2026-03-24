ALTER TABLE jobs ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE jobs ADD COLUMN tentativas INT NOT NULL DEFAULT 0;

UPDATE jobs SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE jobs ALTER COLUMN updated_at SET NOT NULL;
