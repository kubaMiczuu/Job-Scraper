package pl.jobscraper.core.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.api.dto.PageResponse;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.AllJobsService;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class AllJobsController {

    private final AllJobsService allJobsService;
    private final DomainToApiMapper mapper;

    public AllJobsController(AllJobsService allJobsService, DomainToApiMapper mapper) {
        this.allJobsService = allJobsService;
        this.mapper = mapper;
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<JobViewDto>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String state
    ) {
        List<JobEntity> entities = allJobsService.fetchPaginated(page, size, state);

        long totalElements = allJobsService.countTotal(state);

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
