CREATE TABLE extract_jobs (
    id UUID PRIMARY KEY,
    search_history_id UUID NOT NULL REFERENCES search_histories (id) ON DELETE CASCADE,
    max_results INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT extract_jobs_search_history_id_key UNIQUE (search_history_id)
);

CREATE INDEX idx_extract_jobs_status_created ON extract_jobs (status, created_at);
