package pl.jobscraper.core.domain.identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class NormalizationRules {
    private NormalizationRules() {}

    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    private static final Pattern GENDER_TAGS = Pattern.compile("\\((m\\s*/\\s*f\\s*/\\s*d|f\\s*/\\s*m\\s*/\\s*d|all\\s*genders)\\)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> TRACKERS = Set.of(
            "utm_source","utm_medium","utm_campaign","utm_term","utm_content",
            "gclid","fbclid","ref","ref_id","session","sessionId","aff","aff_id"
    );

    public static String normalizeTitle(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = GENDER_TAGS.matcher(s).replaceAll("");
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        return s.toLowerCase(Locale.ROOT);
    }

    public static String normalizeCompany(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        return s.toLowerCase(Locale.ROOT);
    }

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
