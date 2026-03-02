ALTER TABLE jobs ADD COLUMN search_text TEXT;

UPDATE jobs
SET search_text = CONCAT_WS('',
    LOWER(title),
    LOWER(company),
    LOWER(ARRAY_TO_STRING(tech_keywords, ' ')));


COMMENT ON COLUMN jobs.search_text IS 'Combined searchable text (title, company, location, description, keywords)';

CREATE INDEX idx_jobs_search_text ON jobs USING GIN (to_tsvector('simple', search_text));