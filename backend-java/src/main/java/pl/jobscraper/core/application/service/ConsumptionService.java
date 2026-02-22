package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.port.ClockPort;
import pl.jobscraper.core.domain.port.JobRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for marking jobs as consumed.
 * <p>
 * Implements the use case: "Mark jobs as CONSUMED after Notifier processes them".
 * This is the write side of the read-then-mark pattern.
 *
 * @see JobRepository
 * @see ClockPort
 */
@Service
@Transactional
public class ConsumptionService {

    private final JobRepository repository;
    private final ClockPort clock;

    /**
     * Constructor injection of dependencies.
     *
     * @param repository persistence layer for job storage
     * @param clock time abstraction for timestamps
     */
    public ConsumptionService(JobRepository repository, ClockPort clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Marks jobs as CONSUMED (processed by Notifier).
     * <p>
     * Transitions jobs from NEW to CONSUMED state.
     * Idempotent - safe to call multiple times with same IDs.
     *
     * @param ids list of job IDs to mark as consumed
     * @return result with statistics
     * @throws IllegalArgumentException if ids is null or empty
     */
    public ConsumptionResult markConsumed(List<UUID> ids) {
        if(ids == null || ids.isEmpty()) {
            throw  new IllegalArgumentException("ids cannot be null or empty");
        }

        int requested = ids.size();
        Instant now = clock.now();

        JobRepository.ConsumptionStats stats = repository.markConsumed(ids, now);

        return new ConsumptionResult(
                requested,
                stats.marked(),
                stats.alreadyConsumed(),
                stats.notFound()
        );
    }

    /**
     * Result of mark-consumed operation.
     * <p>
     * Contains statistics about state transitions.
     *
     * @param requested      total IDs requested
     * @param marked         NEW → CONSUMED transitions
     * @param alreadyConsumed already CONSUMED (no-op)
     * @param notFound       IDs not in database
     */
    public record ConsumptionResult(
            int requested,
            int marked,
            int alreadyConsumed,
            int notFound
    ){
    }
}
