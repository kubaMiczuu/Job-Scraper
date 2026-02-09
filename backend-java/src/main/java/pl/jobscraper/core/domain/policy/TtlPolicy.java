package pl.jobscraper.core.domain.policy;

import java.time.Duration;
import java.time.Instant;

/**
 * Domain policy for Time-To-Live (TTL) of job states.
 * <p>
 * Defines how long jobs can remain in specific states before transitioning.
 * This is pure business logic - no infrastructure dependencies.
 *
 * <p><strong>Current TTL rules:</strong>
 * <ul>
 *   <li>NEW state: 7 days maximum</li>
 *   <li>If job in NEW for >7 days → transition to STALE</li>
 * </ul>
 *
 * <p><strong>Why TTL?</strong>
 * Prevents system from accumulating stale jobs forever.
 * If Notifier doesn't fetch jobs for 7 days, they're archived as STALE.
 *
 * <p><strong>Usage example:</strong>
 * <pre>{@code
 * Instant now = clock.now();
 * Duration ttl = TtlPolicy.newStateTtl();  // 7 days
 * Instant cutoff = now.minus(ttl);
 *
 * // Jobs with entered_new_at < cutoff should be STALE
 * boolean isExpired = TtlPolicy.isStale(enteredNewAt, now);
 * }</pre>
 *
 * <p><strong>Design note:</strong>
 * This is a stateless utility class (pure functions).
 * All methods are static - no need to instantiate.
 */
public final class TtlPolicy {

    /**
     * Private constructor - utility class (all static methods).
     */
    public TtlPolicy() {
        throw new UnsupportedOperationException("Utility class - do not instantiate this class");
    }

    /**
     * TTL for NEW state (7 days).
     * <p>
     * Jobs in NEW state for longer than this duration should be marked STALE.
     *
     * <p><strong>Contract value:</strong>
     * 7 days - as specified in contract v1.0.
     *
     * @return duration of 7 days
     */
    public static Duration newStateTtl(){
        return Duration.ofDays(7);
    }

    /**
     * Checks if job has exceeded TTL for NEW state.
     * <p>
     * Returns true if job entered NEW state more than 7 days ago.
     *
     * <p><strong>Algorithm:</strong>
     * <pre>
     * age = now - enteredNewAt
     * if age > 7 days → true (STALE)
     * else → false (still valid)
     * </pre>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * Instant enteredNewAt = Instant.parse("2026-01-27T10:00:00Z");
     * Instant now = Instant.parse("2026-02-04T10:00:00Z");
     *
     * boolean stale = TtlPolicy.isStale(enteredNewAt, now);
     * // 8 days difference > 7 days TTL → true
     * }</pre>
     *
     * @param enteredNewAt when job entered NEW state
     * @param now current time
     * @return true if job should be STALE, false otherwise
     * @throws IllegalArgumentException if enteredNewAt or now is null
     */
    public static boolean isStale(Instant enteredNewAt, Instant now){
        if(enteredNewAt == null){
            throw new IllegalArgumentException("enteredNewAt cannot be null");
        }
        if(now == null){
            throw new IllegalArgumentException("now cannot be null");
        }

        Duration age = Duration.between(enteredNewAt, now);
        return age.compareTo(newStateTtl())>0;
    }

    /**
     * Calculates cutoff timestamp for stale jobs.
     * <p>
     * Jobs with entered_new_at before this cutoff should be STALE.
     *
     * <p><strong>Formula:</strong>
     * {@code cutoff = now - 7 days}
     *
     * <p><strong>Usage in cleanup:</strong>
     * <pre>{@code
     * Instant now = clock.now();
     * Instant cutoff = TtlPolicy.staleCutoff(now);
     *
     * // Query: SELECT * FROM jobs
     * //        WHERE state='NEW' AND entered_new_at < cutoff
     * }</pre>
     *
     * @param now current time
     * @return cutoff timestamp (now - 7 days)
     * @throws IllegalArgumentException if now is null
     */
    public static Instant staleCutoff(Instant now) {
        if(now==null){
            throw new IllegalArgumentException("now cannot be null");
        }

        return now.minus(newStateTtl());
    }
}
