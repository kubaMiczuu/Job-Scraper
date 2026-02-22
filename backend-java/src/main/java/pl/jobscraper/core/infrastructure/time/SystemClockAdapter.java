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
 *
 * @see ClockPort
 * @see Instant#now()
 */
@Component
public class SystemClockAdapter implements ClockPort {
    /**
     * Returns current system time.
     *
     * @return current instant in UTC (never null)
     */
    @Override
    public Instant now() {
        return Instant.now();
    }
}
