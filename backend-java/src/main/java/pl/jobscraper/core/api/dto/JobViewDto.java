package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * Outbound job representation for notification processing.
 * <p>
 * Extends the basic job data with system metadata (ID, timestamps)
 * required for downstream consumption and state management.
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
