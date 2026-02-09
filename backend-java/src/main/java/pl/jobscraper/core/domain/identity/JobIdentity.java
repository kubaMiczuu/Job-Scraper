package pl.jobscraper.core.domain.identity;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents unique identity of a job posting (Value Object)
 * <p>
 * This class is the foundation of the deduplication system. Each job posting
 * has exactly ONE identity, determined by either:
 * <ul>
 *     <li><strong>Canonical URL</strong> - normalized, tracker-free URL (preferred)</li>
 *     <li><string>Fallback hash</string> - SHA-256 hash of company+title_location (when URL invalid)</li>
 * </ul>
 *
 * <p><strong>XOR Invariant:</strong> Exactly one field must be non-null
 * This is enforced at construction time and cannot be violated.
 *
 * <p><strong>Immutability:</strong> Once created, identity cannot be changed.
 * All fields are final and there are no setters.
 *
 * <p><strong>Equality:</strong>
 * <ul>
 *     <li>Two URL-based identities are equal if URLs match</li>
 *     <li>Two hash-based identities are equal if hashes match</li>
 *     <li>URL-based and hash-based identities are NEVER equal (different identity types)</li>
 * </ul>
 * 
 * @see JobIdentityCalculator
 * @see NormalizationRules#canonicalUrl(String) 
 */

public final class JobIdentity {

    private static final Pattern HASH_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}+$");

    private final String canonicalUrl;
    private final String fallbackHash;

    /**
     * Private constructor enforcing XOR invariant.
     * <p>
     * Use factory methods instead: {@link #fromCanonicalUrl(String)} or {@link #fromFallbackHash(String)}.
     *
     * @param canonicalUrl canonical URL (XOR with fallbackHash)
     * @param fallbackHash SHA-256 hash (XOR with canonicalUrl)
     * @throws IllegalArgumentException if XOR invariant violated (both null or both non-null)
     */

    private JobIdentity(String canonicalUrl, String fallbackHash) {
        // Validate XOR: exactly one must be non-null
        boolean urlPresent = canonicalUrl != null && !canonicalUrl.isBlank();
        boolean hashPresent = fallbackHash != null && !fallbackHash.isBlank();

        if (!urlPresent && !hashPresent) {
            throw new IllegalArgumentException(
                    "JobIdentity requires either canonicalUrl or fallbackHash (both are null/blank)"
            );
        }

        if (urlPresent && hashPresent) {
            throw new IllegalArgumentException(
                    "JobIdentity XOR violation: both canonicalUrl and fallbackHash are present"
            );
        }

        this.canonicalUrl =  urlPresent ? canonicalUrl.trim() : null;
        this.fallbackHash = hashPresent ? fallbackHash.trim() : null;
    }

    /**
     * Creates URL-based identity.
     * <p>
     * This is the preferred identity type. Use when job posting has a valid
     * canonical URL (normalized via {@link NormalizationRules#canonicalUrl(String)}).
     *
     * @param canonicalUrl canonical job URL (must not be null/blank)
     * @return URL-based JobIdentity
     * @throws IllegalArgumentException if canonicalUrl is null or blank
     */
    public static JobIdentity fromCanonicalUrl(String canonicalUrl) {
        if(canonicalUrl == null || canonicalUrl.isBlank()) {
            throw new IllegalArgumentException("Canonical URL must not be null or blank");
        }
        return new JobIdentity(canonicalUrl, null);
    }

    /**
     * Creates hash-based identity (fallback).
     * <p>
     * Used when job URL is invalid or missing. The hash should be an SHA-256
     * of normalized company+title+location (computed by {@link DefaultJobIdentityCalculator}).
     *
     * @param fallbackHash SHA-256 hash (64 hex characters, must not be null/blank)
     * @return hash-based JobIdentity
     * @throws IllegalArgumentException if hash is null, blank, or invalid format
     */
    public static JobIdentity fromFallbackHash(String fallbackHash) {
        if(fallbackHash == null || fallbackHash.isBlank()) {
            throw new IllegalArgumentException("Fallback hash must not be null or blank");
        }

        if (!HASH_PATTERN.matcher(fallbackHash).matches()) {
            throw new IllegalArgumentException("Fallback hash must be 64 hexadecimal characters (SHA-256), got: " + fallbackHash);
        }

        return new JobIdentity(null, fallbackHash);
    }

    /**
     * Gets canonical URL if this is URL-based identity.
     *
     * @return Optional containing canonical URL, or empty if hash-based
     */
    public Optional<String> getCanonicalUrl() {
        return Optional.ofNullable(canonicalUrl);
    }

    /**
     * Gets fallback hash if this is hash-based identity.
     *
     * @return Optional containing SHA-256 hash, or empty if URL-based
     */
    public Optional<String> getFallbackHash() {
        return Optional.ofNullable(fallbackHash);
    }

    /**
     * Checks if this identity is URL-based
     *
     * @return true if identity uses canonical URL, false if it uses fallback hash
     */
    public boolean isUrlBased() {
        return canonicalUrl != null;
    }

    /**
     * Compares JobIdentity instances.
     * <p>
     * <strong>Equality rules:</strong>
     * <ul>
     *     <li>URL-based == URL-based: compares canonical URLs</li>
     *     <li>Hash-based == Hash-based: compares fallback hashes</li>
     *     <li>URL-based != Hash-based: always false (different identity types)</li>
     * </ul>
     *
     * @param o object to compare
     * @return true if identities are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        JobIdentity that = (JobIdentity) o;

        // URL-based vs Hash-based are never equal
        if (this.isUrlBased() != that.isUrlBased()) {
            return false;
        }

        // Both URL-based: compare URls
        if(that.isUrlBased()){
            return Objects.equals(this.canonicalUrl, that.canonicalUrl);
        }

        // Both hash-based: compare hashes
        return Objects.equals(this.fallbackHash, that.fallbackHash);
    }

    /**
     * Computes hash code.
     * <p>
     * Hash code is based on whichever field is non-null (URL or hash).
     * @return hash code
     */
    @Override
    public int hashCode() {
        return isUrlBased() ? Objects.hash(canonicalUrl, true) : Objects.hash(fallbackHash, false);
    }

    /**
     * String representation for debugging.
     * <p>
     * Format:
     * <ul>
     *     <li>URL-based: {@code JobIdentity{url='https://example.com/job/123'}}</li>
     *     <li>Hash-based: {@code JobIdentity{hash='a1b2c3'}}</li>
     * </ul>
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return isUrlBased() ? "JobIdentity{url=' " + canonicalUrl +"'}": "JobIdentity{hash=' "+fallbackHash + "'}";
    }
}