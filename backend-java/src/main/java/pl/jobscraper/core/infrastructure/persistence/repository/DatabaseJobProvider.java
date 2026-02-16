package pl.jobscraper.core.infrastructure.persistence.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Real implementation of IJobProvider that fetches data from database.
 * <p>
 * This provider uses JobRepository to fetch actual job postings from PostgreSQL.
 * It replaces FakeJobProvider in production use.
 *
 * <p><strong>@Primary annotation:</strong>
 * Tells Spring to prefer this implementation over FakeJobProvider
 * when injecting IJobProvider into NotificationService.
 *
 * <p><strong>Usage:</strong>
 * NotificationService → IJobProvider → DatabaseJobProvider → JobRepository → Database
 */
@Component
@ConditionalOnProperty(name = "job.provider.type", havingValue = "database")
public class DatabaseJobProvider implements IJobProvider{
    private final JobRepository repository;

    /**
     * Constructor injection of JobRepository.
     *
     * @param jobRepository domain repository port for job persistence
     */
    public DatabaseJobProvider(JobRepository jobRepository) {
        this.repository = jobRepository;
    }

    /**
     * Fetches NEW jobs from database (ready for notification).
     * <p>
     * Returns jobs in state NEW, ordered oldest first (FIFO queue).
     * This is the same data that GET /api/jobs/new endpoint returns.
     *
     * <p><strong>SQL executed:</strong>
     * <pre>
     * SELECT * FROM jobs
     * WHERE state = 'NEW'
     * ORDER BY entered_new_at ASC, id ASC
     * LIMIT 100
     * </pre>
     *
     * @return list of NEW jobs (up to 100), oldest first
     */
    @Override
    public List<JobEntity> getNewJobs(JobFilter filter) {
        return repository.fetchNewWithFilters(filter, 100, 0);

    }

    /**
     * Marks a collection of jobs as consumed in the persistent storage.
     * <p>
     * This implementation extracts {@link UUID}s from the provided {@link JobEntity} list
     * and performs a bulk update in the database using the current timestamp.
     * </p>
     *
     * @param jobs The list of {@link JobEntity} objects whose notification status
     * should be updated to "consumed".
     * @throws IllegalArgumentException if the {@code jobs} list is null.
     */
    public void makeConsumedNotifications(List<JobEntity> jobs) {
        List<UUID> uuids = jobs.stream().map(JobEntity::getId).toList();
        repository.markConsumed(uuids, Instant.now());
    }
}
