package pl.jobscraper.core.domain.identity;

/**
 * Service for cryptographic hashing operations.
 * <p>
 * This is a domain-defined port (interface) that abstracts hash computation.
 * Infrastructure layer provides the concrete implementation.
 * <p>
 * Used primarily for fallback identity calculation when URL is invalid.
 *
 * @see JobIdentity
 * @see DefaultJobIdentityCalculator
 */
public interface HashingService {
    /**
     * Computes SHA-256 hash of the input string.
     * <p>
     * The hash is returned as a 64-character hexadecimal string (lowercase).
     * This method is deterministic - same input always produces same hash
     *
     * @param input the string to hash (must not be null)
     * @return 64-character hexadecimal SHA-256 hash (lowercase)
     * @throws IllegalArgumentException if input is null
     */
    public String sha256Hex(String input);
}