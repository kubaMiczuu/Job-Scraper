package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

@Service
@Transactional
public class AllJobsService {
    private final JobRepository repository;

    public AllJobsService(JobRepository repository) {
        this.repository = repository;
    }
    public List<JobEntity> fetchAll(int limit) {
        return repository.fetchAllOldestFirst(limit);
    }
}
