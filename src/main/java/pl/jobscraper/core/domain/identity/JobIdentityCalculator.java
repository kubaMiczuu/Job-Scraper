package pl.jobscraper.core.domain.identity;
import pl.jobscraper.core.domain.model.Job;

public interface JobIdentityCalculator {
    JobIdentity calculate(Job job);
}
