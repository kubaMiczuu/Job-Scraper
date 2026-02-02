package pl.jobscraper.core.domain.identity;

import pl.jobscraper.core.domain.model.Job;
import java.util.Objects;
import static pl.jobscraper.core.domain.identity.NormalizationRules.*;

/**
 * Constructs calculator with hashing service dependency.
 * Default implementation of {@link JobIdentityCalculator}.
 * <p>
 * This calculator implements the core deduplication strategy defined in contract v1.0:
 * <ol>
 *     <li>Attempt to canonicalize the job's URL via {@link NormalizationRules#canonicalUrl(String)}</li>
 *     <li>If successful -> create URL-based identity ({@link JobIdentity#fromCanonicalUrl(String)})</li>
 *     <li>If URL invalid/missing -> compute fallback hash from normalized company, title, and location</li>
 * </ol>
 *
 * <p><strong>Fallback hash computation:</strong>
 * <pre>{@code
 * payload = normalizedCompany + HASH_SEPARATOR + normalizedTitle + HASH_SEPARATOR + normalizedLocation
 * hash = SHA-256(payload)
 * identity = JobIdentity.fromFallbackHash(hash)
 * }</pre>
 * The hash separator prevents collisions attacks.
 *
 * <p><strong>Determinism guarantee:</strong>
 * This calculator is deterministic - same Job data always produces the same JobIdentity.
 * This property is critical for:
 * <ul>
 *     <li>Idempotent batch ingestion (same batch can be sent multiple times)</li>
 *     <li>Reliable deduplication across scraper runs</li>
 *     <li>Consistent identity even if job is updated (URL doesn't change)</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> This class is thread-safe. The HashingService
 * dependency must also be thread-safe (Sha256HashingService is).
 *
 * @see JobIdentity
 * @see JobIdentityCalculator
 * @see NormalizationRules
 * @see HashingService
 */
public final class DefaultJobIdentityCalculator implements JobIdentityCalculator {

    /**
     * Separator used in fallback hash payload to prevent collision attacks.
     * <p>
     * Without separator, "AB" + "C" and "A" + "BC" would hash to the same value
     * With separator: "AB#C" != "A#BC" (different hashes)
     */
    private static final String HASH_SEPARATOR = "#";
    private final HashingService hashing;

    /**
     * Constructs calculator with hashing service dependency.
     * <p>
     * The hashing service is used for fallback identity when URL is invalid.
     *
     * @param hashing SHA-256 hashing service (must not be null)
     * @throws NullPointerException if hashing is null
     */
    public DefaultJobIdentityCalculator(HashingService hashing) {
        this.hashing = Objects.requireNonNull(hashing, "hashing must not be null");
    }

    /**
     * Calculates unique identity for a job posting.
     * <p><strong>Algorithm:</strong>
     * <ol>
     *     <li>Try to canonicalize {@link Job#getUrl()} using {@link NormalizationRules#canonicalUrl(String)}</li>
     *     <li>If canonicalization succeeds (URL valid):
     *         <ul>
     *             <li>Create URL-based identity: {@code JobIdentity.fromCanonicalUrl(canonicalUrl)}</li>
     *             <li>This is the preferred identity type (more stable, URL typically unique)</li>
     *         </ul>
     *     </li>
     *     <li>If canonicalization fails (URL null/blank/invalid):
     *         <ul>
     *             <li>Normalize company: {@link NormalizationRules#normalizeCompany(String)}</li>
     *             <li>Normalize title: {@link NormalizationRules#normalizeTitle(String)}</li>
     *             <li>Normalize location: {@link NormalizationRules#normalizeLocation(String)}</li>
     *             <li>Build payload: {@code company + HASH_SEPARATOR + title + HASH_SEPARATOR + location}</li>
     *             <li>Compute SHA-256 hash: {@link HashingService#sha256Hex(String)}</li>
     *             <li>Create hash-based identity: {@code JobIdentity.fromFallbackHash(hash)}</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * // Example 1: Valid URL -> URL-based identity
     * Job job1 = Job.builder()
     *     .url("https://google.com/jobs/123?utm_source=linkedin")
     *     .company("Google")
     *     .title("Java Developer")
     *     .location("Warsaw")
     *     .publishedDate(Instant.now())
     *     .build();
     *
     * JobIdentity id1 = calculator.calculate(job1);
     * // -> JobIdentity.fromCanonicalUrl("https://google.com/jobs/123")
     * // Note: tracker removed, URL canonicalized
     *
     * // Example 2: Invalid URL → hash-based identity
     * Job job2 = Job.builder()
     *     .url("")  // blank URL
     *     .company("Google")
     *     .title("Java Developer")
     *     .location("Warsaw")
     *     .publishedDate(Instant.now())
     *     .build();
     *
     * JobIdentity id2 = calculator.calculate(job2);
     * // Normalized: company="google", title="java developer", location="warsaw"
     * // Payload: "google#java developer#warsaw"
     * // Hash: SHA-256("google#java developer#warsaw") = "a1b2c3...64chars"
     * // -> JobIdentity.fromFallbackHash("a1b2c3...64chars")
     * }</pre>
     *
     * <p><strong>Normalization effects:</strong>
     * These jobs are considered IDENTICAL (same identity)
     * <pre>{@code
     * Job A: company="Google Inc.", title="Java Developer (M/F/D)", location="Warsaw, PL"
     * Job B: company="google  inc.", title="JAVA DEVELOPER", location="Warsaw , PL"
     *
     * Both noralize to:
     *   company="google inc.", title="java developer", location="warsaw,pl"
     * -> Same hash → Same identity → Deduplicated!
     * }</pre>
     *
     * @param job the job posting to calculate identity for (must not be null)
     * @return unique JobIdentity (either URL-based or hash-based)
     * @throws IllegalArgumentException if job is null
     */
    @Override
    public JobIdentity calculate(Job job){
        Objects.requireNonNull(job, "job must not be null");

        // Step 1: Try URL canonicalization (preferred method)
        var canon = canonicalUrl(job.getUrl());
        if (canon.isPresent()) {
            return JobIdentity.fromCanonicalUrl(canon.get());
        }
        // Step 2: Fallback to hash-based identity
        final String companyN = normalizeCompany(job.getCompany());
        final String titleN = normalizeTitle(job.getTitle());
        final String locationN = normalizeLocation(job.getLocation());

        // Build payload with separator to prevent collisions
        final String payload = companyN + HASH_SEPARATOR + titleN + HASH_SEPARATOR + locationN;

        // Compute SHA-256 hash (64 hex characters)
        final String hashHex = hashing.sha256Hex(payload);

        return JobIdentity.fromFallbackHash(hashHex);
    }

}