package pl.jobscraper.core.api.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jobscraper.core.api.dto.ConsumedResponseDto;
import pl.jobscraper.core.api.dto.MarkConsumedRequestDto;
import pl.jobscraper.core.api.mapper.DomainToApiMapper;
import pl.jobscraper.core.application.service.ConsumptionService;

/**
 * REST controller for job consumption operations.
 * Provides endpoints for updating job consumption status.
 */
@RestController
@RequestMapping("/api/jobs")
public class ConsumptionController {

    private final ConsumptionService consumptionService;
    private final DomainToApiMapper mapper;

    public ConsumptionController(ConsumptionService consumptionService,  DomainToApiMapper mapper) {
        this.consumptionService = consumptionService;
        this.mapper = mapper;
    }

    /**
     * Marks selected jobs as consumed.
     *
     * @param dto request containing job IDs to mark as consumed
     *
     * @return result summary with processed items
     */
    @PostMapping("/mark-consumed")
    public ResponseEntity<ConsumedResponseDto> markConsumed(@Valid @RequestBody MarkConsumedRequestDto dto) {

        ConsumptionService.ConsumptionResult result = consumptionService.markConsumed(dto.ids());

        ConsumedResponseDto responseDto = mapper.toConsumedDto(result);
        return ResponseEntity.ok(responseDto);
    }
}