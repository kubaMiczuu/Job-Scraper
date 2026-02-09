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
 * <p><strong>Use case flow (POST /api/jobs/mark-consumed):</strong>
 * <pre>
 * Notifier → POST /api/jobs/mark-consumed (list of job IDs)
 *   ↓
 * 1. Validate input (IDs not empty)
 * 2. Repository marks jobs: NEW → CONSUMED
 * 3. Return 200 OK
 * </pre>
 *
 * <p><strong>Read-then-mark pattern:</strong>
 * <pre>
 * Step 1: Notifier calls GET /api/jobs/new?limit=100
 * Step 2: Notifier processes jobs (sends notifications)
 * Step 3: Notifier calls POST /api/jobs/mark-consumed (list of IDs)
 *
 * If Notifier crashes between step 2 and 3:
 * - Jobs remain in NEW state (safe - no data loss)
 * - Next GET will return same jobs (at-least-once delivery)
 * - Notifier detects duplicates and skips re-sending
 * </pre>
 *
 * <p><strong>State transition:</strong>
 * <pre>
 * NEW → CONSUMED
 * </pre>
 *
 * <p><strong>Idempotency:</strong>
 * Calling with same IDs multiple times is safe:
 * <ul>
 *   <li>First call: NEW → CONSUMED (state changed)</li>
 *   <li>Subsequent calls: CONSUMED → CONSUMED (no-op, still returns success)</li>
 * </ul>
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
     * <p><strong>State changes:</strong>
     * <ul>
     *   <li>state: NEW → CONSUMED</li>
     *   <li>state_changed_at: updated to now</li>
     *   <li>entered_new_at: cleared (NULL)</li>
     * </ul>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * List<UUID> ids = List.of(
     *     UUID.fromString("a1b2c3d4-..."),
     *     UUID.fromString("e5f6g7h8-...")
     * );
     *
     * service.markConsumed(ids);  // First call: transitions NEW → CONSUMED
     * service.markConsumed(ids);  // Second call: no-op (already CONSUMED)
     * }</pre>
     *
     * @param ids list of job IDs to mark as consumed
     * @throws IllegalArgumentException if ids is null or empty
     */
    public void markConsumed(List<UUID> ids) {
        if(ids == null || ids.isEmpty()) {
            throw  new IllegalArgumentException("ids cannot be null or empty");
        }
        Instant now = clock.now();
        repository.markConsumed(ids, now);
    }
}
