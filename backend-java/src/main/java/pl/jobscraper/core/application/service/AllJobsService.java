package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
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
     * @param seniority optional state filter (null = all)
     * @return list of entities for current page
     */
    public List<JobEntity> fetchPaginated(int page, int size, Seniority seniority, EmploymentType employmentType, String location, String source, String sortBy, String sortOrder) {
        return repository.fetchAllPaginated(page, size, seniority, employmentType, location, source,sortBy, sortOrder);
    }

    /**
     * Counts total jobs (all states or filtered by state).
     *
     * @param seniority optional seniority filter
     * @param employmentType optional employmentType filter
     * @param location optional location filter
     * @param source optional source filter
     * @return total count
     */
    public long countTotal(String seniority, String employmentType, String location, String source) {
        return repository.countAll(seniority, employmentType, location, source);
    }
}
