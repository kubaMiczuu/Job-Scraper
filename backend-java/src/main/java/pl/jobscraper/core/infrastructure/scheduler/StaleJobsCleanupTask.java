package pl.jobscraper.core.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.policy.TtlPolicy;
import pl.jobscraper.core.domain.port.ClockPort;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled task for cleaning up stale jobs.
 * <p>
 * Runs daily to mark NEW jobs older than 7 days as STALE.
 * This prevents system from accumulating old unprocessed jobs forever.
 *
 * <p><strong>Schedule:</strong>
 * Daily at 3:00 AM (server time).
 * Low-traffic hour to minimize impact on production.
 *
 * <p><strong>Algorithm:</strong>
 * <pre>
 * 1. Get current time: now = clock.now()
 * 2. Calculate cutoff: cutoff = now - 7 days
 * 3. Find stale jobs: SELECT * FROM jobs
 *                     WHERE state='NEW' AND entered_new_at < cutoff
 * 4. Mark as STALE: UPDATE jobs SET state='STALE' WHERE id IN (...)
 * 5. Log result: "Marked X jobs as STALE"
 * </pre>
 *
 * <p><strong>State transition:</strong>
 * <pre>
 * NEW (>7 days) → STALE
 * </pre>
 *
 * <p><strong>Example scenario:</strong>
 * <pre>
 * Job entered NEW: 2026-01-27 10:00
 * Cleanup runs:    2026-02-04 03:00
 * Age: 8 days > 7 days TTL → marked STALE
 * </pre>
 *
 * <p><strong>Why this is needed:</strong>
 * If Notifier is down for >7 days, jobs accumulate in NEW state.
 * Without cleanup, queue grows unbounded. TTL keeps system healthy.
 *
 * @see TtlPolicy
 */
@Component
public class StaleJobsCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(StaleJobsCleanupTask.class);

    private final JobRepository repository;
    private final ClockPort clock;

    /**
     * Constructor injection of dependencies.
     *
     * @param repository persistence layer for job storage
     * @param clock time abstraction for deterministic testing
     */
    public StaleJobsCleanupTask(JobRepository repository, ClockPort clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupStaleJobs() {
        logger.info("Starting Stale Jobs Cleanup Task");

        try{
            Instant now = clock.now();

            Instant cutoff = TtlPolicy.staleCutoff(now);

            logger.debug("Cutoff timestamp: {} (jobs older than this will be marked STALE)", cutoff);

            List<JobEntity> staleJobs = repository.findStaleJobs(cutoff);

            if(staleJobs.isEmpty()){
                logger.info("No stale jobs found - cleanup complete");
                return;
            }

            List<UUID> staleIds = staleJobs.stream()
                    .map(JobEntity::getId)
                    .toList();

            logger.info("Found {} stale jobs (NEW older than 7 days)", staleIds.size());

            repository.markStale(staleIds, now);

            logger.info("Successfully marked {} jobs as STALE", staleIds.size());
        }catch (Exception e){
            logger.error("Error during stale jobs cleanup", e);
            throw e;
        }
    }
}
