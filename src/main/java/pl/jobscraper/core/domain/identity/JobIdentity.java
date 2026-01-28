package pl.jobscraper.core.domain.identity;
import java.util.Objects;
import java.util.Optional;

public class JobIdentity {

    private final String canonicalUrl;
    private final String fallbackHash;


    public JobIdentity(String canonicalUrl, String fallbackHash) {

        boolean urlPresent = canonicalUrl != null && !canonicalUrl.isBlank();
        boolean hashPresent = fallbackHash != null && !fallbackHash.isBlank();

        if (urlPresent == hashPresent) { // both present or both blank
            throw new IllegalArgumentException(
                    "Exactly one of canonicalUrl or fallbackHash must be present (xor)."
            );
        }

        this.canonicalUrl =  urlPresent ? canonicalUrl.trim() : null;
        this.fallbackHash = hashPresent ? fallbackHash.trim() : null;
    }

    public static JobIdentity fromCanonicalUrl(String canonicalUrl) {
        if(canonicalUrl == null || canonicalUrl.isBlank()) {
            throw new IllegalArgumentException("Canonical URL is null or empty");
        }
        return new JobIdentity(canonicalUrl, null);
    }

    public static JobIdentity fromFallbackHash(String hash) {

        if(hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Hash is null or empty");
        }

        if (!hash.matches("^[0-9a-fA-F]{64}$")) {
            throw new IllegalArgumentException("Hash must be 64 hex chars (SHA-256).");
        }

        return new JobIdentity(null, hash);
    }

    public Optional<String> getCanonicalUrl() {
        return Optional.ofNullable(canonicalUrl);
    }
    public Optional<String> getFallbackHash() {
        return Optional.ofNullable(fallbackHash);
    }

    public boolean isUrlBased() {
        return canonicalUrl != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof  JobIdentity that)) return false;
        if(this.isUrlBased() && that.isUrlBased()){
            return  this.canonicalUrl.equals(that.canonicalUrl);
        }
        if(!this.isUrlBased() && !that.isUrlBased()){
            return this.fallbackHash.equals(that.fallbackHash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return isUrlBased() ? Objects.hash(canonicalUrl, true) : Objects.hash(fallbackHash, false);
    }

    @Override
    public String toString() {
        return isUrlBased() ? "JobIdentity[url: " + canonicalUrl +"]": "JobIdentity[hash: "+fallbackHash + "]";
    }
}
