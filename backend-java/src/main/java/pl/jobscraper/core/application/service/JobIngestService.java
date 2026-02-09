package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.identity.JobIdentityCalculator;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.port.ClockPort;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.*;

/**
 * Application service for job batch ingestion.
 * <p>
 * Implements the core use case: "Ingest batch of job postings from scraper".
 * Handles deduplication (batch + global), upsert logic, and result aggregation.
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Orchestration: coordinates domain calculators + persistence repository</li>
 *   <li>Batch deduplication: removes duplicates within same batch</li>
 *   <li>Global deduplication: checks database for existing jobs</li>
 *   <li>Upsert logic: INSERT new jobs, UPDATE existing (without re-promotion)</li>
 *   <li>Aggregation: counts inserted, updated, skipped for response</li>
 * </ul>
 *
 * <p><strong>Use case flow (POST /api/jobs):</strong>
 * <pre>
 * Scraper → JSON batch → Controller → DTO validation → JobIngestService
 *   ↓
 * 1. Calculate identity for each job
 * 2. Remove batch duplicates (same identity in batch)
 * 3. For each unique job:
 *    - Lookup in DB by identity
 *    - If exists: UPDATE (without re-promoting to NEW)
 *    - If not exists: INSERT (state=NEW, entered_new_at=now)
 * 4. Return aggregates (inserted, updated, skipped)
 * </pre>
 *
 * <p><strong>Idempotency guarantee:</strong>
 * Same batch can be sent multiple times:
 * <ul>
 *   <li>First call: inserts new jobs → inserted count > 0</li>
 *   <li>Second call: updates existing jobs → updated count > 0, inserted = 0</li>
 *   <li>No duplicates created (identity-based deduplication)</li>
 * </ul>
 *
 * <p><strong>Transaction boundary:</strong>
 * Entire batch is processed in single transaction (all-or-nothing).
 * If any error occurs, entire batch is rolled back.
 *
 * @see Job
 * @see JobIdentityCalculator
 * @see JobRepository
 */
@Service
@Transactional
public class JobIngestService {

    private final JobRepository repository;
    private final JobIdentityCalculator identityCalculator;
    private final ClockPort clock;

    /**
     * Constructor injection of dependencies.
     *
     * @param repository         persistence layer for job storage
     * @param identityCalculator domain calculator for job identity
     * @param clock              time abstraction for timestamps
     */
    public JobIngestService(
            JobRepository repository,
            JobIdentityCalculator identityCalculator,
            ClockPort clock
    ) {
        this.repository = repository;
        this.identityCalculator = identityCalculator;
        this.clock = clock;
    }

    /**
     * Ingests batch of job postings with deduplication.
     * <p>
     * <strong>Algorithm:</strong>
     * <ol>
     *   <li>Calculate identity for each job</li>
     *   <li>Remove batch duplicates (same identity within batch)</li>
     *   <li>For each unique job:
     *     <ul>
     *       <li>Lookup in database by identity</li>
     *       <li>If not found: INSERT as NEW</li>
     *       <li>If found: UPDATE existing (no re-promotion to NEW)</li>
     *     </ul>
     *   </li>
     *   <li>Aggregate results (inserted, updated, skipped)</li>
     * </ol>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * Batch input: [jobA, jobB, jobA, jobC]  // jobA duplicated in batch
     *
     * Step 1: Calculate identities
     *   jobA → identity1
     *   jobB → identity2
     *   jobA → identity1  (duplicate!)
     *   jobC → identity3
     *
     * Step 2: Batch deduplication
     *   unique: [jobA (identity1), jobB (identity2), jobC (identity3)]
     *   skipped: 1 (duplicate jobA)
     *
     * Step 3: Global deduplication
     *   identity1 exists in DB → UPDATE jobA
     *   identity2 NOT in DB → INSERT jobB as NEW
     *   identity3 NOT in DB → INSERT jobC as NEW
     *
     * Result: inserted=2, updated=1, skipped=1
     * }</pre>
     *
     * <p><strong>Idempotency example:</strong>
     * <pre>{@code
     * // First call
     * ingest([jobA, jobB])
     * → inserted=2, updated=0, skipped=0
     *
     * // Second call (same batch)
     * ingest([jobA, jobB])
     * → inserted=0, updated=2, skipped=0  (jobs already exist)
     *
     * // Third call (same batch)
     * ingest([jobA, jobB])
     * → inserted=0, updated=2, skipped=0  (idempotent!)
     * }</pre>
     *
     * @param jobs batch of jobs from scraper (may contain duplicates)
     * @return aggregated results (counts of inserted, updated, skipped)
     */
    public IngestResult ingest(List<Job> jobs) {
        // Get current timestamp once (for all operations)
        Instant now = clock.now();

        // Result aggregates
        int inserted = 0;
        int updated = 0;
        int skippedDuplicates = 0;

        // Step 1: Calculate identity for each job + batch deduplication
        Map<JobIdentity, Job> uniqueJobs = new LinkedHashMap<>();  // Preserves insertion order

        for (Job job : jobs) {
            JobIdentity identity = identityCalculator.calculate(job);

            if (uniqueJobs.containsKey(identity)) {
                // Duplicate within batch - skip
                skippedDuplicates++;
            } else {
                // First occurrence - keep
                uniqueJobs.put(identity, job);
            }
        }

        // Step 2: Global deduplication + upsert
        for (Map.Entry<JobIdentity, Job> entry : uniqueJobs.entrySet()) {
            JobIdentity identity = entry.getKey();
            Job job = entry.getValue();

            // Lookup in database
            Optional<JobEntity> existing = repository.findByIdentity(identity);

            if (existing.isEmpty()) {
                // Not in database → INSERT as NEW
                repository.saveNew(job, identity, now);
                inserted++;
            } else {
                // Already in database → UPDATE (no re-promotion to NEW!)
                UUID existingId = existing.get().getId();
                repository.updateExisting(existingId, job, now);
                updated++;
            }
        }

        // Step 3: Return aggregated results
        return new IngestResult(
                jobs.size(),           // received
                inserted,              // insertedNew
                updated,               // updatedExisting
                skippedDuplicates      // skippedDuplicates
        );
    }

    /**
     * Result of batch ingestion operation.
     * <p>
     * Contains aggregated statistics about processed batch:
     * <ul>
     *   <li>received: total jobs in batch (including duplicates)</li>
     *   <li>insertedNew: how many new jobs were inserted (state=NEW)</li>
     *   <li>updatedExisting: how many existing jobs were updated (state unchanged)</li>
     *   <li>skippedDuplicates: how many duplicates were in batch (skipped)</li>
     * </ul>
     *
     * <p><strong>Invariant:</strong>
     * {@code received = insertedNew + updatedExisting + skippedDuplicates}
     *
     * @param received total jobs in batch
     * @param insertedNew count of newly inserted jobs
     * @param updatedExisting count of updated jobs
     * @param skippedDuplicates count of batch duplicates
     */
    public record IngestResult(
            int received,
            int insertedNew,
            int updatedExisting,
            int skippedDuplicates
    ) {
        /**
         * Validates invariant: received = inserted + updated + skipped.
         *
         * @throws IllegalArgumentException if invariant violated
         */
        public IngestResult {
            if (received != insertedNew + updatedExisting + skippedDuplicates) {
                throw new IllegalArgumentException(
                        "IngestResult invariant violated: received != inserted + updated + skipped"
                );
            }
        }
    }
}
