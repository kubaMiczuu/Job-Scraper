package pl.jobscraper.core.api.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.NewJobsService;
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
     * Fetches NEW jobs queue (FIFO order).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Service fetches entities from database (ORDER BY entered_new_at ASC)</li>
     *   <li>Mapper converts: List&lt;JobEntity&gt; → List&lt;JobViewDto&gt;</li>
     *   <li>Return 200 OK with JSON array</li>
     </ol>
     *
     * <p><strong>Ordering guarantee:</strong>
     * Jobs are always returned in same order (deterministic).
     * Oldest NEW jobs come first (FIFO queue).
     *
     * @param limit maximum number of jobs to return (default 100)
     * @return 200 OK with list of JobViewDto
     */
    @GetMapping("/new")
    public ResponseEntity<List<JobViewDto>> getNewJobs(@RequestParam(defaultValue = "100") int limit) {
        List<JobEntity> entities = newJobsService.fetchNew(limit);

        List<JobViewDto> dtos = entities.stream().map(mapper::toViewDto).toList();
        return ResponseEntity.ok(dtos);
    }
}