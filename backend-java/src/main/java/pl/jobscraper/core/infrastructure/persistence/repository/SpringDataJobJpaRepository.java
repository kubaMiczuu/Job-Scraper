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

    /**
     * Retrieves all jobs from the database ordered by their publication date in ascending order.
     * <p>
     * This query provides a chronological view of all processed jobs, starting from the oldest.
     * The secondary sort by {@code id} ensures a deterministic order for jobs published
     * at the exact same time.
     *
     * <p><strong>Usage example:</strong>
     * <pre>{@code
     * // Fetch the first 50 oldest jobs
     * Pageable pageable = PageRequest.of(0, 50);
     * List<JobEntity> jobs = repo.findAllJobsOrderedOldestFirst(pageable);
     * }</pre>
     *
     * @param pageable pagination and sorting information (e.g., page number and size)
     * @return a list of all job entities, ordered from oldest to newest
     */
    @Query("SELECT j FROM JobEntity j ORDER BY j.publishedDate ASC, j.id ASC")
    List<JobEntity> findAllJobsOrderedOldestFirst(Pageable pageable);

    /**
     * Finds NEW jobs with dynamic filters using a native PostgreSQL query.
     * <p>
     * This method handles:
     * <ul>
     * <li>Case-insensitive partial match for location (ILIKE)</li>
     * <li>Exact match for seniority level</li>
     * <li>Keyword matching within the PostgreSQL text array (tech_keywords)</li>
     * </ul>
     *
     * @param location  optional location filter (partial string, null to skip)
     * @param seniority optional seniority level (exact string match, null to skip)
     * @param keywords  optional array of keywords (matches if any database tag exists in this array)
     * @param limit     maximum number of records to return
     * @param offset    number of records to skip (pagination)
     * @return list of {@link JobEntity} matching all provided criteria
     */
    @Query(value = """
        SELECT * FROM jobs j
        WHERE j.state = 'NEW'
            AND (CAST(:location AS VARCHAR) IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', CAST(:location AS VARCHAR), '%')))
            AND (CAST(:seniority AS VARCHAR) IS NULL OR j.seniority = CAST(:seniority AS VARCHAR))
            AND (
                COALESCE(ARRAY_LENGTH(CAST(:keywords AS TEXT[]), 1), 0) = 0
                OR EXISTS (
                         SELECT 1 FROM UNNEST(j.tech_keywords) AS keyword
                         WHERE LOWER(keyword) = ANY(
                             SELECT LOWER(kw) FROM UNNEST(CAST(:keywords AS TEXT[])) AS kw
                         )
                     )
            )
            ORDER BY j.entered_new_at ASC, j.id ASC
            LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<JobEntity> findNewJobsWithFilters(
            @Param("location") String location,
            @Param("seniority") String seniority,
            @Param("keywords") String[] keywords,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
