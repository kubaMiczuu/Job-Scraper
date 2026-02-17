package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

/**
 * Application service for fetching all jobs with pagination.
 */
@Service
@Transactional
public class AllJobsService {
    private final JobRepository repository;

    public AllJobsService(JobRepository repository) {
        this.repository = repository;
    }

    /**
     * Fetches paginated jobs (all states or filtered by state).
     *
     * @param page  page number (0-based)
     * @param size  page size
     * @param state optional state filter (null = all)
     * @return list of entities for current page
     */
    public List<JobEntity> fetchPaginated(int page, int size, String state) {
        return repository.fetchAllPaginated(page, size, state);
    }

    /**
     * Counts total jobs (all states or filtered by state).
     *
     * @param state optional state filter (null = all)
     * @return total count
     */
    public long countTotal(String state) {
        return repository.countAll(state);
    }
}
