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
 * Orchestrates the ingestion of job postings from external sources.
 * <p>
 * This service implements the core idempotency logic:
 * <ul>
 * <li>Identifies duplicates within the incoming batch.</li>
 * <li>Performs a "Global Upsert": New jobs are promoted to NEW status,
 * while existing jobs are updated without state change.</li>
 * </ul>
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
     * Processes a batch of jobs using a "deduplicate-then-upsert" strategy.
     *
     * @param jobs batch of jobs from scraper (may contain duplicates)
     * @return aggregated results (counts of inserted, updated, skipped)
     */
    public IngestResult ingest(List<Job> jobs) {
        Instant now = clock.now();

        int inserted = 0;
        int updated = 0;
        int skippedDuplicates = 0;

        Map<JobIdentity, Job> uniqueJobs = new LinkedHashMap<>();

        for (Job job : jobs) {
            JobIdentity identity = identityCalculator.calculate(job);

            if (uniqueJobs.containsKey(identity)) {
                skippedDuplicates++;
            } else {
                uniqueJobs.put(identity, job);
            }
        }

        for (Map.Entry<JobIdentity, Job> entry : uniqueJobs.entrySet()) {
            JobIdentity identity = entry.getKey();
            Job job = entry.getValue();

            Optional<JobEntity> existing = repository.findByIdentity(identity);

            if (existing.isEmpty()) {
                repository.saveNew(job, identity, now);
                inserted++;
            } else {
                UUID existingId = existing.get().getId();
                repository.updateExisting(existingId, job, now);
                updated++;
            }
        }

        return new IngestResult(
                jobs.size(),           // received
                inserted,              // insertedNew
                updated,               // updatedExisting
                skippedDuplicates      // skippedDuplicates
        );
    }

    /**
     * Business summary of the ingestion process.
     * Invariant: {@code received == insertedNew + updatedExisting + skippedDuplicates}
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
