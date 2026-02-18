package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Response DTO for mark-consumed operation.
 * <p>
 * Contains statistics about how many jobs were marked as CONSUMED.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>{@code
 * {
 *   "requested": 5,
 *   "marked": 3,
 *   "alreadyConsumed": 1,
 *   "notFound": 1
 * }
 * }</pre>
 *
 * <p><strong>Field meanings:</strong>
 * <ul>
 *   <li>requested: total IDs in request</li>
 *   <li>marked: how many NEW → CONSUMED transitions</li>
 *   <li>alreadyConsumed: how many were already CONSUMED (idempotent)</li>
 *   <li>notFound: how many IDs don't exist in database</li>
 * </ul>
 *
 * @param requested      total IDs requested
 * @param marked         count of NEW → CONSUMED transitions
 * @param alreadyConsumed count already in CONSUMED state
 * @param notFound       count of non-existent IDs
 */
public record ConsumedResponseDto(
        int requested,
        int marked,
        @JsonProperty("alreadyConsumed")
        int alreadyConsumed,
        @JsonProperty("notFound")
        int notFound
) {
    /**
     * Validates invariant: requested = marked + alreadyConsumed + notFound.
     */
    public ConsumedResponseDto{
        if (requested != marked + alreadyConsumed + notFound){
            throw new IllegalArgumentException("ConsumedResponseDto invariant violated: " + "requested != marked + alreadyConsumed + notFound");
        }
    }
}
