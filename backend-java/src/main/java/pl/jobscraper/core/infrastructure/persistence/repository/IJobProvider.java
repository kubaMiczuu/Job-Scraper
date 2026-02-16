package pl.jobscraper.core.infrastructure.persistence.repository;

import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

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
    List<JobEntity> getNewJobs(JobFilter filter);

    /**
     * Marks the specified list of job entities as processed or notified.
     * This method ensures that the provider records which jobs have already
     * been handled, preventing duplicate notifications.
     *
     * @param jobs A {@link List} of {@link JobEntity} objects that have been
     * successfully consumed or delivered to the user.
     */
    void makeConsumedNotifications(List<JobEntity> jobs);
}
