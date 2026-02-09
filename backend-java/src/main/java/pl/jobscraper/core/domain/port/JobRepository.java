package pl.jobscraper.core.domain.port;

import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for job persistence operations.
 * <p>
 * This interface is defined by the DOMAIN layer (not infrastructure),
 * following the Dependency Inversion Principle (SOLID).
 * Infrastructure provides the implementation (adapter).
 *
 * <p><strong>Design pattern:</strong> Hexagonal Architecture (Ports & Adapters)
 * <ul>
 *   <li><strong>Port (this interface):</strong> Defined by domain, describes what domain needs</li>
 *   <li><strong>Adapter (JobRepositoryImpl):</strong> Defined by infrastructure, provides implementation</li>
 * </ul>
 *
 * <p><strong>Why interface in domain?</strong>
 * <ul>
 *   <li>Domain doesn't depend on infrastructure (JPA, Spring Data, SQL)</li>
 *   <li>Easy to mock in tests (no need to spin up database)</li>
 *   <li>Can swap implementation (PostgreSQL → MongoDB) without touching domain</li>
 *   <li>Follows Dependency Inversion: high-level policy (domain) doesn't depend on low-level details (JPA)</li>
 * </ul>
 *
 * <p><strong>Return types - domain vs persistence:</strong>
 * Methods returning data to domain return {@link Job} (domain model).
 * Methods used for lookups/checks may return {@link JobEntity} (convenience for implementation).
 *
 * <p><strong>Timestamp responsibility:</strong>
 * All timestamp parameters ({@code Instant now}) are passed from caller (typically via {@link ClockPort}).
 * Repository doesn't call {@code Instant.now()} directly - enables deterministic testing.
 *
 * @see pl.jobscraper.core.infrastructure.persistence.repository.JobRepositoryImpl
 * @see Job
 * @see JobIdentity
 */
public interface JobRepository {

    /**
     * Saves new job posting (INSERT with state=NEW).
     * <p>
     * This method is called when a job is encountered for the first time
     * during ingestion. It sets:
     * <ul>
     *   <li>state = NEW</li>
     *   <li>entered_new_at = now (for queue sorting and TTL)</li>
     *   <li>created_at = updated_at = state_changed_at = now</li>
     *   <li>identity (canonical_url OR fallback_hash from JobIdentity)</li>
     * </ul>
     *
     * <p><strong>Idempotency note:</strong>
     * Caller should check existence via {@link #findByIdentity(JobIdentity)}
     * before calling this method to avoid duplicate key violations.
     *
     * <p><strong>Example usage:</strong>
     * <pre>{@code
     * JobIdentity identity = calculator.calculate(job);
     * Optional<JobEntity> existing = repository.findByIdentity(identity);
     *
     * if (existing.isEmpty()) {
     *     repository.saveNew(job, identity, clock.now());  // INSERT
     * } else {
     *     repository.updateExisting(existing.get().getId(), job, clock.now());  // UPDATE
     * }
     * }</pre>
     *
     * @param job domain job (business data)
     * @param identity calculated job identity (URL or hash-based)
     * @param now current timestamp (from ClockPort) for all timestamp fields
     * @throws org.springframework.dao.DataIntegrityViolationException if duplicate identity (should not happen if caller checks first)
     */
    void saveNew(Job job, JobIdentity identity, Instant now);

    /**
     * Updates existing job posting (UPDATE without re-promoting to NEW).
     * <p>
     * Updates only business fields from Job. Does NOT change:
     * <ul>
     *   <li>state (remains as-is: NEW/CONSUMED/STALE)</li>
     *   <li>entered_new_at (NOT reset - update doesn't re-promote to NEW)</li>
     *   <li>created_at (immutable)</li>
     *   <li>identity fields (canonical_url/fallback_hash are immutable)</li>
     * </ul>
     *
     * <p>Updates:
     * <ul>
     *   <li>Business fields: title, company, location, url, salary, etc.</li>
     *   <li>updated_at = now</li>
     * </ul>
     *
     * <p><strong>Critical contract rule:</strong>
     * Update does NOT re-promote job to NEW state. This ensures:
     * <ul>
     *   <li>Notifier sees each job exactly once (no duplicate notifications)</li>
     *   <li>Queue stability (jobs don't "jump back" into NEW state)</li>
     *   <li>Deterministic behavior (same input always produces same state transitions)</li>
     * </ul>
     *
     * @param id database ID of existing job
     * @param job updated domain job (source of new business field values)
     * @param now current timestamp (for updated_at)
     * @throws IllegalArgumentException if job with given ID doesn't exist
     */
    void updateExisting(UUID id, Job job, Instant now);

