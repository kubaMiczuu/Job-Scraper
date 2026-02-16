package pl.jobscraper.core.infrastructure.persistence.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pl.jobscraper.core.api.mapper.DomainToEntityMapper;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.model.Job;
import org.springframework.stereotype.Component;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IJobProvider} that provides mock job data.
 * Used primarily for testing purposes or during development when the real
 * data source is unavailable.
 */
@Component
@ConditionalOnProperty(name = "job.provider.type", havingValue = "fake")
public class FakeJobProvider implements IJobProvider {

    /** List containing newly discovered mock jobs. */
    public List<JobEntity> newJobs = new ArrayList<>();

    /** List containing all historical mock jobs. */
    public List<JobEntity> allJobs = new ArrayList<>();

    private final DomainToEntityMapper mapper;

    /**
     * Constructs the provider and populates {@link #newJobs} with dummy data.
     * Generates 10 sample jobs and manually sets additional attributes (seniority,
     * salary, etc.) for specific items to simulate various data scenarios.
     */
    public FakeJobProvider(DomainToEntityMapper mapper) {
        this.mapper = mapper;
        initializeMockData();
    }

    /**
     * Creates a mock data for test use
     */
    private void initializeMockData() {
        // Basic jobs
        for (int i = 0; i < 2; i++) {
            Job job = Job.builder()
                    .title("Job" + i)
                    .company("Company" + i)
                    .location("Location" + i)
                    .url("https://job.com/" + i)
                    .publishedDate(Instant.parse("2026-01-26T10:00:00Z"))
                    .build();
            newJobs.add(mapper.toEntity(job));
        }
    }

    /**
     * Returns a list of mock jobs generated during initialization.
     *
     * @return A list of newly created {@link Job} objects.
     */
    public List<JobEntity> getNewJobs(JobFilter filter) {
            return newJobs;
    }

    /**
     * returns nothing :D
     */
    @Override
    public void makeConsumedNotifications(List<JobEntity> jobs) {

    }


}