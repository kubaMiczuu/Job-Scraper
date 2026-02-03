package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for ingest operation response.
 * <p>
 * Contains aggregated statistics about processed batch.
 * Returned by POST /api/jobs endpoint.
 *
 * <p><strong>Example JSON response:</strong>
 * <pre>{@code
 * {
 *   "received": 150,
 *   "insertedNew": 45,
 *   "updatedExisting": 95,
 *   "skippedDuplicates": 10
 * }
 * }</pre>
 *
 * <p><strong>Invariant:</strong>
 * {@code received = insertedNew + updatedExisting + skippedDuplicates}
 *
 * <p><strong>Field meanings:</strong>
 * <ul>
 *   <li><strong>received:</strong> Total jobs in batch (including duplicates)</li>
 *   <li><strong>insertedNew:</strong> How many new jobs inserted (state=NEW)</li>
 *   <li><strong>updatedExisting:</strong> How many existing jobs updated (state unchanged)</li>
 *   <li><strong>skippedDuplicates:</strong> How many duplicates within batch</li>
 * </ul>
 *
 * <p><strong>Idempotency example:</strong>
 * <pre>{@code
 * // First call: ingest([jobA, jobB])
 * {
 *   "received": 2,
 *   "insertedNew": 2,
 *   "updatedExisting": 0,
 *   "skippedDuplicates": 0
 * }
 *
 * // Second call: ingest([jobA, jobB]) - same batch
 * {
 *   "received": 2,
 *   "insertedNew": 0,
 *   "updatedExisting": 2,  // Jobs already exist
 *   "skippedDuplicates": 0
 * }
 * }</pre>
 *
 * @param received total jobs in batch
 * @param insertedNew count of newly inserted jobs
 * @param updatedExisting count of updated jobs
 * @param skippedDuplicates count of batch duplicates
 */
public record JobIngestResponseDto(

        /**
         * Total number of jobs received in batch.
         */
        int received,

        /**
         * Number of jobs inserted as NEW.
         */
        @JsonProperty("insertedNew")
        int insertedNew,

        /**
         * Number of jobs updated (without re-promotion to NEW).
         */
        @JsonProperty("updatedExisting")
        int updatedExisting,

        /**
         * Number of duplicate jobs within batch (skipped).
         */
        @JsonProperty("skippedDuplicates")
        int skippedDuplicates
) {
}