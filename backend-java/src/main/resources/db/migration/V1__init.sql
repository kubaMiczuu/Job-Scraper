-- ============================================================================
-- Job Scraper & Alert System - Data Layer
-- Migration V1: Initialize schema (PostgreSQL)
-- ============================================================================

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Table: jobs
-- Stores job posting with identity (canonical URL or hash), state, and metadata
CREATE TABLE jobs(
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Required business fields
    title VARCHAR(500) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    published_date TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Optional business fields
    source VARCHAR(100),
    seniority VARCHAR(50),
    employment_type VARCHAR(50),
    tech_keywords TEXT[],
    salary VARCHAR(255),
    description_snippet TEXT,
    --Identity fields (XOR constraint: exactly one must be NOT NULL)
    canonical_url TEXT,
    fallback_hash CHAR(64),
    -- State managment
    state VARCHAR(20) NOT NULL DEFAULT 'NEW'
          CHECK (state IN ('NEW', 'CONSUMED', 'STALE')),
    -- Technical metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    state_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entered_new_at TIMESTAMP WITH TIME ZONE,
    -- CONSTRAINTS
    CONSTRAINT chk_identity_xor CHECK (
            (canonical_url IS NOT NULL AND fallback_hash IS NULL) or
            (canonical_url IS NULL AND fallback_hash IS NOT NULL)
        )
    );

-- ============================================================================
-- Indexes for performance
-- ============================================================================

-- Unique index on canonical_url (deduplication during ingest)
CREATE UNIQUE INDEX idx_jobs_canonical_url
    ON jobs(canonical_url)
    WHERE canonical_url IS NOT NULL;
-- Unique index on fallback_hash (deduplication during ingest)
CREATE UNIQUE INDEX idx_jobs_fallback_hash
    ON jobs(fallback_hash)
    WHERE fallback_hash IS NOT NULL;
-- Index on state (filtering by state: NEW, CONSUMED, STALE)
CREATE INDEX idx_jobs_state
    ON jobs(state);
-- Compound index for NEW jobs queue (sorting by oldest first)
CREATE INDEX idx_jobs_new_queue
    ON jobs(entered_new_at, id)
    WHERE state = 'NEW';

-- ============================================================================
-- Comments (documentation in database)
-- ============================================================================
COMMENT ON TABLE jobs IS 'Job postings with deduplication, state management, and metadata';
COMMENT ON COLUMN jobs.canonical_url IS 'Normalized URL (identity method 1)';
COMMENT ON COLUMN jobs.fallback_hash IS 'SHA-256 hash of company+title+location (identity method 2, fallback when URL invalid)';
COMMENT ON COLUMN jobs.state IS 'Job state: NEW (ready for notification), CONSUMED (processed), STALE (expired after 7 days)';
COMMENT ON COLUMN jobs.entered_new_at IS 'When job entered NEW state (used for queue sorting and TTL); NULL for non-NEW jobs';