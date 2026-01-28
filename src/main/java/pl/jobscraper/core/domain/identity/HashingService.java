package pl.jobscraper.core.domain.identity;

public interface HashingService {
    public String sha256Hex(String input);
}
