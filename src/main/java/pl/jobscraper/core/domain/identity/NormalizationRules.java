package pl.jobscraper.core.domain.identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Normalization rules for job posting data.
 * <p>
 * This utility class provides static methods for normalizing job data fields
 * to ensure deterministic identity calculation and deduplication.
 *
 * <p><strong>Why normalization matters:</strong>
 * Without normalization, semantically identical jobs might be treated as different:
 * <ul>
 *     <li>"Java Developer" vs "java developer" vs "JAVA DEVELOPER"</li>
 *     <li>"Google Inc." vs "google inc." vs "Google  Inc."</li>
 *     <li>"https://example.com/job?id=123&utm_source=linkedin" vs "https://example.com/job?id=123"</li>
 * </ul>
 *
 * <p><strong>Key principles:</strong>
 * <ul>
 *     <li><strong>Deterministic:</strong> Same input always produces same output</li>
 *     <li><strong>Lossy:</strong> Normalization removes information (case, whitespace, trackers)</li>
 *     <li><strong>Idempotent</strong> normalize(normalize(x)) == normalize(x)</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> All methods are static and stateless (thread-safe).
 * Compiled patterns are thread-safe and reused for better performance.
 * @see JobIdentity
 * @see DefaultJobIdentityCalculator
 */
public final class NormalizationRules {
    /**
     * Private constructor - this is a utility class with only static methods.
     * <p>
     *     Prevents instantion via {@code new NormalizationRules()}.
     */
    private NormalizationRules() {}

    /**
     * Pre-compiled pattern for matching multiple consecutive whitespace characters.
     * <p>
     * Used to compress "Java  Developer" -> "Java Developer" (single space).
     * Pre-compilation improves performance when normalizing thousands of jobs.
     */
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    /**
     * Pre-compiled pattern for matching gender tags in job titles.
     * <p>
     * Matches (case-insensitive):
     * <ul>
     *     <li>(m/f/d) or (M / F / D) - male/female/diverse</li>
     *     <li>(f/m/d) or (F / M / D) - female/male/diverse</li>
     *     <li>(all genders) or (ALL GENDERS)</li>
     * </ul>
     * These tags are removed to normalize job titles for deduplication.
     */
    private static final Pattern GENDER_TAGS = Pattern.compile("\\((m\\s*/\\s*f\\s*/\\s*d|f\\s*/\\s*m\\s*/\\s*d|all\\s*genders)\\)", Pattern.CASE_INSENSITIVE);
    /**
     * Set of URL tracking parameters to remove during canonicalization.
     * <p>
     * These are common marketing/analytics parameters that don't affect
     * the actual resource (same page, different tracking).
     * Removing them ensures URLs like:
     * <ul>
     *     <li>{@code /job?id=123&utm_source=linkedin}</li>
     *     <li>{@code /job?id=123&utm_source=facebook}</li>
     * </ul>
     * are treated as the same job (id=123).
     */
    private static final Set<String> TRACKERS = Set.of(
            "utm_source","utm_medium","utm_campaign","utm_term","utm_content",
            "gclid","fbclid","ref","ref_id","session","sessionId","aff","aff_id"
    );

    /**
     * Normalizes job title.
     * <p>
     * Normalization steps:
     * <ol>
     *     <li>Trim leading/trailing whitespace</li>
     *     <li>Remove gender tags: (m/f/d), (f,m,d), (all genders) - case insensitive</li>
     *     <li>Compress multiple whitespace characters to single space</li>
     *     <li>Convert to lowercase (locale-independent using ROOT)</li>
     * </ol>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * normalizeTitle("  Java  Developer  ") → "java developer"
     * normalizeTitle("Senior Developer (M/F/D)") → "senior developer"
     * normalizeTitle("Backend Dev (All Genders)") → "backend dev"
     * normalizeTitle(null) → ""
     * }</pre>
     *
     * @param raw raw title (maybe null)
     * @return normalized title (lowercase, trimmed, no gender tags), or empty string if null
     */
    public static String normalizeTitle(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = GENDER_TAGS.matcher(s).replaceAll("");
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes company name.
     * <p>
     * Normalization steps:
     * <ol>
     *     <li>Trim leading/trailing whitespace</li>
     *     <li>Compress multiple whitespace characters to single space</li>
     *     <li>Convert to lowercase (Locale-independent using ROOT)</li>
     * </ol>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * normalizeCompany("  Google   Inc.  ") → "google inc."
     * normalizeCompany("MICROSOFT") → "microsoft"
     * normalizeCompany(null) → ""
     * }</pre>
     *
     * @param raw raw company name (maybe null)
     * @return normalized company name (lowercase, trimmed), or empty string if null
     */
    public static String normalizeCompany(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        return s.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalize job location.
     * <p>
     * Normalization steps:
     * <ol>
     *     <li>Trim leading/trailing whitespace</li>
     *     <li>Compress multiple whitespace characters to single space</li>
     *     <li>Convert to lowercase (locale-independent using ROOT)</li>
     *     <li>Apply location mapping (Polish -> English):
     *          <ul>
     *              <li>"zdalnie" -> "remote"</li>
     *              <li>"home office" -> "remote"</li>
     *              <li>"hybrydowo" -> "hybrid"</li>
     *              <li>"stacjonarnie" -> "onsite"</li>
     *          </ul>
     *     </li>
     *     <li>Normalize coma formating (remove spaces around commas)</li>
     * </ol>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * normalizeLocation("Kraków , PL") → "kraków,pl"
     * normalizeLocation("Zdalnie") → "remote"
     * normalizeLocation("Home Office") → "remote"
     * normalizeLocation("Hybrydowo") → "hybrid"
     * normalizeLocation(null) → ""
     * }</pre>
     *
     * @param raw raw location (maybe null)
     * @return normalized location (lowercase, mapped, comma-normalized), or empty string if null
     */
    public static String normalizeLocation(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        s = s.toLowerCase(Locale.ROOT);
        s = s.replace("zdalnie", "remote")
                .replace("home office", "remote")
                .replace("hybrydowo", "hybrid")
                .replace("stacjonarnie", "onsite");
        s = s.replaceAll("\\s*,\\s*", ",");
        return s;
    }

