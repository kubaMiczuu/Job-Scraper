package pl.jobscraper.core.infrastructure.time;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.domain.port.ClockPort;

import java.time.Instant;

/**
 * Production implementation of {@link ClockPort} using system clock.
 * <p>
 * This adapter simpy delegates to {@link Instant#now()} providing
 * real system time. It is the default implementation used in production
 * and development environments.
 *
 * <p><strongThread-safety:</strong> This class is stateless and thread-safe.
 * {@link Instant#now()} is also thread-safe.
 *
 * <p><strong>Spring integration:</strong> Annotated with {@code @Component}
 * to be automatically registered as a Spring bean. Can be injected into
 * services via constructor dependency injection.
 *
 * <p><strong>Usage (automatic via Spring):</strong>
 * <pre>{@code
 * @Service
 * public class JobIngestService {
 *     private final ClockPort clock;  // Spring injects SystemClockAdapter
 *
 *     public JobIngestService(ClockPort clock) {
 *         this.clock = clock;
 *     }
 *
 *     public void save(Job job) {
 *         Instant now = clock.now();  // Real system time
 *         entity.setCreatedAt(now);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Time zone:</strong> {@link Instant} is always UTC (no timezone offset).
 * For local time conversions, use {@code ZonedDateTime.ofInstant(clock.now(), zoneId)}.
 *
 * <p><strong>Precision:</strong> Depends on underlying OS and JVM.
 * Typically, nanosecond precision on modern systems, but actual resolution
 * may be coarser (e.g., milliseconds on Windows).
 *
 * <p><strong>Testing:</strong> In tests, replace with {@code FixedClock}
 * for deterministic, controllable time.
 *
 * @see ClockPort
 * @see Instant#now()
 */
@Component
public class SystemClockAdapter implements ClockPort {
    /**
     * Returns current system time.
     * <p>
     * Delegates directly to {@link Instant#now()}, which returns
     * the current instant from the system UTC clock.
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * ClockPort clock = new SystemClockAdapter();
     * Instant now = clock.now();
     * // → 2026-02-02T14:30:45.123456789Z (current UTC time)
     * }</pre>
     *
     * @return current instant in UTC (never null)
     */
    @Override
    public Instant now() {
        return Instant.now();
    }
}
