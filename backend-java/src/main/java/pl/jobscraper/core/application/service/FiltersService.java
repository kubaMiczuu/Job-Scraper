package pl.jobscraper.core.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jobscraper.core.domain.port.JobRepository;

import java.util.List;

@Service
@Transactional(readOnly=true)
public class FiltersService {
    private final JobRepository repository;

    public FiltersService(JobRepository repository) {
        this.repository = repository;
    }

    public List<String> getAvailableSeniorities() {
        return repository.findDistinctSeniorities();
    }

    public List<String> getAvailableEmploymentTypes() {
        return repository.findDistinctEmploymentTypes();
    }

    public List<String> getAvailableLocations() {
        return repository.findDistinctLocations();
    }

    public List<String> getDistinctSources() {
        return  repository.findDistinctSources();
    }
}
