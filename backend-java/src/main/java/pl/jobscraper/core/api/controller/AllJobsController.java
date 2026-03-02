package pl.jobscraper.core.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.jobscraper.core.api.dto.FiltersResponseDto;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.api.dto.PageResponse;
import pl.jobscraper.core.api.dto.SortField;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.AllJobsService;
import pl.jobscraper.core.application.service.FiltersService;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

/**
 * REST controller exposing job listing endpoints.
 * Supports pagination, filtering, and sorting.
 */
@RestController
@RequestMapping("/api/jobs")
public class AllJobsController {

    private final AllJobsService allJobsService;
    private final DomainToApiMapper mapper;
    private final FiltersService filtersService;

    public AllJobsController(AllJobsService allJobsService, DomainToApiMapper mapper, FiltersService filtersService) {
        this.allJobsService = allJobsService;
        this.mapper = mapper;
        this.filtersService = filtersService;
    }

    /**
     * Retrieves a paginated list of jobs.
     * Supports optional filtering and sorting.
     *
     * @param page zero-based page index (default: 0)
     * @param size page size (default: 20)
     * @param seniority optional filter
     * @param employmentType optional filter
     * @param location optional filter
     * @param source optional filter
     * @param searchText optional filter
     * @param sort sorting in format: field,direction (default: publishedDate,desc)
     *
     * @return paginated list of jobs
     */
    @GetMapping("/all")
    public ResponseEntity<PageResponse<JobViewDto>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Seniority[] seniority,
            @RequestParam(required = false) EmploymentType[] employmentType,
            @RequestParam(required = false) String[] location,
            @RequestParam(required = false) String[] source,
            @RequestParam(value = "search", required = false) String[] searchText,
            @RequestParam(defaultValue = "publishedDate,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        String sortBy = sortParts.length > 0 ? sortParts[0].trim() : "publishedDate";
        String sortOrder = sortParts.length > 1 ? sortParts[1].trim() : "desc";


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
                seniority,
                employmentType,
                location,
                source,
                searchText,
                entitySortField,
                validatedSortOrder
        );

        long totalElements = allJobsService.countTotal(seniority, employmentType, location, source, searchText);

        List<JobViewDto> dtos = entities.stream().map(mapper::toViewDto).toList();

        List<String> seniorities = filtersService.getAvailableSeniorities();
        List<String> employmentTypes = filtersService.getAvailableEmploymentTypes();
        List<String> locations = filtersService.getAvailableLocations();
        List<String> sources = filtersService.getDistinctSources();

        FiltersResponseDto filters = new FiltersResponseDto(
                seniorities,
                employmentTypes,
                locations,
                sources
        );

        PageResponse<JobViewDto> response = PageResponse.of(
                dtos,
                page,
                size,
                totalElements,
                filters
        );
        return ResponseEntity.ok(response);
    }
}
