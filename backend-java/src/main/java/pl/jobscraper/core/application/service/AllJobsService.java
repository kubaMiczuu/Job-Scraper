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
     * @param seniorities optional seniority filter
     * @param employmentTypes optional employmentType filter
     * @param locations optional location filter
     * @param sources optional source filter
     * @return list of entities for current page
     */
    public List<JobEntity> fetchPaginated(int page, int size, Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] searchText ,String sortBy, String sortOrder) {
        return repository.fetchAllPaginated(page, size, seniorities, employmentTypes, locations, sources, searchText, sortBy, sortOrder);
    }

    /**
     * Counts total jobs (all states or filtered by state).
     *
     * @param seniorities optional seniority filter
     * @param employmentTypes optional employmentType filter
     * @param locations optional location filter
     * @param sources optional source filter
     * @return total count
     */
    public long countTotal(Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] searchText) {
        return repository.countAll(seniorities, employmentTypes, locations, sources, searchText);
    }
}
