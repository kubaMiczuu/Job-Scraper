package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;

import java.time.Instant;
import java.util.List;

/**
 * Inbound job posting payload for batch ingestion.
 * <p>
 * This DTO defines the external API contract for scrapers.
 * Constraints are enforced via Jakarta Bean Validation.
 * * @see pl.jobscraper.core.domain.model.Job
 */
public record JobIngestItemDto(

        @NotBlank(message = "Title is required")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,

        @NotBlank(message = "Company is required")
        @Size(max = 255, message = "Company must not exceed 255 characters")
        String company,

        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @NotBlank(message = "URL is required")
        String url,

        @NotNull(message = "Published date is required")
        @JsonProperty("publishedDate")
        Instant publishedDate,

        @Size(max = 100, message = "Source must not exceed 100 characters")
        String source,

        Seniority seniority,

        @JsonProperty("employmentType")
        EmploymentType employmentType,

        @JsonProperty("techKeywords")
        List<String> techKeywords,

        @Size(max = 255, message = "Salary must not exceed 255 characters")
        String salary,

        @JsonProperty("descriptionSnippet")
        String descriptionSnippet
) {
}
