package pl.jobscraper.core.domain.model;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Job {
    private final String title;
    private final String company;
    private final String location;
    private final String url;
    private final Instant publishedDate;

    private final String source;
    private final Seniority seniority;
    private final EmploymentType employmentType;
    private final List<String> techKeywords;
    private final String salary;
    private final String descriptionSnippet;

    private Job(Builder builder) {
        // Walidacja wymaganych pól
        this.title = requireNonBlank(builder.title, "title");
        this.company = requireNonBlank(builder.company, "company");
        this.location = requireNonBlank(builder.location, "location");
        this.url = requireNonBlank(builder.url, "url");
        this.publishedDate = Objects.requireNonNull(builder.publishedDate, "publishedDate");

        // Opcjonalne
        this.source = builder.source;
        this.seniority = builder.seniority;
        this.employmentType = builder.employmentType;
        this.techKeywords = builder.techKeywords != null ? List.copyOf(builder.techKeywords) : List.of();
        this.salary = builder.salary;
        this.descriptionSnippet = builder.descriptionSnippet;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getUrl() { return url; }
    public Instant getPublishedDate() { return publishedDate; }
    public String getSource() { return source; }
    public Seniority getSeniority() { return seniority; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public List<String> getTechKeywords() { return techKeywords; }
    public String getSalary() { return salary; }
    public String getDescriptionSnippet() { return descriptionSnippet; }

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder publishedDate(Instant publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder seniority(Seniority seniority) {
            this.seniority = seniority;
            return this;
        }

        public Builder employmentType(EmploymentType employmentType) {
            this.employmentType = employmentType;
            return this;
        }

        public Builder techKeywords(List<String> techKeywords) {
            this.techKeywords = techKeywords;
            return this;
        }

        public Builder salary(String salary) {
            this.salary = salary;
            return this;
        }

        public Builder descriptionSnippet(String descriptionSnippet) {
            this.descriptionSnippet = descriptionSnippet;
            return this;
        }

        public Job build() {
            return new Job(this);
        }
    }
}