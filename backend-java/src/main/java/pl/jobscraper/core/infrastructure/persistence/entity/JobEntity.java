package pl.jobscraper.core.infrastructure.persistence.entity;

import jakarta.persistence.*;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence entity mapping the domain job posting to the database.
 * <p>
 * This class is strictly technical and includes database-specific metadata (UUIDs, timestamps, states).
 * Business logic should never operate directly on this entity; use the domain model instead.
 *
 * @see pl.jobscraper.core.domain.model.Job
 * @see JobState
 */
@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    @Column(name = "company", nullable = false)
    private String company;
    @Column(name = "location", nullable = false)
    private String location;
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;
    @Column(name = "published_date", nullable = false)
    private Instant publishedDate;
    @Column(name = "source", length = 100)
    private String source;
    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", length = 50)
    private Seniority seniority;
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 50)
    private EmploymentType employmentType;
    @Column(name = "tech_keywords", columnDefinition = "TEXT[]")
    private List<String> techKeywords;
    @Column(name = "salary")
    private String salary;
    @Column(name = "description_snippet", columnDefinition = "TEXT")
    private String descriptionSnippet;
    @Column(name = "canonical_url", columnDefinition = "TEXT")
    private String canonicalUrl;
    @Column(name = "fallback_hash", length = 64)
    private String fallbackHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private JobState state;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "state_changed_at", nullable = false)
    private Instant stateChangedAt;
    @Column(name = "entered_new_at")
    private Instant enteredNewAt;
    public JobEntity() {
        // Required by JPA
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Instant publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Seniority getSeniority() {
        return seniority;
    }

    public void setSeniority(Seniority seniority) {
        this.seniority = seniority;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public List<String> getTechKeywords() {
        return techKeywords;
    }

    public void setTechKeywords(List<String> techKeywords) {
        this.techKeywords = techKeywords;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDescriptionSnippet() {
        return descriptionSnippet;
    }

    public void setDescriptionSnippet(String descriptionSnippet) {
        this.descriptionSnippet = descriptionSnippet;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public String getFallbackHash() {
        return fallbackHash;
    }

    public void setFallbackHash(String fallbackHash) {
        this.fallbackHash = fallbackHash;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getStateChangedAt() {
        return stateChangedAt;
    }

    public void setStateChangedAt(Instant stateChangedAt) {
        this.stateChangedAt = stateChangedAt;
    }

    public Instant getEnteredNewAt() {
        return enteredNewAt;
    }

    public void setEnteredNewAt(Instant enteredNewAt) {
        this.enteredNewAt = enteredNewAt;
    }
}