    /**
     * Finds job by identity (for deduplication during ingest).
     * <p>
     * Supports both identity types:
     * <ul>
     *   <li>URL-based: looks up by canonical_url</li>
     *   <li>Hash-based: looks up by fallback_hash</li>
     * </ul>
     *
     * <p>Uses unique indexes for fast lookup:
     * <ul>
     *   <li>idx_jobs_canonical_url (for URL-based)</li>
     *   <li>idx_jobs_fallback_hash (for hash-based)</li>
     * </ul>
     *
     * <p><strong>Usage in deduplication:</strong>
     * <pre>{@code
     * JobIdentity identity = calculator.calculate(job);
     * Optional<JobEntity> existing = repository.findByIdentity(identity);
     *
     * if (existing.isPresent()) {
     *     // Job already exists - UPDATE
     *     repository.updateExisting(existing.get().getId(), job, now);
     * } else {
     *     // New job - INSERT
     *     repository.saveNew(job, identity, now);
     * }
     * }</pre>
     *
     * @param identity job identity (URL or hash-based)
     * @return Optional containing JobEntity if found, empty if not exists
     */
    Optional<JobEntity> findByIdentity(JobIdentity identity);

    /**
     * Fetches NEW jobs ordered oldest first (for notification queue).
     * <p>
     * Returns JobEntity (not Job) because controller needs metadata (id, enteredNewAt).
     * Controller will map entity → Job → JobViewDto.
     *
     * @param limit maximum number of jobs to return
     * @return list of JobEntity (oldest NEW first), up to limit
     */
    List<JobEntity> fetchNewOldestFirst(int limit);

    /**
     * Marks jobs as CONSUMED (NEW → CONSUMED transition).
     * <p>
     * Called by Notifier after successfully sending notifications.
     * Implements read-then-mark pattern for at-least-once delivery guarantee.
     *
     * <p><strong>State transition:</strong>
     * <pre>
     * NEW → CONSUMED
     * </pre>
     *
     * <p><strong>Updates:</strong>
     * <ul>
     *   <li>state = CONSUMED</li>
     *   <li>state_changed_at = now</li>
     *   <li>entered_new_at = NULL (no longer in NEW queue)</li>
     * </ul>
     *
     * <p><strong>Idempotency:</strong>
     * If job is already CONSUMED, operation succeeds (no error).
     * If job is STALE, operation is ignored (cannot consume stale jobs).
     * Only NEW → CONSUMED transition is performed.
     *
     * <p><strong>Read-then-mark flow:</strong>
     * <pre>{@code
     * // 1. Notifier fetches jobs
     * List<Job> jobs = repository.fetchNewOldestFirst(100);
     *
     * // 2. Notifier processes and sends notifications
     * notificationService.send(jobs);
     *
     * // 3. Notifier marks as consumed (after success)
     * List<UUID> ids = jobs.stream().map(Job::getId).toList();
     * repository.markConsumed(ids, clock.now());
     * }</pre>
     *
     * @param ids list of job IDs to mark as consumed
     * @param now current timestamp (for state_changed_at)
     */
    void markConsumed(List<UUID> ids, Instant now);

    /**
     * Marks jobs as STALE (NEW → STALE transition, TTL cleanup).
     * <p>
     * Called by scheduled task to move jobs that have been in NEW state
     * for more than 7 days (TTL expired).
     *
     * <p><strong>State transition:</strong>
     * <pre>
     * NEW (>7 days) → STALE
     * </pre>
     *
     * <p><strong>Updates:</strong>
     * <ul>
     *   <li>state = STALE</li>
     *   <li>state_changed_at = now</li>
     *   <li>entered_new_at = NULL (no longer in NEW queue)</li>
     * </ul>
     *
     * <p><strong>Purpose:</strong>
     * Prevents NEW queue from growing indefinitely if Notifier is down.
     * After 7 days, old jobs are automatically archived to STALE state.
     *
     * <p><strong>Usage in scheduled task:</strong>
     * <pre>{@code
     * @Scheduled(cron = "0 0 3 * * *")  // Daily at 3 AM
     * public void cleanupStaleJobs() {
     *     Instant now = clock.now();
     *     Instant cutoff = now.minus(Duration.ofDays(7));
     *
     *     // Find NEW jobs older than 7 days
     *     List<JobEntity> staleJobs = jpaRepo.findStaleJobs(cutoff);
     *     List<UUID> ids = staleJobs.stream().map(JobEntity::getId).toList();
     *
     *     // Mark as STALE
     *     repository.markStale(ids, now);
     * }
     * }</pre>
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
}