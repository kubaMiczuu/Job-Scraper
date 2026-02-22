package pl.jobscraper.core.domain.policy;

import java.time.Duration;
import java.time.Instant;

/**
 * Domain policy governing job record retention and expiration.
 * <p>
 * Centralizes the business rules for job lifecycles, specifically determining
 * when a {@link pl.jobscraper.core.domain.model.JobState#NEW} job becomes
 * {@link pl.jobscraper.core.domain.model.JobState#STALE}.
 */
public final class TtlPolicy {

    public TtlPolicy() {
        throw new UnsupportedOperationException("Utility class - do not instantiate this class");
    }

    /**
     * Returns the maximum allowed duration for a job to remain in the NEW state.
     */
    public static Duration newStateTtl(){
        return Duration.ofDays(7);
    }

    /**
     * Determines if a job record should be considered stale based on its entry date.
     *
     * @param enteredNewAt the timestamp when the job was marked as NEW.
     * @param now current system time.
     * @return true if the age exceeds the defined retention policy.
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
     * Calculates the point in time before which any job in NEW state is considered expired.
     * <p>Formula: {@code now - 7 days}.
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
