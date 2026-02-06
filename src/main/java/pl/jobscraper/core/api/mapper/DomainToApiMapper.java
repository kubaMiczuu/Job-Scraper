package pl.jobscraper.core.api.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.api.dto.JobIngestResponseDto;
import pl.jobscraper.core.api.dto.JobViewDto;
import pl.jobscraper.core.application.service.JobIngestService.IngestResult;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;


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

    public JobViewDto toViewDto(JobEntity entity) {
        return new JobViewDto(
                entity.getId(),
                entity.getTitle(),
                entity.getCompany(),
                entity.getLocation(),
                entity.getUrl(),
                entity.getPublishedDate(),
                entity.getSource(),
                entity.getSeniority(),
                entity.getEmploymentType(),
                entity.getTechKeywords(),
                entity.getSalary(),
                entity.getDescriptionSnippet(),
                entity.getEnteredNewAt()
        );
    }
}