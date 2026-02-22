package pl.jobscraper.core.domain.port;

import java.time.Instant;

/**
 * Domain port for time retrieval.
 * <p>
 * Decouples the domain logic from the system clock, ensuring that all
 * time-dependent operations (such as TTL validation and record versioning)
 * are deterministic and fully testable.
 * @see <a href="https://martinfowler.com/articles/injection.html">Dependency Injection (Fowler)</a>
 */
public interface ClockPort {
    /**
     * Returns the current {@link Instant}.
     * <p>
     * Implementations must guarantee UTC precision. In test environments,
     * this should be backed by a controllable clock to simulate time passage.
     *
     * @return current point in time.
     */
    Instant now();
}
