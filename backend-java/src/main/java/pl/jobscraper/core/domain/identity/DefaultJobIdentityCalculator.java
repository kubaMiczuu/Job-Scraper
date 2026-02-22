package pl.jobscraper.core.domain.identity;

import pl.jobscraper.core.domain.model.Job;
import java.util.Objects;
import static pl.jobscraper.core.domain.identity.NormalizationRules.*;

/**
 * Default implementation of the job identity calculation strategy.
 * <p>
 * This calculator ensures deterministic deduplication using a two-tier approach:
 * <ol>
 * <li><b>Primary (URL):</b> Canonicalized URL is used if valid.</li>
 * <li><b>Fallback (Content Hash):</b> SHA-256 hash of normalized company, title, and location.</li>
 * </ol>
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
     * Calculates a unique {@link JobIdentity}.
     * <p>Prefers URL-based identity to maintain stability even if job descriptions
     * are slightly updated by the source.
     *
     *
     * @param job the job posting to calculate identity for (must not be null)
     * @return unique JobIdentity (either URL-based or hash-based)
     * @throws IllegalArgumentException if job is null
     */
    @Override
    public JobIdentity calculate(Job job){
        Objects.requireNonNull(job, "job must not be null");

        var canon = canonicalUrl(job.getUrl());
        if (canon.isPresent()) {
            return JobIdentity.fromCanonicalUrl(canon.get());
        }
        final String companyN = normalizeCompany(job.getCompany());
        final String titleN = normalizeTitle(job.getTitle());
        final String locationN = normalizeLocation(job.getLocation());

        final String payload = companyN + HASH_SEPARATOR + titleN + HASH_SEPARATOR + locationN;

        final String hashHex = hashing.sha256Hex(payload);

        return JobIdentity.fromFallbackHash(hashHex);
    }
}