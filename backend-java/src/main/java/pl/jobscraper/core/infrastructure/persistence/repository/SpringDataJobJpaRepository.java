package pl.jobscraper.core.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link JobEntity}.
 * <p>
 * This interface extends {@link JpaRepository}, which provides automatic
 * implementation of common CRUD operations. Spring Data generates the
 * implementation at runtime - no need to write SQL or implementation classes!
 *
 * <p><strong>Auto-implemented methods (from JpaRepository):</strong>
 * <ul>
 *   <li>{@code save(JobEntity)} - INSERT or UPDATE (merge)</li>
 *   <li>{@code findById(UUID)} - SELECT by primary key</li>
 *   <li>{@code findAll()} - SELECT all records</li>
 *   <li>{@code delete(JobEntity)} - DELETE by entity</li>
 *   <li>{@code count()} - COUNT(*)</li>
 *   <li>{@code existsById(UUID)} - check if exists</li>
 *   <li>and 20+ more methods!</li>
 * </ul>
 *
 * <p><strong>Method name query derivation:</strong>
 * Spring Data parses method names and generates SQL automatically.
 * Example: {@code findByCanonicalUrl(String url)} generates:
 * <pre>
 * SELECT j FROM JobEntity j WHERE j.canonicalUrl = :url
 * </pre>
 *
 * <p><strong>Custom queries:</strong>
 * For complex queries that can't be expressed by method names, use
 * {@code @Query} annotation with JPQL (Java Persistence Query Language).
 *
 * <p><strong>Why interface, not class?</strong>
 * Spring Data creates a proxy implementation at runtime (dynamic proxy pattern).
 * You define the contract (interface), Spring provides the implementation.
 *
 * <p><strong>Usage (automatic via Spring):</strong>
 * <pre>{@code
 * @Repository
 * public class JobRepositoryImpl {
 *     private final SpringDataJobJpaRepository jpaRepo;  // Auto-injected
 *
 *     public Optional<JobEntity> findByUrl(String url) {
 *         return jpaRepo.findByCanonicalUrl(url);  // Spring-generated code!
 *     }
 * }
 * }</pre>
 *
 * @see JpaRepository
 * @see JobEntity
 */
public interface SpringDataJobJpaRepository extends JpaRepository<JobEntity, UUID> {

    /**
     * Finds job by canonical URL (URL-based identity).
     * <p>
     * <strong>Generated SQL:</strong>
     * <pre>
     * SELECT * FROM jobs WHERE canonical_url = ?
     * </pre>
     *
     * <p>Uses unique index {@code idx_jobs_canonical_url} for fast lookup.
     *
     * @param canonicalUrl canonical URL to search for
     * @return Optional containing job entity if found, empty otherwise
     */
    Optional<JobEntity> findByCanonicalUrl(String canonicalUrl);

    /**
     * Finds job by fallback hash (hash-based identity).
     * <p>
     * <strong>Generated SQL:</strong>
     * <pre>
     * SELECT * FROM jobs WHERE fallback_hash = ?
     * </pre>
     *
     * <p>Uses unique index {@code idx_jobs_fallback_hash} for fast lookup.
     *
     * @param fallbackHash SHA-256 hash (64 hex characters)
     * @return Optional containing job entity if found, empty otherwise
     */
    Optional<JobEntity> findByFallbackHash(String fallbackHash);

    /**
     * Finds NEW jobs ordered oldest first (for notification queue).
     * <p>
     * This is the core query for {@code GET /api/jobs/new} endpoint.
     * Returns jobs in deterministic order (FIFO - First In, First Out).
     *
     * <p><strong>Query explanation:</strong>
     * <ul>
     *   <li>Filter: {@code state = 'NEW'}</li>
     *   <li>Sort: {@code entered_new_at ASC, id ASC} (oldest first)</li>
     *   <li>Why id as secondary sort? Ensures deterministic order when
     *       multiple jobs have same entered_new_at timestamp</li>
     * </ul>
     *
     * <p><strong>Index usage:</strong>
     * Uses compound index {@code idx_jobs_new_queue (entered_new_at, id)}
     * for efficient querying and sorting.
     *
     * <p><strong>Pagination:</strong>
     * Use {@link Pageable} to limit results:
     * <pre>{@code
     * Pageable pageable = PageRequest.of(0, 100);  // First 100 jobs
     * List<JobEntity> jobs = repo.findNewJobsOrderedOldestFirst(pageable);
     * }</pre>
     *
     * @param pageable pagination and sorting parameters (limit, offset)
     * @return list of NEW jobs, oldest first (deterministic order)
     */
    @Query("SELECT j FROM JobEntity j " + "WHERE j.state = 'NEW' " + "ORDER BY j.enteredNewAt ASC, j.id ASC")
    List<JobEntity> findNewJobsOrderedOldestFirst(Pageable pageable);

    /**
     * Finds NEW jobs older than cutoff time (for TTL cleanup).
     * <p>
     * Used by scheduled task to identify stale jobs that have been
     * in NEW state for more than 7 days.
     *
     * <p><strong>Query:</strong>
     * <pre>
     * SELECT * FROM jobs
     * WHERE state = 'NEW' AND entered_new_at < :cutoff
     * </pre>
     *
     * <p><strong>Usage example:</strong>
     * <pre>{@code
     * Instant cutoff = clock.now().minus(Duration.ofDays(7));
     * List<JobEntity> staleJobs = repo.findStaleJobs(cutoff);
     * staleJobs.forEach(job -> job.setState(STALE));
     * }</pre>
     *
     * @param cutoff timestamp threshold (jobs older than this are stale)
     * @return list of NEW jobs older than cutoff
     */
    @Query("SELECT j FROM JobEntity j " + "WHERE j.state = 'NEW' AND j.enteredNewAt < :cutoff")
    List<JobEntity> findStaleJobs(@Param("cutoff") Instant cutoff);

    @Query("SELECT j FROM JobEntity j ORDER BY j.enteredNewAt ASC, j.id ASC")
    List<JobEntity> findAllJobsOrderedOldestFirst(Pageable pageable);
}
