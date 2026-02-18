package pl.jobscraper.core.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.application.service.NewJobsService;
import pl.jobscraper.core.domain.model.value.Seniority;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;


/**
 * REST controller for fetching NEW jobs queue.
 * <p>
 * Handles GET /api/jobs/new endpoint - returns NEW jobs for Notifier
 * in deterministic FIFO order.
 *
 * <p><strong>Endpoint:</strong>
 * <pre>
 * GET /api/jobs/new?limit=100
 *
 * Response: List of JobViewDto (NEW jobs, oldest first)
 * </pre>
 *
 * <p><strong>Example request:</strong>
 * <pre>{@code
 * GET http://localhost:8080/api/jobs/new?limit=100
 * }</pre>
 *
 * <p><strong>Example response:</strong>
 * <pre>{@code
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 *
 * [
 *   {
 *     "id": "a1b2c3d4-...",
 *     "title": "Senior Java Developer",
 *     "company": "Google",
 *     "location": "Warsaw, Poland",
 *     "url": "https://google.com/careers/123",
 *     "publishedDate": "2026-02-03T10:00:00Z",
 *     "enteredNewAt": "2026-02-03T10:15:30Z",
 *     ...
 *   },
 *   ...
 * ]
 * }</pre>
 *
 * <p><strong>Query parameters:</strong>
 * <ul>
 *   <li>limit: max number of jobs (default 100, typical batch size for Notifier)</li>
 * </ul>
 *
 * <p><strong>Read-only operation:</strong>
 * This endpoint does NOT change job state. Jobs remain in NEW state.
 * Notifier must call POST /api/jobs/mark-consumed to mark as processed.
 *
 * @see NewJobsService
 * @see JobViewDto
 */
@RestController
@RequestMapping("/api/jobs")
public class NewJobsController {

    private final NewJobsService newJobsService;
    private final DomainToApiMapper mapper;

    /**
     * Constructor injection of dependencies.
     *
     * @param newJobsService application service for fetching NEW jobs
     * @param mapper mapper entity → DTO
     */
    public NewJobsController(NewJobsService newJobsService, DomainToApiMapper mapper) {
        this.newJobsService = newJobsService;
        this.mapper = mapper;
    }


    /**
     * Retrieves a list of new job offers with optional filtering and pagination.
     * <p>
     * Example usage: GET /api/jobs/new?location=London&keywords=java,aws&limit=20&offset=0
     *
     * @param limit     max number of results (default 100)
     * @param offset    starting record index (default 0)
     * @param location  (optional) city or country
     * @param seniority (optional) seniority level (e.g., JUNIOR, MID, SENIOR)
     * @param keywords  (optional) comma-separated list of technologies
     * @return a list of DTOs representing the job views
     */
    @GetMapping("/new")
    public ResponseEntity<List<JobViewDto>> getNewJobs(
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(name = "location",  required = false) String location,
            @RequestParam(name = "seniority", required = false) Seniority seniority,
            @RequestParam(name = "keywords", required = false) String keywords
    ) {

        List<JobEntity> entities;

        if(location == null && seniority == null && keywords == null){
            entities = newJobsService.fetchNew(limit);
        }else{
            List<String> keywordsList = null;
            if (keywords != null &&  !keywords.isBlank()) {
                keywordsList = List.of(keywords.split(","));
            }
            JobFilter filter = new JobFilter(location, seniority, keywordsList);
            entities = newJobsService.fetchNewWithFilters(filter, limit, offset);
        }

        List<JobViewDto> dtos = entities.stream().map(mapper::toViewDto).toList();
        return ResponseEntity.ok(dtos);
    }
}