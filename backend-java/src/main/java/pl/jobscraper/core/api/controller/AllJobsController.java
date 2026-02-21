package pl.jobscraper.core.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.api.dto.PageResponse;
import pl.jobscraper.core.api.dto.SortField;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.AllJobsService;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class AllJobsController {

    private final AllJobsService allJobsService;
    private final DomainToApiMapper mapper;
    private static  final Logger log = LoggerFactory.getLogger(AllJobsController.class);

    public AllJobsController(AllJobsService allJobsService, DomainToApiMapper mapper) {
        this.allJobsService = allJobsService;
        this.mapper = mapper;
    }

    /**
     * Fetches paginated list of jobs with sorting.
     *
     * <p><strong>Query parameters:</strong>
     * <ul>
     *   <li>page: page number, 0-based (default 0)</li>
     *   <li>size: items per page (default 20)</li>
     *   <li>seniority: filter by seniority - NEW, CONSUMED, STALE (optional)</li>
     *   <li>sort: format "field,direction" (default "publishedDate,desc")</li>
     * </ul>
     *
     * <p><strong>Sort fields:</strong>
     * publishedDate, title, company, location, salary, createdAt
     *
     * <p><strong>Sort directions:</strong>
     * asc (ascending), desc (descending)
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * # Default (newest first):
     * GET /api/jobs/all
     *
     * # Company A-Z:
     * GET /api/jobs/all?sort=company,asc
     *
     * # Highest salary first:
     * GET /api/jobs/all?sort=salary,desc
     *
     * # NEW jobs, sorted by title:
     * GET /api/jobs/all?seniority=NEW&sort=title,asc
     *
     * # Page 2, 10 items, sorted by location:
     * GET /api/jobs/all?page=1&size=10&sort=location,asc
     * }</pre>
     *
     * @param page  page number, 0-based (default 0)
     * @param size  items per page (default 20)
     * @param seniority optional seniority filter (NEW, CONSUMED, STALE)
     * @param sort  sort parameter: "field,direction" (default "publishedDate,desc")
     * @return paginated response with metadata
     */
    @GetMapping("/all")
    public ResponseEntity<PageResponse<JobViewDto>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String seniority,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "publishedDate,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        String sortBy = sortParts.length > 0 ? sortParts[0].trim() : "publishedDate";
        String sortOrder = sortParts.length > 1 ? sortParts[1].trim() : "desc";

        Seniority jobSeniority = null;
        if (seniority != null && !seniority.isBlank()) {
            try {
                jobSeniority = Seniority.valueOf(seniority.toUpperCase());
            }catch (IllegalArgumentException e) {
                log.error("Failed to fetch jobs from database with seniority: {}", seniority, e);
            }
        }
        EmploymentType jobEmploymentType = null;
        if (employmentType != null && !employmentType.isBlank()) {
            try {
                jobEmploymentType = EmploymentType.valueOf(employmentType.toUpperCase());
            }catch (IllegalArgumentException e) {
                log.error("Failed to fetch jobs from database with employmentType: {}", employmentType, e);
            }
        }

        String entitySortField;
        try {
            SortField sortField = SortField.valueOf(sortBy.toUpperCase().replace("_", ""));
            entitySortField = sortField.getFieldName();
        }catch (IllegalArgumentException e){
            entitySortField = SortField.PUBLISHED_DATE.getFieldName();
        }

        String validatedSortOrder = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";

        List<JobEntity> entities = allJobsService.fetchPaginated(
                page,
                size,
                jobSeniority,
                jobEmploymentType,
                location,
                source,
                entitySortField,
                validatedSortOrder
        );

        long totalElements = allJobsService.countTotal(seniority, employmentType, location, source);

        List<JobViewDto> dtos = entities.stream().map(mapper::toViewDto).toList();

        PageResponse<JobViewDto> response = PageResponse.of(
                dtos,
                page,
                size,
                totalElements
        );
        return ResponseEntity.ok(response);
    }
}
