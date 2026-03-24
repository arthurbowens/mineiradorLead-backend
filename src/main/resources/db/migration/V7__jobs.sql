CREATE TABLE jobs (
    id VARCHAR(64) PRIMARY KEY,
    tipo VARCHAR(255) NOT NULL DEFAULT '',
    payload TEXT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_jobs_status_created ON jobs (status, created_at);
