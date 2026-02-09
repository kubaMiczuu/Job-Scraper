package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * DTO for single job in NEW jobs queue response.
 * <p>
 * Represents one job returned by GET /api/jobs/new endpoint.
 * This is what Notifier receives and processes.
 *
 * <p><strong>Example JSON response:</strong>
 * <pre>{@code
 * [
 *   {
 *     "id": "a1b2c3d4-5678-90ab-cdef-123456789abc",
 *     "title": "Senior Java Developer",
 *     "company": "Google",
 *     "location": "Warsaw, Poland",
 *     "url": "https://google.com/careers/123",
 *     "publishedDate": "2026-02-03T10:00:00Z",
 *     "source": "LinkedIn",
 *     "seniority": "SENIOR",
 *     "employmentType": "UOP",
 *     "techKeywords": ["java", "spring", "postgresql"],
 *     "salary": "15000-20000 PLN",
 *     "descriptionSnippet": "We are looking for...",
 *     "enteredNewAt": "2026-02-03T10:15:30Z"
 *   }
 * ]
 * }</pre>
 *
 * <p><strong>Additional fields vs JobIngestItemDto:</strong>
 * <ul>
 *   <li>id: Database UUID (for mark-consumed request)</li>
 *   <li>enteredNewAt: When job entered NEW state (for debugging/monitoring)</li>
 * </ul>
 *
 * <p><strong>Why include ID?</strong>
 * Notifier needs ID to mark jobs as consumed via POST /api/jobs/mark-consumed.
 *
 * @see pl.jobscraper.core.domain.model.Job
 */
public record JobViewDto(

        UUID id,
        String title,
        String company,
        String location,
        String url,
        @JsonProperty("publishedDate")
        Instant publishedDate,
        String source,
        Seniority seniority,
        @JsonProperty("employmentType")
        EmploymentType employmentType,
        @JsonProperty("techKeywords")
        List<String> techKeywords,
        String salary,
        @JsonProperty("descriptionSnippet")
        String descriptionSnippet,
        @JsonProperty("enteredNewAt")
        Instant enteredNewAt
) {
}
