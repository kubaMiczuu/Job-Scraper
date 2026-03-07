package pl.jobscraper.core.api.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.api.dto.JobIngestItemDto;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.value.EmploymentType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
                .employmentType(parseEmploymentType(dto.employmentType()))
                .techKeywords(dto.techKeywords())
                .salary(dto.salary())
                .descriptionSnippet(dto.descriptionSnippet())
                .build();
    }

    private List<EmploymentType> parseEmploymentType(String value) {
        if(value == null || value.isBlank()) {
            return List.of();
        }
        String[] parts = value.split(",");

        return Arrays.stream(parts)
                .map(String::trim)
                .map(EmploymentType::fromString)
                .filter(Objects::nonNull)
                .toList();
    }
}