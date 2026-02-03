package pl.jobscraper.core.api.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.api.dto.JobIngestResponseDto;
import pl.jobscraper.core.application.service.JobIngestService.IngestResult;

/**
 * Mapper from domain objects to API DTOs.
 * <p>
 * Converts domain layer representations to API layer DTOs for responses.
 * This is a simple field-to-field mapping with NO business logic.
 *
 * <p><strong>Responsibility:</strong>
 * Pure conversion - copies fields from domain to DTO.
 * No formatting, no calculations, no business decisions.
 */
@Component
public class DomainToApiMapper {

    /**
     * Converts IngestResult to JobIngestResponseDto.
     * <p>
     * Simple field-to-field mapping.
     *
     * @param result domain IngestResult from service
     * @return API response DTO
     */
    public JobIngestResponseDto toDto(IngestResult result) {
        return new JobIngestResponseDto(
                result.received(),
                result.insertedNew(),
                result.updatedExisting(),
                result.skippedDuplicates()
        );
    }
}