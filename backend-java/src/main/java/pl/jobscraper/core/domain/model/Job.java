package pl.jobscraper.core.domain.model;

import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a job posting.
 * <p>
 * This is a pure business object (Value Object) with no persistence concerns.
 * It does NOT contain technical fields like:
 * <ul>
 *     <li>state (NEW, CONSUMED, STALE) managed by persistence layer</li>
 *     <li>id (UUID) - managed by database</li>
 *     <li>timestamps (created_at, updated_at) - managed by persistence layer</li>
 * </ul>
 *
 * <p><strong>Immutability:</strong> All fields are final. Use builder pattern
 * to create instances. Once created a Job cannot be modified.
 *
 * <p><strong>Requires fields (validated at construction):</strong>
 * <ul>
 *     <li>title - job title</li>
 *     <li>company - company name</li>
 *     <li>location - job location</li>
 *     <li>url - job posting URL</li>
 *     <li>publishedDate - when job was posted</li>
 * </ul>
 *
 * <p><strong>Optional fields (maybe null):</strong>
 * source, seniority, employmentType, techKeywords, salary, descriptionSnippet
 *
 * <p><strong>Example usage: </strong>
 * <pre>{@code
 * Job job = Job.builder()
 *     .title("Java Developer")
 *     .company("Google")
 *     .location("Warsaw")
 *     .url("https:/google.com/jobs/123")
 *     .publishedDate(Instant.now())
 *     .seniority(Seniority.MID)
 *     .build();
 * }</pre>
 *
 * @see pl.jobscraper.core.domain.identity.JobIdentity
 * @see JobState
 */
public final class Job {
    //Required fields
    private final String title;
    private final String company;
    private final String location;
    private final String url;
    private final Instant publishedDate;

    //Optional fields
    private final String source;
    private final Seniority seniority;
    private final EmploymentType employmentType;
    private final List<String> techKeywords;
    private final String salary;
    private final String descriptionSnippet;

    /**
     * Private constructor - use {@link #builder()} instead.
     * <p>
     * Validates all required fields using {@link #requireNonBlank(String, String)}
     * Optional fields are assigned directly (maybe null).
     *
     * @param builder builder instance containing all field values
     * @throws IllegalArgumentException if any required field is null or blank
     */
    private Job(Builder builder) {
        // Validate and assign required fields
        this.title = requireNonBlank(builder.title, "title");
        this.company = requireNonBlank(builder.company, "company");
        this.location = requireNonBlank(builder.location, "location");
        this.url = requireNonBlank(builder.url, "url");
        this.publishedDate = Objects.requireNonNull(builder.publishedDate, "publishedDate");

        // Assign optional fields
        this.source = builder.source;
        this.seniority = builder.seniority;
        this.employmentType = builder.employmentType;
        this.techKeywords = builder.techKeywords != null ? List.copyOf(builder.techKeywords) : List.of();
        this.salary = builder.salary;
        this.descriptionSnippet = builder.descriptionSnippet;
    }

    /**
     * Validates that a string field is not null or blank.
     * <p>
     * This is a helper  method for constructor validation, ensuring
     * required fields meet the non-blank contract.
     *
     * @param value the field value to validate
     * @param fieldName the name of the field (for error message)
     * @return the validated value (guaranteed non-null and non-blank)
     * @throws IllegalArgumentException if value is null or blank
     */
    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    //Getters

    /**
     * Gets job title
     *
     * @return job title (never null/blank)
     */
    public String getTitle() { return title; }

    /**
     * Gets company name.
     *
     * @return company name (never null/blank)
     */
    public String getCompany() { return company; }

    /**
     * Gets job location.
     *
     * @return location (never null/blank)
     */
    public String getLocation() { return location; }

    /**
     * Gets job posting URL.
     *
     * @return URL (never null/blank)
     */
    public String getUrl() { return url; }

    /**
     * Gets publication date.
     *
     * @return when job was published (never null)
     */
    public Instant getPublishedDate() { return publishedDate; }

    /**
     * Gets job source/board name.
     *
     * @return source  (maybe null)
     */
    public String getSource() { return source; }

    /**
     * Gets seniority level.
     *
     * @return seniority (maybe null)
     */
    public Seniority getSeniority() { return seniority; }

