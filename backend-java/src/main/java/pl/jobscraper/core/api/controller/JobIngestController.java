package pl.jobscraper.core.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.jobscraper.core.api.dto.JobIngestItemDto;
import pl.jobscraper.core.api.dto.JobIngestResponseDto;
import pl.jobscraper.core.api.mapper.ApiToDomainMapper;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.JobIngestService;
import pl.jobscraper.core.application.service.JobIngestService.IngestResult;
import pl.jobscraper.core.domain.model.Job;

import java.util.List;


/**
 * REST controller for job ingestion.
 * Accepts batches of job postings, validates input,
 * and delegates processing to the application service.
 * @see JobIngestService
 * @see JobIngestItemDto
 * @see JobIngestResponseDto
 */
@RestController
@RequestMapping("/api/jobs")
public class JobIngestController {

    private final JobIngestService ingestService;
    private final ApiToDomainMapper apiToDomainMapper;
    private final DomainToApiMapper domainToApiMapper;


    /**
     * Constructor injection of dependencies.
     *
     * @param ingestService application service for job ingestion
     * @param apiToDomainMapper mapper DTO → domain
     * @param domainToApiMapper mapper domain → DTO
     */
    public JobIngestController(
            JobIngestService ingestService,
            ApiToDomainMapper apiToDomainMapper,
            DomainToApiMapper domainToApiMapper
    ) {
        this.ingestService = ingestService;
        this.apiToDomainMapper = apiToDomainMapper;
        this.domainToApiMapper = domainToApiMapper;
    }

    /**
     * Ingests and processes a batch of job postings.
     * Performs validation, deduplication, and persistence.
     *
     * @param dtos list of job items
     * @return ingestion summary
     */
    @PostMapping
    public ResponseEntity<JobIngestResponseDto> ingestJobs(@Valid @RequestBody List<JobIngestItemDto> dtos)
    {
        List<Job> jobs = dtos.stream()
                .map(apiToDomainMapper::toDomain)
                .toList();

        IngestResult result = ingestService.ingest(jobs);

        JobIngestResponseDto responseDto = domainToApiMapper.toDto(result);

        return ResponseEntity.ok(responseDto);
    }
}