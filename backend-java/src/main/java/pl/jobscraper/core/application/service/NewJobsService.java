package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

/**
 * Provides access to the queue of jobs awaiting notification.
 * <p>
 * Implements a FIFO (First-In-First-Out) retrieval strategy to ensure
 * that the oldest unprocessed jobs are handled first.
 *
 * @see Job
 * @see JobRepository
 */
@Service
@Transactional
public class NewJobsService {

    private final JobRepository repository;

    /**
     * Constructor injection dependancies.
     *
     * @param repository persistence layer for job storage
     */
    public NewJobsService(JobRepository repository) {
        this.repository = repository;
    }

    /**
     * Fetches NEW jobs ordered oldest first (FIFO queue).
     * <p>
     * Returns jobs in deterministic order for stable queue behavior.
     * Jobs remain in NEW state after fetch (read-only operation).
     *
     * @param limit maximum number of jobs to return
     * @return list of domain Jobs (oldest NEW first), up to limit
     */
    public List<JobEntity> fetchNew(int limit) {
        return repository.fetchNewOldestFirst(limit);
    }

    /**
     * Fetches NEW jobs with optional filters (filtered queue).
     * <p>
     * Returns jobs matching filter criteria in FIFO order.
     *
     *
     * @param filter filter criteria (use JobFilter.none() for no filtering)
     * @param limit maximum number of jobs to return
     * @return list of JobEntity matching filters (oldest NEW first), up to limit
     */
    public List<JobEntity> fetchNewWithFilters(JobFilter filter, int limit, int offset) {
        return repository.fetchNewWithFilters(filter, limit, offset);
    }
}
