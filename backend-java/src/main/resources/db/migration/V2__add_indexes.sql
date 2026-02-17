-- V2: Add performance indexes for filter queries
-- These indexes support GET /api/jobs/new filtering:
-- ?seniority=SENIOR and ?location=remote

-- Index for seniority filter (exact match)
CREATE INDEX idx_jobs_seniority
    ON jobs(seniority)
    WHERE seniority IS NOT NULL;

-- Index for location filter (text search via LIKE)
CREATE INDEX idx_jobs_location
    ON jobs(location);