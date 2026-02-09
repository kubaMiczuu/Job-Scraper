package pl.jobscraper.core.api.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;

/**
 * Mapper from domain Job to persistence JobEntity.
 * <p>
 * Converts domain layer objects to JPA entities for database persistence.
 *
 * <p><strong>Responsibility:</strong>
 * Maps domain fields to entity fields and sets technical fields
 * (state, timestamps, identity) required by persistence layer.
 */
@Component
public class DomainToEntityMapper {

    /**
     * Converts domain Job to JobEntity.
     * <p>
     * Maps all domain fields and initializes technical fields:
     * - state: NEW
     * - timestamps: current time
     * - canonicalUrl: uses job URL as identity
     *
     * @param job domain Job object
     * @return JobEntity ready for persistence
     */
    public JobEntity toEntity(Job job) {
        JobEntity entity = new JobEntity();

        // Map domain fields
        entity.setTitle(job.getTitle());
        entity.setCompany(job.getCompany());
        entity.setLocation(job.getLocation());
        entity.setUrl(job.getUrl());
        entity.setPublishedDate(job.getPublishedDate());
        entity.setSource(job.getSource());
        entity.setSeniority(job.getSeniority());
        entity.setEmploymentType(job.getEmploymentType());
        entity.setTechKeywords(job.getTechKeywords());
        entity.setSalary(job.getSalary());
        entity.setDescriptionSnippet(job.getDescriptionSnippet());

        // Set technical fields
        entity.setState(JobState.NEW);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setStateChangedAt(now);
        entity.setEnteredNewAt(now);

        // Set identity (canonical URL)
        entity.setCanonicalUrl(job.getUrl());

        return entity;
    }
}