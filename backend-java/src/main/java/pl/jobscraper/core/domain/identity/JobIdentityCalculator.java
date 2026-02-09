package pl.jobscraper.core.domain.identity;

import pl.jobscraper.core.domain.model.Job;

/**
 * Calculator for determining unique identity of job postings.
 * <p>
 * This is the domain's primary deduplication mechanism. The calculator
 * decides whether two job postings represent the same job offer.
 * <p>
 * <strong>Strategy (as per contract v1.0):</strong>
 * <ol>
 *     <li>Try to canonicalize the job's URL</li>
 *     <li>If successful -> use canonical URL as identity</li>
 *     <li>If URL invalid/missing -> compute fallback hash from company, title, location</li>
 * </ol>
 * This ensures deterministic identity calculation critical for idempotent ingestion.
 *
 * @see JobIdentity
 * @see NormalizationRules
 */
public interface JobIdentityCalculator {
    /**
     * Calculates unique identity for a job posting.
     * <p>
     * This method is deterministic - same Job data always produces
     * the same JobIdentity. This property is essential for deduplication
     * and idempotent batch ingestion.
     * @param job the job posting to calculate identity for (must not be null)
     * @return unique JobIdentity (either URL-based or hash-based)
     * @throws IllegalArgumentException if job is null
     */
    JobIdentity calculate(Job job);
}