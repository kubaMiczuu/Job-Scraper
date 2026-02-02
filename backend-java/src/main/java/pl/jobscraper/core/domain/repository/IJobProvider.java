package pl.jobscraper.core.domain.repository;

import pl.jobscraper.core.domain.model.Job;

import java.util.List;

/**
 * Common interface for job data providers.
 * Defines the standard operations for retrieving job postings from various
 * external sources or internal databases.
 */
public interface IJobProvider {

    /**
     * Retrieves a list of recently discovered job postings.
     * These are typically jobs found since the last synchronization or
     * identified as "new" based on specific provider criteria.
     *
     * @return A {@link List} of {@link Job} objects representing new offers.
     */
    List<Job> getNewJobs();

    /**
     * Retrieves all available job postings managed by this provider.
     * This may include both new and previously processed jobs.
     *
     * @return A {@link List} of all {@link Job} objects currently stored.
     */
    List<Job> getAllJobs();
}
