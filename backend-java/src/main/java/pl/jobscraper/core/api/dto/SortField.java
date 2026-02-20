package pl.jobscraper.core.api.dto;


/**
 * Allowed sort fields for job queries.
 * <p>
 * Defines which fields can be used for sorting in API endpoints.
 */
public enum SortField {

    PUBLISHED_DATE("publishedDate"),
    TITLE("title"),
    COMPANY("company"),
    LOCATION("location"),
    CREATED_AT("createdAt");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
