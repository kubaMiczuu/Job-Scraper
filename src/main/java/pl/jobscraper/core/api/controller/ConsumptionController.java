package pl.jobscraper.core.api.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class ConsumptionController {
    @PostMapping("/mark-consumed")
    public ResponseEntity<String> markConsumed(@RequestBody String idsJson) {
        // TODO: Implement in Day 8
        return ResponseEntity.ok("Mark consumed endpoint - coming soon!");
    }
}
