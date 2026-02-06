package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

/**
 * Application service for fetching NEW jobs queue.
 * <p>
 * Implements the use case: "Fetch NEW jobs for Notifier in deterministic order".
 * This is the read side of the read-then-mark pattern.
 *
 * <p><strong>Use case flow (GET /api/jobs/new):</strong>
 * <pre>
 * Notifier → GET /api/jobs/new?limit=100
 *   ↓
 * 1. Fetch NEW jobs from database (oldest first)
 * 2. Return domain Jobs to controller
 * 3. Controller maps to DTOs for JSON response
 * </pre>
 *
 * <p><strong>Queue ordering:</strong>
 * Jobs are returned in FIFO order (First In, First Out):
 * <ul>
 *   <li>Primary sort: entered_new_at ASC (oldest jobs first)</li>
 *   <li>Secondary sort: id ASC (deterministic when timestamps equal)</li>
 * </ul>
 *
 * <p><strong>Deterministic ordering importance:</strong>
 * Stable order prevents "disappearing items" effect when Notifier
 * fetches multiple times. Same query always returns same jobs in same order.
 *
 * <p><strong>Read-then-mark pattern:</strong>
 * <pre>
 * 1. Notifier: GET /api/jobs/new?limit=100
 * 2. Notifier: processes jobs (sends notifications)
 * 3. Notifier: POST /api/jobs/mark-consumed (list of IDs)
 *
 * If Notifier crashes between step 2 and 3:
 * - Jobs remain in NEW state
 * - Next GET will return same jobs again (at-least-once delivery)
 * </pre>
 *
 * <p><strong>No business logic:</strong>
 * This service is a thin wrapper around repository. All logic
 * (filtering, sorting) is in persistence layer via SQL.
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
     * <p><strong>Sorting:</strong>
     * <ul>
     *   <li>Primary: entered_new_at ASC (oldest NEW jobs first)</li>
     *   <li>Secondary: id ASC (deterministic tie-breaker)</li>
     * </ul>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * // Fetch up to 100 oldest NEW jobs
     * List<Job> jobs = service.fetchNew(100);
     *
     * // Jobs are in FIFO order:
     * // Job 1: entered_new_at = 2026-02-01T10:00:00Z
     * // Job 2: entered_new_at = 2026-02-02T11:00:00Z
     * // Job 3: entered_new_at = 2026-02-03T12:00:00Z
     * }</pre>
     *
     * <p><strong>State unchanged:</strong>
     * Jobs remain in NEW state. Only mark-consumed changes state.
     *
     * @param limit maximum number of jobs to return
     * @return list of domain Jobs (oldest NEW first), up to limit
     */
    public List<JobEntity> fetchNew(int limit) {
        return repository.fetchNewOldestFirst(limit);
    }
}
