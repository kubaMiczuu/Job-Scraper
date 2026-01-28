package pl.jobscraper.core.api.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class NewJobsController {
    @GetMapping("/new")
    public ResponseEntity<String> getNewJobs(@RequestParam(defaultValue = "100") int limit) {
        // TODO: Implement in Day 7
        return ResponseEntity.ok("New jobs endpoint - coming soon! Limit: " + limit);
    }
}
