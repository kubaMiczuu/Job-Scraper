package pl.jobscraper.core.application.dto;

import pl.jobscraper.core.domain.model.value.Seniority;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) for job search criteria.
 * <p>
 * Encapsulates filtering parameters passed from the API layer to the Application layer.
 * All fields are optional (null indicates no filtering for that specific field).
 */
public class JobFilter {

    private final String location;

    private final Seniority seniority;

    private final List<String> keywords;

    /**
     * Constructs a filter with specified criteria.
     *
     * @param location  target location (can be partial)
     * @param seniority required seniority level
     * @param keywords  list of required technologies or keywords
     */
    public JobFilter(String location, Seniority seniority, List<String> keywords) {
        this.location = location;
        this.seniority = seniority;
        this.keywords = keywords;
    }

    /**
     * Creates an empty filter with no criteria.
     *
     * @return a JobFilter instance with all null fields
     */
    public static JobFilter none() {
        return new JobFilter(null, null, null);
    }

    /**
     * Checks if the filter has any active criteria.
     *
     * @return true if all filter fields are null or empty
     */
    public boolean isEmpty() {
        return location == null
                && seniority == null
                && (keywords == null || keywords.isEmpty());
    }

    public boolean hasLocation() {
        return location != null;
    }
    public boolean hasSeniority() {
        return seniority != null;
    }
    public boolean hasKeywords() {
        return keywords != null && !keywords.isEmpty();
    }

    public String getLocation() {
        return location;
    }
    public Seniority getSeniority() {
        return seniority;
    }
    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        JobFilter jobFilter = (JobFilter) o;
        return Objects.equals(location, jobFilter.location)
                && seniority == jobFilter.seniority
                && Objects.equals(keywords, jobFilter.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, seniority, keywords);
    }

    @Override
    public String toString() {
        return "JobFilter{" +
                "location='" + location + '\'' +
                ", seniority=" + seniority +
                ", keywords=" + keywords +
                '}';
    }
}

