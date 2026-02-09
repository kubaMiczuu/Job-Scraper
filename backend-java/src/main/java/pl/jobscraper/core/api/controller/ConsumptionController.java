package pl.jobscraper.core.api.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jobscraper.core.api.dto.MarkConsumedRequestDto;
import pl.jobscraper.core.application.service.ConsumptionService;

@RestController
@RequestMapping("/api/jobs")
public class ConsumptionController {

    public final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionService consumptionService) {
        this.consumptionService = consumptionService;
    }

    @PostMapping("/mark-consumed")
    public ResponseEntity<Void> markConsumed(@Valid @RequestBody MarkConsumedRequestDto dto) {

        consumptionService.markConsumed(dto.ids());
        return ResponseEntity.ok().build();
    }
}