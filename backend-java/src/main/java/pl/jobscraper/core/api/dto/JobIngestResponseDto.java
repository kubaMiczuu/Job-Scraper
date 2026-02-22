package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Statistics for a completed job ingestion batch.
 * <p>
 * Provides a breakdown of processing results for audit and monitoring.
 * The invariant {@code received == insertedNew + updatedExisting + skippedDuplicates} must hold.
 *
 * @param received total jobs in batch
 * @param insertedNew count of newly inserted jobs
 * @param updatedExisting count of updated jobs
 * @param skippedDuplicates count of batch duplicates
 */
public record JobIngestResponseDto(


        int received,

        @JsonProperty("insertedNew")
        int insertedNew,

        @JsonProperty("updatedExisting")
        int updatedExisting,

        @JsonProperty("skippedDuplicates")
        int skippedDuplicates
) {
        /**
         * Internal integrity check for the ingestion summary.
         */
        public JobIngestResponseDto {
                if (received != (insertedNew + updatedExisting + skippedDuplicates)) {
                        throw new IllegalStateException("Ingestion statistics mismatch: " + received + " total != sum of parts");
                }
        }
}