    /**
     * Canonicalizes job posting URL.
     * <p>
     * This is the preferred method for job identity. A canonical URL is:
     * <ul>
     *     <li>Normalized (lowercase scheme and host)</li>
     *     <li>Tracker-free (utm_*, gclid, fbclid, etc. removed)</li>
     *     <li>Fragment-free (hash part after # is ignored by URL parser)</li>
     *     <li>Deterministic (same resource always produces same canonical URL)</li>
     * </ul>
     *
     * <p><strong>Canonicalization rules:</strong>
     * <ol>
     *     <li>Accept only http:// or https:// schemes (reject ftp://, file://, etc.)</li>
     *     <li>Lowercase the scheme and host: HTTPS://Example.COM -> https://example.com</li>
     *     <li>Preserve path exactly (case-sensitive): /jobs/Senior-Developer</li>
     *     <li>Fragment is automatically ignored by URI (not included in canonical URL)</li>
     *     <li>Parse and filter query parameters:
     *         <ul>
     *             <li>Remove tracking parameters (utm_*, gclid, fbclid, etc.)</li>
     *             <li>Sort remaining parameters alphabetically (TreeMap ensures deterministic order)</li>
     *             <li>Preserve multiple values for same key: ?tag=java&tag=spring</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * <p><strong>Why TreeMap for query params?</strong>
     * TreeMap maintains alphabetical order (deterministic), so:
     * <ul>
     *     <li>{@code ?z=1&a=2} and {@code ?a=2&z=1} both canonicalize to {@code ?a=2&z=1}</li>
     * </ul>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * // Tracer removal + lowercase host
     * canonicalUrl("https://Example.COM/jobs?utm_source=linkedin&id=123")
     *   ->Optional["https://example.com/jobs?id=123"]
     *
     * // Query param sorting (deterministic)
     * canonicalUrl("https://site.com/job?z=1&a=2&m=3")
     *   ->Optional["https://site.com/job?a=2&m=3&z=1"]
     *
     * // Fragment ignored (not in result)
     * canonicalUrl("https://site.com/job#apply-section")
     *   ->Optional["https://site.com/job"]
     *
     * // Invalid scheme
     * canonicalUrl("ftp://example.com/file")
     *   ->Optional.empty()
     *
     * // Invalid URL syntax
     * canonicalURL("not a url")
     *   ->Optional.empty()
     *
     * // Null or blank
     * canonicalUrl(null)
     *   ->Optional.empty
     * }</pre>
     * @param rawUrl raw URL from job posting (maybe null, blank, or invalid)
     * @return Optional containing canonical URL if valid http/https,
     *         or empty if null/blank/invalid/unsupported scheme
     */
    public static Optional<String> canonicalUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return Optional.empty();
        try {
            URI uri = new URI(rawUrl.trim());
            if (uri.getScheme() == null || uri.getHost() == null) return Optional.empty();

            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("https") && !scheme.equals("http")) return Optional.empty();

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            String path = (uri.getPath() == null || uri.getPath().isBlank()) ? "" : uri.getPath();

            // Parse query parameters into TreeMap (alphabetically sorted)
            Map<String, List<String>> kept = new TreeMap<>();
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                for (String pair : uri.getQuery().split("&")) {
                    if (pair.isBlank()) continue;
                    String[] kv = pair.split("=", 2);
                    String k = kv[0];
                    // Skip tracking parameters
                    if (TRACKERS.contains(k)) continue;
                    // Keep parameter (supports multiple values per key)
                    kept.computeIfAbsent(k, key -> new ArrayList<>()).add(kv.length == 2 ? kv[1] : "");
                }
            }

            // Rebuild query string (alphabetically sorted, tracker-free)
            String query = kept.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> e.getKey() + "=" + v))
                    .collect(Collectors.joining("&"));

            // Rebuild canonical URL
            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://").append(host);
            sb.append(path);
            if (!query.isBlank()) sb.append("?").append(query);
            return Optional.of(sb.toString());
        } catch (URISyntaxException ex) {
            // Invalid URL syntax
            return Optional.empty();
        }
    }
}