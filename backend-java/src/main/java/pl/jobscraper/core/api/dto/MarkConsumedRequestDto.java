package pl.jobscraper.core.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for mark-consumed request.
 * <p>
 * Contains list of job IDs that Notifier has successfully processed.
 *
 * <p><strong>Example JSON request:</strong>
 * <pre>{@code
 * {
 *   "ids": [
 *     "a1b2c3d4-5678-90ab-cdef-123456789abc",
 *     "e5f6g7h8-1234-56cd-78ef-abcdef123456"
 *   ]
 * }
 * }</pre>
 *
 * <p><strong>Validation:</strong>
 * List must not be empty (at least 1 ID required).
 *
 * @param ids list of job IDs to mark as consumed (not empty)
 */
public record MarkConsumedRequestDto(
        @NotEmpty(message = "IDs list must not be empty")
        List<UUID> ids
) {
}
