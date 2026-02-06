package pl.jobscraper.core.domain.port;

import java.time.Instant;

/**
 * Port for accessing system time.
 * <p>
 * This interface abstracts time retrieval, enabling deterministic testing
 * of time-dependent business logic (TTL, state transitions, timestamps).
 *
 * <p><strong>Why abstraction over Instant.now()?</strong>
 * <ul>
 *     <li>Testability: Can inject FixedClock in tests to control time</li>
 *     <li>Determinism: Tests don't depend on system clock (no race conditions)</li>
 *     <li>Time travel: Can simulate passage of time (advance 7 days for TTL tests)</li>
 * </ul>
 *
 * <p><strong>Implementations:</strong>
 * <ul>
 *     <li>{@link pl.jobscraper.core.infrastructure.time.SystemClockAdapter} - Production (returns {@code Instant.now()})</li>
 *     <li>{@code FixedClock} - Testing (returns fixed/controllable time)</li>
 * </ul>
 *
 * <p><strong>Usage example (production):</strong>
 * <pre>{@code
 * @Service
 * public class JobIngestSService{
 *     private final ClockPort clock;
 *
 *     public void saveNew(Job job){
 *         Instant now = clock.now();
 *         entity.setCreatedAt(now);
 *         entity.setEnteredNewAt(now);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Usage example (testing):</strong>
 * <pre>{@code
 * @Test
 * void shouldMarkJobAsStaleAfter7Days(){
 *     FixedClock clock = new FixedClock(Instant.parse("2026-01-01T00:00:00Z"));
 *     Job job = createJob();
 *     job.setEnteredNewAt(clock.now()); // 2025-01-01
 *
 *     clock.advance(Duration.ofDays(7)) // Travel to 2026-01-08
 *
 *     cleanupService.cleanup(clock.now());
 *     assertThat(job.getState()).isEqualTo(STALE);
 * }
 * }</pre>
 *
 * <p><strong>Design pattern:</strong> Hexagonal Architecture (Ports & Adapters)
 * <ul>
 *     <li>port (this interface): Defined by domain, describes what is needed</li>
 *     <li>Adapter (SystemClockAdapter): Defined by infrastructure, provides implementation</li>
 * </ul>
 *
 * @see pl.jobscraper.core.infrastructure.time.SystemClockAdapter
 */
public interface ClockPort {
    /**
     * Returns current instant in time.
     * <p>
     * In production: returns {@code Instant.now()} (system clock).
     * In tests: returns fixed or controllable instant.
     *
     * <p><strong>Time zone:</strong> Always UTC (Instant in timezone-agnostic)
     *
     * @return current instant (never null)
     */
    Instant now();
}
