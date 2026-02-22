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
 * Spring Data JPA abstraction for {@link JobEntity} persistence.
 * <p>
 * Leverages native PostgreSQL features for efficient filtering and sorting,
 * including array operations and regular expressions for salary parsing.
 *
 * @see JpaRepository
 * @see JobEntity
 */
public interface SpringDataJobJpaRepository extends JpaRepository<JobEntity, UUID> {

    Optional<JobEntity> findByCanonicalUrl(String canonicalUrl);

    Optional<JobEntity> findByFallbackHash(String fallbackHash);

    /**
     * Retrieves jobs in the NEW state using a FIFO (First-In-First-Out) strategy.
     *
     * @param pageable pagination and sorting parameters (limit, offset)
     * @return list of NEW jobs, oldest first (deterministic order)
     */
    @Query("SELECT j FROM JobEntity j " + "WHERE j.state = 'NEW' " + "ORDER BY j.enteredNewAt ASC, j.id ASC")
    List<JobEntity> findNewJobsOrderedOldestFirst(Pageable pageable);

    /**
     * Finds jobs in the NEW state that have exceeded the time-to-live (TTL) threshold.
     *
     * @param cutoff timestamp threshold (jobs older than this are stale)
     * @return list of NEW jobs older than cutoff
     */
    @Query("SELECT j FROM JobEntity j " + "WHERE j.state = 'NEW' AND j.enteredNewAt < :cutoff")
    List<JobEntity> findStaleJobs(@Param("cutoff") Instant cutoff);

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
    List<JobEntity> findNewJobsWithFilters(@Param("location") String location,@Param("seniority") String seniority, @Param("keywords") String[] keywords, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * Universal query for all jobs with optional seniority filter and sorting.
     * <p>
     * Supports:
     * - Optional seniority filter (null = all seniority)
     * - Dynamic sorting by any field OR numeric salary sorting
     * - Ascending order
     *
     * @param seniorities   optional seniority filter
     * @param employmentTypes optional employment filter
     * @param locations optional location filter
     * @param sources optional source filter
     * @param sortField  field to sort by (e.g., "company", "title", "published_date")
     * @param useSalarySort if true, uses numeric salary extraction; sortField is ignored
     * @param limit      max results
     * @param offset     pagination offset
     * @return list of JobEntity matching criteria, sorted ascending
     */
    @Query(value = """
        SELECT * FROM jobs\s
        WHERE 1=1
            AND (COALESCE(ARRAY_LENGTH(CAST(:seniorities AS TEXT[]),1),0) = 0 OR seniority = ANY(CAST(:seniorities AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:employmentTypes AS TEXT[]),1),0) = 0 OR employment_type = ANY(CAST(:employmentTypes AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:locations AS TEXT[]),1),0) = 0 OR location ILIKE ANY(CAST(:locations AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:sources AS TEXT[]),1),0) = 0 OR source = ANY(CAST(:sources AS TEXT[])))
        ORDER BY
            CASE
                WHEN :useSalarySort = true THEN
                    CASE
                        WHEN salary IS NULL OR salary !~ '^[0-9]' THEN 2147483647
                        ELSE CAST(substring(salary FROM '^[0-9]+') AS INTEGER)
                    END
                ELSE NULL
            END ASC,
            CASE WHEN :useSalarySort = false THEN
                CASE :sortField
                    WHEN 'company' THEN company
                    WHEN 'salary' THEN salary
                    WHEN 'publishedDate' THEN CAST(published_date AS TEXT)
                END
            END ASC,
            id ASC
        LIMIT :limit OFFSET :offset""", nativeQuery = true)
    List<JobEntity> findJobsUniversalAsc(
            @Param("seniorities") Object[] seniorities,
            @Param("employmentTypes") Object[] employmentTypes,
            @Param("locations") String[] locations,
            @Param("sources") String[] sources,
            @Param("sortField") String sortField,
            @Param("useSalarySort") boolean useSalarySort ,
            @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
        SELECT * FROM jobs\s
        WHERE 1=1
            AND (COALESCE(ARRAY_LENGTH(CAST(:seniorities AS TEXT[]),1),0) = 0 OR seniority = ANY(CAST(:seniorities AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:employmentTypes AS TEXT[]),1),0) = 0 OR employment_type = ANY(CAST(:employmentTypes AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:locations AS TEXT[]),1),0) = 0 OR location ILIKE ANY(CAST(:locations AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:sources AS TEXT[]),1),0) = 0 OR source = ANY(CAST(:sources AS TEXT[])))
        ORDER BY
            CASE
                WHEN :useSalarySort = true THEN
                    CASE
                        WHEN salary IS NULL OR salary !~ '^[0-9]' THEN 2147483647
                        ELSE CAST(substring(salary FROM '^[0-9]+') AS INTEGER)
                    END
                ELSE NULL
            END DESC,
            CASE WHEN :useSalarySort = false THEN
                CASE :sortField
                    WHEN 'company' THEN company
                    WHEN 'salary' THEN salary
                    WHEN 'publishedDate' THEN CAST(published_date AS TEXT)
                END
            END DESC,
            id ASC
        LIMIT :limit OFFSET :offset""", nativeQuery = true)
    List<JobEntity> findJobsUniversalDesc(
            @Param("seniorities") Object[] seniorities,
            @Param("employmentTypes") Object[] employmentTypes,
            @Param("locations") String[] locations,
            @Param("sources") String[] sources,
            @Param("sortField") String sortField,
            @Param("useSalarySort") boolean useSalarySort ,
            @Param("limit") int limit, @Param("offset") int offset);

    /**
     * Counts jobs matching filters.
     *
     * @param seniorities      optional seniority filter
     * @param employmentTypes optional employment type filter
     * @param locations       optional location filter (partial match)
     * @param sources         optional source filter
     * @return count of jobs matching all filters
     */
    @Query(value = """
        SELECT COUNT(*) FROM jobs\s
        WHERE 1=1
            AND (COALESCE(ARRAY_LENGTH(CAST(:seniorities AS TEXT[]),1),0) = 0 OR seniority = ANY(CAST(:seniorities AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:employmentTypes AS TEXT[]),1),0) = 0 OR employment_type = ANY(CAST(:employmentTypes AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:locations AS TEXT[]),1),0) = 0 OR location ILIKE ANY(CAST(:locations AS TEXT[])))
            AND (COALESCE(ARRAY_LENGTH(CAST(:sources AS TEXT[]),1),0) = 0 OR source = ANY(CAST(:sources AS TEXT[])))
   \s""", nativeQuery = true)
    long countWithFilters(
            @Param("seniorities") String[] seniorities,
            @Param("employmentTypes") String[] employmentTypes,
            @Param("locations") String[] locations,
            @Param("sources") String[] sources
    );
}