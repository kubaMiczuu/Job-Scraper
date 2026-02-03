package pl.jobscraper.core.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.time.Instant;
import java.util.List;

/**
 * Mapper for converting between domain {@link Job} and persistence {@link JobEntity}.
 * <p>
 * This mapper provides pure field-to-field conversion with NO business logic.
 * Business decisions (state management, timestamps, identity calculation)
 * are handled by services and policies, not the mapper.
 *
 * <p><strong>Mapping responsibilities:</strong>
 * <ul>
 *   <li><strong>toEntity:</strong> Job + metadata → JobEntity (for INSERT)</li>
 *   <li><strong>updateEntityFromJob:</strong> Copy business fields from Job to existing entity (for UPDATE)</li>
 *   <li><strong>toDomain:</strong> JobEntity → Job (for SELECT/response)</li>
 * </ul>
 *
 * <p><strong>Key principle: No business logic!</strong>
 * <ul>
 *   <li>❌ Don't decide state (NEW/CONSUMED/STALE) - service does this</li>
 *   <li>❌ Don't compute timestamps - clock service provides these</li>
 *   <li>❌ Don't validate data - validators do this</li>
 *   <li>✅ Only copy fields from one object to another</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong>
 * <pre>{@code
 * // INSERT
 * Job job = Job.builder().title("Java Dev").build();
 * JobIdentity identity = calculator.calculate(job);
 * JobEntity entity = mapper.toEntity(job, identity, JobState.NEW, clock.now());
 * repository.save(entity);
 *
 * // UPDATE
 * JobEntity existing = repository.findById(id).orElseThrow();
 * mapper.updateEntityFromJob(existing, updatedJob);
 * existing.setUpdatedAt(clock.now());
 * repository.save(existing);
 *
 * // SELECT
 * JobEntity entity = repository.findById(id).orElseThrow();
 * Job job = mapper.toDomain(entity);
 * return job;  // to API
 * }</pre>
 *
 * @see Job
 * @see JobEntity
 */
@Component
public class JobEntityMapper {

    /**
     * Converts domain Job to persistence JobEntity (for INSERT).
     * <p>
     * Creates a new entity with all fields from domain Job plus
     * technical metadata (identity, state, timestamps).
     *
     * <p><strong>Field mapping:</strong>
     * <ul>
     *   <li>Business fields: copied from Job (title, company, location, etc.)</li>
     *   <li>Identity: extracted from JobIdentity (canonicalUrl XOR fallbackHash)</li>
     *   <li>State: provided as parameter (typically NEW for insert)</li>
     *   <li>Timestamps: all set to 'now' parameter</li>
     *   <li>ID: left null (database generates via gen_random_uuid())</li>
     * </ul>
     *
     * <p><strong>enteredNewAt logic:</strong>
     * Set to 'now' if state is NEW, otherwise null.
     * This timestamp is used for queue sorting and TTL calculation.
     *
     * @param job domain job (business data)
     * @param identity calculated job identity (URL or hash-based)
     * @param state initial state (typically NEW for new inserts)
     * @param now current timestamp (from ClockPort)
     * @return new JobEntity ready for persistence
     */
    public JobEntity toEntity(Job job, JobIdentity identity, JobState state, Instant now) {
        JobEntity entity = new JobEntity();

        // Business fields from domain Job
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

        // Identity (XOR: canonicalUrl OR fallbackHash)
        if (identity.isUrlBased()) {
            entity.setCanonicalUrl(identity.getCanonicalUrl().orElseThrow());
            entity.setFallbackHash(null);
        } else {
            entity.setCanonicalUrl(null);
            entity.setFallbackHash(identity.getFallbackHash().orElseThrow());
        }

        // State management
        entity.setState(state);

        // Timestamps (all set to now for new entity)
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setStateChangedAt(now);

        // entered_new_at: only set if state is NEW
        entity.setEnteredNewAt(state == JobState.NEW ? now : null);

        // ID is null - database will generate via gen_random_uuid()

        return entity;
    }

    /**
     * Updates business fields in existing entity from domain Job (for UPDATE).
     * <p>
     * Copies only business fields (title, salary, etc.) from Job to entity.
     * Does NOT update technical fields (id, state, timestamps, identity).
     *
     * <p><strong>Updated fields:</strong>
     * title, company, location, url, publishedDate, source, seniority,
     * employmentType, techKeywords, salary, descriptionSnippet
     *
     * <p><strong>NOT updated (preserved from existing entity):</strong>
     * id, canonicalUrl, fallbackHash, state, createdAt, updatedAt,
     * stateChangedAt, enteredNewAt
     *
     * <p><strong>Caller responsibility:</strong>
     * After calling this method, the caller should:
     * <ul>
     *   <li>Set updatedAt = clock.now()</li>
     *   <li>Save entity via repository</li>
     * </ul>
     *
     * <p><strong>Why not update state/timestamps here?</strong>
     * State transitions and timestamp management are business logic,
     * handled by services/policies. Mapper only copies fields.
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

        // DO NOT update:
        // - id (immutable)
        // - canonicalUrl / fallbackHash (immutable identity)
        // - state (managed by state transitions)
        // - createdAt (immutable)
        // - updatedAt (set by caller after update)
        // - stateChangedAt (only on state transitions)
        // - enteredNewAt (only on NEW state entry)
    }

    /**
     * Converts persistence JobEntity to domain Job (for SELECT/response).
     * <p>
     * Creates a domain Job with business fields from entity.
     * Technical fields (id, state, timestamps) are NOT included in domain model.
     *
     * <p><strong>Field mapping:</strong>
     * All business fields are copied from entity to Job builder.
     * Technical metadata (state, timestamps) is discarded - domain Job
     * is a pure business object without persistence concerns.
     *
     * <p><strong>techKeywords handling:</strong>
     * Entity may have null or empty list - Job always returns non-null list
     * (empty list if not set, see Job.getTechKeywords()).
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

        // Note: Job does NOT have:
        // - id (domain doesn't know about database IDs)
        // - state (technical concern, not business)
        // - timestamps (technical metadata)
        // - identity fields (canonicalUrl/hash are persistence detail)
    }
}