package pl.jobscraper.core.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic container for paginated data.
 * <p>
 * Standardizes how collections are returned across the API, providing
 * essential navigation metadata for clients.
 *
 * @param <T> The type of the resource being paginated.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        @JsonProperty("totalElements")
        long totalElements,
        @JsonProperty("totalPages")
        int totalPages,
        @JsonProperty("hasNext")
        boolean hasNext,
        @JsonProperty("hasPrevious")
        boolean hasPrevious,
        FiltersResponseDto filters

) {
    /**
     * Factory method - creates PageResponse from Spring Page.
     *
     * @param content   items on current page
     * @param page      current page number
     * @param size      page size
     * @param totalElements total elements count
     * @return PageResponse with calculated metadata
     */
    public static  <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements, FiltersResponseDto filters) {
        int totalPages = size>0 ? (int)Math.ceil((double)totalElements / size) : 0;
        boolean hasNext = page < totalPages-1;
        boolean hasPrevious = page > 0;

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious,
                filters
        );
    }
}
