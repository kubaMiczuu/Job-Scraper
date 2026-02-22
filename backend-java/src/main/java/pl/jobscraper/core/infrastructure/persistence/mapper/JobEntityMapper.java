package pl.jobscraper.core.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;

/**
 * Data transformation layer between domain models and persistence entities.
 * <p>
 * Ensures total separation between the business-focused {@link Job} and
 * the technical {@link JobEntity}. All logic regarding state transitions
 * and timestamps is injected by the caller to maintain statelessness.
 *
 * @see Job
 * @see JobEntity
 */
@Component
public class JobEntityMapper {

    /**
     * Maps a domain Job to a new persistence entity.
     * <p>Populates business data, sets identity from the provided {@link JobIdentity},
     * and initializes technical metadata for auditing.</p>
     *
     * @param job domain job (business data)
     * @param identity calculated job identity (URL or hash-based)
     * @param state initial state (typically NEW for new inserts)
     * @param now current timestamp (from ClockPort)
     * @return new JobEntity ready for persistence
     */
    public JobEntity toEntity(Job job, JobIdentity identity, JobState state, Instant now) {
        JobEntity entity = new JobEntity();

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

        if (identity.isUrlBased()) {
            entity.setCanonicalUrl(identity.getCanonicalUrl().orElseThrow());
            entity.setFallbackHash(null);
        } else {
            entity.setCanonicalUrl(null);
            entity.setFallbackHash(identity.getFallbackHash().orElseThrow());
        }

        entity.setState(state);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setStateChangedAt(now);
        entity.setEnteredNewAt(state == JobState.NEW ? now : null);

        return entity;
    }

    /**
     * Updates an existing database entity with fresh business data from the domain.
     * <p>Technical fields (ID, state, identity, creation timestamps) remain untouched.</p>
     *
     * @param entity existing entity from database (will be modified in-place)
     * @param job updated domain job (source of new values)
     */
    public void updateEntityFromJob(JobEntity entity, Job job) {
        // Update only business fields (no state, no timestamps, no identity)
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

    }

    /**
     * Converts persistence JobEntity to domain Job (for SELECT/response).
     * <p>
     * Creates a domain Job with business fields from entity.
     * Technical fields (id, state, timestamps) are NOT included in domain model.
     *
     * @param entity persistence entity from database
     * @return domain Job (business object for use cases)
     */
    public Job toDomain(JobEntity entity) {
        return Job.builder()
                .title(entity.getTitle())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .url(entity.getUrl())
                .publishedDate(entity.getPublishedDate())
                .source(entity.getSource())
                .seniority(entity.getSeniority())
                .employmentType(entity.getEmploymentType())
                .techKeywords(entity.getTechKeywords())
                .salary(entity.getSalary())
                .descriptionSnippet(entity.getDescriptionSnippet())
                .build();

    }
}