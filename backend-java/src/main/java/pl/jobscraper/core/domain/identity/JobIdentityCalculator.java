package pl.jobscraper.core.domain.identity;

import pl.jobscraper.core.domain.model.Job;

/**
 * Domain service for job deduplication.
 * <p>
 * Defines the contract for computing a deterministic {@link JobIdentity} from job data.
 * Implementations must ensure that identical job postings result in the same identity
 * to support idempotent ingestion.
 *
 * @see JobIdentity
 * @see NormalizationRules
 */
public interface JobIdentityCalculator {
    /**
     * Computes a unique identity for the given job posting.
     *
     * @param job the job posting to calculate identity for (must not be null)
     * @return unique JobIdentity (either URL-based or hash-based)
     * @throws IllegalArgumentException if job is null
     */
    JobIdentity calculate(Job job);
}