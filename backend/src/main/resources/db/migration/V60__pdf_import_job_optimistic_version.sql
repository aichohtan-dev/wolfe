-- Prevent a stale PDF worker entity from resurrecting a job that the recovery scheduler
-- has already marked FAILED. The @Version column turns scheduler/worker races into
-- an explicit optimistic-lock failure instead of allowing a lost update.
ALTER TABLE pdf_import_jobs
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
