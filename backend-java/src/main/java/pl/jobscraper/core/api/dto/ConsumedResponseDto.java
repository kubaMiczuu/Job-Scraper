package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Summary of the job consumption process.
 * * <p>Ensures full traceability by breaking down the status of all requested IDs.
 * The invariant {@code requested == marked + alreadyConsumed + notFound} is enforced at construction.
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
