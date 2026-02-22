package pl.jobscraper.core.api.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.api.dto.JobIngestItemDto;
import pl.jobscraper.core.domain.model.Job;

/**
 * Mapper from API DTOs to domain objects.
 * <p>
 * Converts API layer representations (DTOs) to domain layer objects.
 * This is a simple field-to-field mapping with NO business logic.
 *
 * <p><strong>Responsibility:</strong>
 * Pure conversion - copies fields from DTO to domain object.
 * No validation, no calculations, no business decisions.
 */
@Component
public class ApiToDomainMapper {

    /**
     * Converts JobIngestItemDto to domain Job.
     * <p>
     * Simple field-to-field mapping. All fields are copied as-is.
     *
     * @param dto API DTO from request
     * @return domain Job object
     */
    public Job toDomain(JobIngestItemDto dto) {
        return Job.builder()
                .title(dto.title())
                .company(dto.company())
                .location(dto.location())
                .url(dto.url())
                .publishedDate(dto.publishedDate())
                .source(dto.source())
                .seniority(dto.seniority())
                .employmentType(dto.employmentType())
                .techKeywords(dto.techKeywords())
                .salary(dto.salary())
                .descriptionSnippet(dto.descriptionSnippet())
                .build();
    }
}