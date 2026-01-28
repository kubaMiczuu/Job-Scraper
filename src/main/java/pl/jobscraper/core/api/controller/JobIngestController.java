package pl.jobscraper.core.api.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobIngestController {
    @PostMapping
    public ResponseEntity<String> ingestJobs(@RequestBody String jobsJson) {
        // TODO: Implement in Day 5
        return ResponseEntity.ok("Ingest endpoint - coming soon!");
    }
}
