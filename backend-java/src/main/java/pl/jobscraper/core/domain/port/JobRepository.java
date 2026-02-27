package pl.jobscraper.core.domain.port;

import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Primary output port for job persistence and lifecycle management.
 * <p>
 * Decouples the domain logic from specific database technologies (JPA, SQL).
 * All timestamps are provided externally to ensure deterministic execution.
 *
 * @see pl.jobscraper.core.infrastructure.persistence.repository.JobRepositoryImpl
 * @see Job
 * @see JobIdentity
 */
public interface JobRepository {

    /**
     * Persists a newly discovered job in the NEW state.
     *
     * @param job domain job (business data)
     * @param identity calculated job identity (URL or hash-based)
     * @param now current timestamp (from ClockPort) for all timestamp fields
     * @param searchText searchable text for full-text search
     * @throws org.springframework.dao.DataIntegrityViolationException if duplicate identity (should not happen if caller checks first)
     */
    void saveNew(Job job, JobIdentity identity, Instant now, String searchText);

    /**
     * Updates business data for an existing job without affecting its current state.
     *
     * @param id database ID of existing job
     * @param job updated domain job (source of new business field values)
     * @param now current timestamp (for updated_at)
     * @param searchText updated searchable text
     * @throws IllegalArgumentException if job with given ID doesn't exist
     */
    void updateExisting(UUID id, Job job, Instant now, String searchText);

    /**
     * Retrieves an existing job by its identity for deduplication purposes.
     *
     * @param identity job identity (URL or hash-based)
     * @return Optional containing JobEntity if found, empty if not exists
     */
    Optional<JobEntity> findByIdentity(JobIdentity identity);

    /**
     * Retrieves jobs in the NEW state, ordered by entry time (oldest first).
     *
     * @param limit maximum number of jobs to return
     * @return list of JobEntity (oldest NEW first), up to limit
     */
    List<JobEntity> fetchNewOldestFirst(int limit);

    /**
     * Retrieves filtered jobs in the NEW state with support for pagination.
     *
     * @param filter filter criteria (use JobFilter.none() for no filtering)
     * @param limit maximum number of jobs to return
     * @return list of JobEntity matching filters (oldest NEW first), up to limit
     */
    List<JobEntity> fetchNewWithFilters(JobFilter filter, int limit, int offset);

    /**
     * Statistics from mark-consumed operation.
     *
     * @param marked         count of NEW → CONSUMED
     * @param alreadyConsumed count already CONSUMED
     * @param notFound       count of non-existent IDs
     */
    record ConsumptionStats(int marked, int alreadyConsumed, int notFound){}

    /**
     * Transitions a batch of jobs from NEW to CONSUMED state.
     * <p>Only transitions valid NEW jobs; already consumed or stale records are ignored.
     *
     * @param ids list of job IDs to mark as consumed
     * @param now current timestamp (for state_changed_at)
     */
    ConsumptionStats markConsumed(List<UUID> ids, Instant now);

    /**
     * Archives jobs that have exceeded their retention period (NEW -> STALE)
     *
     * @param ids list of job IDs to mark as stale
     * @param now current timestamp (for state_changed_at)
     */
    void markStale(List<UUID> ids, Instant now);

    /**
     * Finds stale jobs (NEW older than cutoff).
     *
     * @param cutoff timestamp cutoff
     * @return list of stale jobs
     */
    List<JobEntity> findStaleJobs(Instant cutoff);

    /**
     * Fetches all jobs with pagination support.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param seniorities optional seniority filter
     * @param employmentTypes optional employmentType filter
     * @param locations optional location filter
     * @param sources optional source filter
     * @param sortBy sort field name
     * @param sortOrder sort direction (ASC/DESC)
     * @return list of JobEntity for current page
     */
    List<JobEntity> fetchAllPaginated(int page, int size, Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] searchText, String sortBy, String sortOrder);

    /**
     * Counts all jobs (optionally filtered).
     *
     * @param seniorities optional seniority filter
     * @param employmentTypes optional employmentType filter
     * @param locations optional location filter
     * @param sources optional source filter
     * @return total count
     */
    long countAll(Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] searchText);

    List<String> findDistinctSeniorities();

    List<String> findDistinctEmploymentTypes();

    List<String> findDistinctLocations();

    List<String> findDistinctSources();

}