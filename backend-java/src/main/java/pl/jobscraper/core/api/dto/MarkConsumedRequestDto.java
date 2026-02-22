package pl.jobscraper.core.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request payload to acknowledge job processing.
 * <p>
 * Signals that the specified jobs have been successfully handled by the downstream consumer.
 * @param ids list of job IDs to mark as consumed (not empty)
 */
public record MarkConsumedRequestDto(
        @NotEmpty(message = "IDs list must not be empty")
        List<UUID> ids
) {
}
