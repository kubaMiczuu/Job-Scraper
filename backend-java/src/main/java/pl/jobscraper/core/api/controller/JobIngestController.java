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
 * REST controller for job batch ingestion.
 * <p>
 * Handles POST /api/jobs endpoint - accepts batches of job postings
 * from scraper, validates, delegates to service, returns aggregated results.
 *
 * <p><strong>Endpoint:</strong>
 * <pre>
 * POST /api/jobs
 * Content-Type: application/json
 *
 * Request body: List of JobIngestItemDto
 * Response: JobIngestResponseDto (aggregated statistics)
 * </pre>
 *
 * <p><strong>Example request:</strong>
 * <pre>{@code
 * POST http://localhost:8080/api/jobs
 * Content-Type: application/json
 *
 * [
 *   {
 *     "title": "Senior Java Developer",
 *     "company": "Google",
 *     "location": "Warsaw, Poland",
 *     "url": "https://google.com/careers/123",
 *     "publishedDate": "2026-02-03T10:00:00Z",
 *     "seniority": "SENIOR",
 *     "employmentType": "UOP",
 *     "techKeywords": ["java", "spring", "postgresql"],
 *     "salary": "15000-20000 PLN"
 *   }
 * ]
 * }</pre>
 *
 * <p><strong>Example response:</strong>
 * <pre>{@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 *
 * {
 *   "received": 1,
 *   "insertedNew": 1,
 *   "updatedExisting": 0,
 *   "skippedDuplicates": 0
 * }
 * }</pre>
 *
 * <p><strong>Validation:</strong>
 * {@code @Valid} triggers Jakarta Bean Validation on each DTO.
 * If validation fails, returns 400 Bad Request with error details.
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>HTTP handling (accept JSON, return JSON)</li>
 *   <li>DTO validation (@Valid)</li>
 *   <li>Mapping: DTO → domain → DTO</li>
 *   <li>Delegation to service (NO business logic here!)</li>
 * </ul>
 *
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
     * Ingests batch of job postings.
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Spring validates DTOs (@Valid)</li>
     *   <li>Mapper converts: List&lt;DTO&gt; → List&lt;Job&gt;</li>
     *   <li>Service processes batch (deduplication + upsert)</li>
     *   <li>Mapper converts: IngestResult → ResponseDto</li>
     *   <li>Return 200 OK with aggregates</li>
     * </ol>
     *
     * @param dtos list of job DTOs from scraper (validated)
     * @return 200 OK with aggregated statistics
     */
    @PostMapping
    public ResponseEntity<String> ingestJobs(@Valid @RequestBody List<JobIngestItemDto> dtos)
    {
        // 1. Convert DTO → domain
        List<Job> jobs = dtos.stream()
                .map(apiToDomainMapper::toDomain)
                .toList();

        // 2. Delegate to service (business logic)
        IngestResult result = ingestService.ingest(jobs);

        // 3. Convert domain → DTO
        JobIngestResponseDto responseDto = domainToApiMapper.toDto(result);

        // 4. Return HTTP 200 OK with body
        return ResponseEntity.ok("Ingest endpoint - coming soon!");
    }
}