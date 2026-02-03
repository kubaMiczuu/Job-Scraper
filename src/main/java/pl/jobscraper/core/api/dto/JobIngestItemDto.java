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
 * DTO for single job posting in ingest request.
 * <p>
 * Represents one job in the batch sent by scraper via POST /api/jobs.
 * This is the API contract - changes to fields require API versioning.
 *
 * <p><strong>Validation:</strong>
 * Required fields are validated using Jakarta Bean Validation annotations.
 * Controller uses {@code @Valid} to trigger validation before processing.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>{@code
 * {
 *   "title": "Senior Java Developer",
 *   "company": "Google",
 *   "location": "Warsaw, Poland",
 *   "url": "https://google.com/careers/123",
 *   "publishedDate": "2026-02-03T10:00:00Z",
 *   "source": "LinkedIn",
 *   "seniority": "SENIOR",
 *   "employmentType": "UOP",
 *   "techKeywords": ["java", "spring", "postgresql"],
 *   "salary": "15000-20000 PLN",
 *   "descriptionSnippet": "We are looking for..."
 * }
 * }</pre>
 *
 * <p><strong>DTO vs Domain:</strong>
 * This is an API DTO (data transfer object), NOT domain model.
 * Mapper converts: JobIngestItemDto → Job (domain).
 *
 * @see pl.jobscraper.core.domain.model.Job
 */
public record JobIngestItemDto(
        /**
         * Job title (required).
         * Example: "Senior Java Developer"
         */
        @NotBlank(message = "Title is required")
        @Size(max = 500, message = "Title must not exceed 500 characters")
        String title,

        /**
         * Company name (required).
         * Example: "Google"
         */
        @NotBlank(message = "Company is required")
        @Size(max = 255, message = "Company must not exceed 255 characters")
        String company,

        /**
         * Job location (required).
         * Example: "Warsaw, Poland" or "Remote"
         */
        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        /**
         * Job posting URL (required).
         * Example: "https://google.com/careers/123"
         */
        @NotBlank(message = "URL is required")
        String url,

        /**
         * Publication date (required).
         * Example: "2026-02-03T10:00:00Z"
         */
        @NotNull(message = "Published date is required")
        @JsonProperty("publishedDate")
        Instant publishedDate,

        /**
         * Job board/source name (optional).
         * Example: "LinkedIn"
         */
        @Size(max = 100, message = "Source must not exceed 100 characters")
        String source,

        /**
         * Seniority level (optional).
         * Possible values: JUNIOR, MID, SENIOR, LEAD
         */
        Seniority seniority,

        /**
         * Employment type (optional).
         * Possible values: UOP, B2B, INTERNSHIP, OTHER
         */
        @JsonProperty("employmentType")
        EmploymentType employmentType,

        /**
         * Technology keywords (optional).
         * Example: ["java", "spring", "postgresql"]
         */
        @JsonProperty("techKeywords")
        List<String> techKeywords,

        /**
         * Salary information (optional).
         * Example: "15000-20000 PLN"
         */
        @Size(max = 255, message = "Salary must not exceed 255 characters")
        String salary,

        /**
         * Job description snippet (optional).
         * Example: "We are looking for..."
         */
        @JsonProperty("descriptionSnippet")
        String descriptionSnippet
) {
}
