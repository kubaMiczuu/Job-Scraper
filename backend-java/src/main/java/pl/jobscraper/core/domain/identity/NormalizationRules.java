package pl.jobscraper.core.domain.identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Normalization engine for job posting data.
 * <p>
 * Ensures data consistency by applying lossy transformations (lowercasing,
 * whitespace compression, tracker removal).
 * <p>All methods are deterministic, stateless, and thread-safe.
 *
 * @see JobIdentity
 * @see DefaultJobIdentityCalculator
 */
public final class NormalizationRules {

    private NormalizationRules() {}

    /**
     * Pre-compiled pattern for matching multiple consecutive whitespace characters.
     */
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    /**
     * Matches common gender-neutral tags like (m/f/d), (f/m/d), or (all genders).
     */
    private static final Pattern GENDER_TAGS = Pattern.compile("\\((m\\s*/\\s*f\\s*/\\s*d|f\\s*/\\s*m\\s*/\\s*d|all\\s*genders)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Marketing and analytics query parameters to be stripped during canonicalization.
     */
    private static final Set<String> TRACKERS = Set.of(
            "utm_source","utm_medium","utm_campaign","utm_term","utm_content",
            "gclid","fbclid","ref","ref_id","session","sessionId","aff","aff_id"
    );

    /**
     * Normalizes a job title by removing gender tags and excessive whitespace.
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
     * Normalizes a company name by compressing whitespace and lowercasing.
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
     * Normalizes location data, mapping common Polish terms to English equivalents.
     * <p>Standardizes "zdalnie" to "remote" and removes spaces around separators.
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
     * Transforms a raw URL into a canonical, tracker-free form.
     * <ol>
     * <li>Enforces http/https schemes.</li>
     * <li>Lowercases host and scheme.</li>
     * <li>Removes analytics fragments and tracking query parameters.</li>
     * <li>Sorts remaining parameters alphabetically for determinism.</li>
     * </ol>
     *
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

            Map<String, List<String>> kept = new TreeMap<>();
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                for (String pair : uri.getQuery().split("&")) {
                    if (pair.isBlank()) continue;
                    String[] kv = pair.split("=", 2);
                    String k = kv[0];
                    if (TRACKERS.contains(k)) continue;
                    kept.computeIfAbsent(k, key -> new ArrayList<>()).add(kv.length == 2 ? kv[1] : "");
                }
            }

            String query = kept.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(v -> e.getKey() + "=" + v))
                    .collect(Collectors.joining("&"));

            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append("://").append(host);
            sb.append(path);
            if (!query.isBlank()) sb.append("?").append(query);
            return Optional.of(sb.toString());
        } catch (URISyntaxException ex) {
            return Optional.empty();
        }
    }
}