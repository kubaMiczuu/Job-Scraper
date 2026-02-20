package pl.jobscraper.core.api.dto;

import org.springframework.data.domain.Sort;


/**
 * Sort order direction (ascending/descending).
 */
public enum SortOrder {

    ASC,
    DESC;

    /**
     * Converts to Spring Sort.Direction.
     *
     * @return Spring Sort.Direction
     */
    public Sort.Direction toSpringDirection() {
        return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

}