    /**
     * Gets employment type.
     *
     * @return employment type (maybe null)
     */
    public EmploymentType getEmploymentType() { return employmentType; }

    /**
     * Gets list of technology keywords.
     * <p>
     *     Returns an unmodifiable list to preserve immutability
     * @return immutable list of keywords, or null if not set
     */
    public List<String> getTechKeywords() { return techKeywords; }

    /**
     * Gets salary information.
     *
     * @return salary (maybe null)
     */
    public String getSalary() { return salary; }

    /**
     * Gets description snippet/preview.
     *
     * @return description snippet (maybe null)
     */
    public String getDescriptionSnippet() { return descriptionSnippet; }

    /**
     * Creates a new Builder for constructing Job instances
     * <p>
     * The builder provides a fluent API for setting required and optional fields.
     * Call {@link Builder#build()} to create the Job instance
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing Job instances.
     * <p>
     * Provides a fluent API for setting fields. Required fields (title, company,
     * location, url, publishedDate) must be set before calling {@link #build()},
     * otherwise an {@link IllegalArgumentException} will be thrown.
     * <p>
     * Example usage:
     * <pre>{@code
     * Job job = Job.builder()
     *     .title("Java Developer")
     *     .company("Google")
     *     .location("Warsaw")
     *     .url("https:/google.com/jobs/123")
     *     .publishedDate(Instant.now())
     *     .seniority(Seniority.MID)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private String title;
        private String company;
        private String location;
        private String url;
        private Instant publishedDate;
        private String source;
        private Seniority seniority;
        private EmploymentType employmentType;
        private List<String> techKeywords;
        private String salary;
        private String descriptionSnippet;

        private Builder() {}

        /**
         * Sets job title (required).
         *
         * @param title job title (must not be null or blank)
         * @return this builder for method chaining
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets company name (required).
         *
         * @param company company name (must not be null or blank)
         * @return this builder for method chaining
         */
        public Builder company(String company) {
            this.company = company;
            return this;
        }

        /**
         * Sets job location (required).
         *
         * @param location location (must not be null or blank)
         * @return this builder for method chaining
         */
        public Builder location(String location) {
            this.location = location;
            return this;
        }

        /**
         * Sets job URL (required).
         *
         * @param url job posting URL (must not be null or blank)
         * @return this builder for method chaining
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets publication date (required).
         *
         * @param publishedDate when job was published (must not be null or blank)
         * @return this builder for method chaining
         */
        public Builder publishedDate(Instant publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        /**
         * Sets job source/job board name (optional).
         *
         * @param source source/job board name (maybe null)
         * @return this builder for method chaining
         */
        public Builder source(String source) {
            this.source = source;
            return this;
        }

        /**
         * Sets seniority level (optional).
         *
         * @param seniority seniority (maybe null)
         * @return this builder for method chaining
         */
        public Builder seniority(Seniority seniority) {
            this.seniority = seniority;
            return this;
        }

        /**
         * Sets employment type (optional).
         *
         * @param employmentType employment type (maybe null)
         * @return this builder for method chaining
         */
        public Builder employmentType(EmploymentType employmentType) {
            this.employmentType = employmentType;
            return this;
        }

        /**
         * Sets technology keywords (optional).
         *
         * @param techKeywords list of keywords (maybe null)
         * @return this builder for method chaining
         */
        public Builder techKeywords(List<String> techKeywords) {
            this.techKeywords = techKeywords;
            return this;
        }

        /**
         * Sets salary information (optional).
         *
         * @param salary salary (maybe null)
         * @return this builder for method chaining
         */
        public Builder salary(String salary) {
            this.salary = salary;
            return this;
        }

        /**
         * Setts description snippet (optional).
         *
         * @param descriptionSnippet snippet (maybe null)
         * @return this builder for method chaining
         */
        public Builder descriptionSnippet(String descriptionSnippet) {
            this.descriptionSnippet = descriptionSnippet;
            return this;
        }

        /**
         * Builds and validates the Job instance.
         * <p>
         * All required fields (title, company, location, url, publishedDate)
         * must be set before calling this method
         *
         * @return new Job instance
         * @throws IllegalArgumentException if any required field is null or blank
         */
        public Job build() {
            return new Job(this);
        }
    }
}