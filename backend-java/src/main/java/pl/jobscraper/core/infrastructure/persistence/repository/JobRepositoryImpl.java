package pl.jobscraper.core.infrastructure.persistence.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.domain.model.value.EmploymentType;
import pl.jobscraper.core.domain.model.value.Seniority;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;
import pl.jobscraper.core.infrastructure.persistence.mapper.JobEntityMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * JPA implementation of {@link JobRepository} port.
 * <p>
 * This adapter translates domain operations into JPA/SQL operations,
 * orchestrating {@link SpringDataJobJpaRepository} (Spring Data JPA)
 * and {@link JobEntityMapper} (conversion).
 *
 * <p><strong>Architecture:</strong> Hexagonal (Ports & Adapters)
 * <ul>
 *   <li>Port: {@link JobRepository} interface (defined by domain)</li>
 *   <li>Adapter: This class (implemented by infrastructure)</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Orchestration: coordinates mapper + Spring Data JPA</li>
 *   <li>Conversion: Job ↔ JobEntity (via mapper)</li>
 *   <li>Transaction management: @Transactional on write operations</li>
 *   <li>SQL delegation: actual queries executed by Spring Data JPA</li>
 * </ul>
 *
 * <p><strong>No business logic!</strong>
 * This class contains NO business decisions:
 * <ul>
 *   <li>State management logic → in services/policies</li>
 *   <li>Identity calculation → in JobIdentityCalculator</li>
 *   <li>Timestamp generation → via ClockPort (passed as params)</li>
 *   <li>Validation → in API layer or domain</li>
 * </ul>
 * Only orchestration: receive domain objects → map → persist → map back.
 *
 * <p><strong>Transaction boundaries:</strong>
 * Write methods are {@code @Transactional} - changes committed on success,
 * rolled back on exception. Read methods are NOT transactional (read-only).
 *
 * <p><strong>Dependencies:</strong>
 * <ul>
 *   <li>{@link SpringDataJobJpaRepository} - Spring Data JPA interface</li>
 *   <li>{@link JobEntityMapper} - domain ↔ persistence conversion</li>
 * </ul>
 *
 * @see JobRepository
 * @see SpringDataJobJpaRepository
 * @see JobEntityMapper
 */
@Repository
@Transactional
public class JobRepositoryImpl implements JobRepository {

    private final SpringDataJobJpaRepository jpaRepository;
    private final JobEntityMapper mapper;

    /**
     * Constructor injection of dependencies.
     *
     * @param jpaRepository Spring Data JPA repository (auto-implemented by Spring)
     * @param mapper mapper for Job ↔ JobEntity conversion
     */
    public JobRepositoryImpl(SpringDataJobJpaRepository jpaRepository, JobEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * Saves new job posting (INSERT with state=NEW).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Mapper converts: Job + identity + state=NEW + now → JobEntity</li>
     *   <li>Spring Data JPA executes: INSERT INTO jobs (...) VALUES (...)</li>
     *   <li>Database generates UUID (gen_random_uuid())</li>
     *   <li>Transaction commits</li>
     * </ol>
     *
     * <p><strong>State initialization:</strong>
     * <ul>
     *   <li>state = NEW</li>
     *   <li>entered_new_at = now (for queue sorting)</li>
     *   <li>created_at = updated_at = state_changed_at = now</li>
     * </ul>
     *
     * <p><strong>Identity (XOR):</strong>
     * Sets canonicalUrl OR fallbackHash (exactly one non-null).
     *
     * @param job domain job (business data)
     * @param identity calculated identity (URL or hash-based)
     * @param now current timestamp (all metadata timestamps)
     */
    @Override
    public void saveNew(Job job, JobIdentity identity, Instant now) {
        // 1. Convert domain → persistence
        JobEntity entity = mapper.toEntity(job, identity, JobState.NEW, now);

        // 2. Persist via Spring Data JPA
        jpaRepository.save(entity);
        // SQL: INSERT INTO jobs (...) VALUES (...)
    }

    /**
     * Updates existing job posting (UPDATE without re-promoting to NEW).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Fetch existing entity from database (by ID)</li>
     *   <li>Mapper updates business fields: entity ← job</li>
     *   <li>Set updated_at = now</li>
     *   <li>Spring Data JPA detects changes and executes UPDATE</li>
     *   <li>Transaction commits</li>
     * </ol>
     *
     * <p><strong>Updated fields:</strong>
     * title, company, location, url, publishedDate, source, seniority,
     * employmentType, techKeywords, salary, descriptionSnippet, updated_at
     *
     * <p><strong>NOT updated (critical!):</strong>
     * <ul>
     *   <li>state - remains as-is (NEW/CONSUMED/STALE)</li>
     *   <li>entered_new_at - NOT reset (update doesn't re-promote to NEW)</li>
     *   <li>created_at - immutable</li>
     *   <li>identity fields - immutable (canonical_url, fallback_hash)</li>
     * </ul>
     *
     * <p><strong>Why no re-promotion to NEW?</strong>
     * Contract v1.0 specifies: "Update does NOT re-promote to NEW".
     * This ensures:
     * <ul>
     *   <li>Notifier sees each job exactly once</li>
     *   <li>No duplicate notifications</li>
     *   <li>Stable queue (jobs don't jump back to NEW)</li>
     * </ul>
     *
     * @param id database UUID of existing job
     * @param job updated domain job (source of new values)
     * @param now current timestamp (for updated_at)
     * @throws IllegalArgumentException if job with ID doesn't exist
     */
    @Override
    public void updateExisting(UUID id, Job job, Instant now) {
        // 1. Fetch existing entity (or throw if not found)
        JobEntity entity = jpaRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Job not found with ID: " + id));

        // 2. Update business fields from domain Job
        mapper.updateEntityFromJob(entity, job);

        // 3. Set updated_at (mapper doesn't touch timestamps)
        entity.setUpdatedAt(now);

        // 4. Save (JPA auto-detects changes and generates UPDATE)
        jpaRepository.save(entity);
        // SQL: UPDATE jobs SET title=?, company=?, ..., updated_at=? WHERE id=?

        // Note: state, entered_new_at, created_at remain unchanged!
    }

    /**
     * Finds job by identity (for deduplication).
     * <p>
     * Delegates to Spring Data JPA based on identity type:
     * <ul>
     *   <li>URL-based → {@link SpringDataJobJpaRepository#findByCanonicalUrl(String)}</li>
     *   <li>Hash-based → {@link SpringDataJobJpaRepository#findByFallbackHash(String)}</li>
     * </ul>
     *
     * <p><strong>Index usage:</strong>
     * Uses unique indexes for O(log n) lookup:
     * <ul>
     *   <li>idx_jobs_canonical_url (URL-based)</li>
     *   <li>idx_jobs_fallback_hash (hash-based)</li>
     * </ul>
     *
     * <p><strong>Example usage (deduplication):</strong>
     * <pre>{@code
     * JobIdentity identity = calculator.calculate(job);
     * Optional<JobEntity> existing = findByIdentity(identity);
     *
     * if (existing.isPresent()) {
     *     updateExisting(existing.get().getId(), job, now);  // UPDATE
     * } else {
     *     saveNew(job, identity, now);  // INSERT
     * }
     * }</pre>
     *
     * @param identity job identity (URL or hash-based)
     * @return Optional containing entity if found, empty otherwise
     */
    @Override
    public Optional<JobEntity> findByIdentity(JobIdentity identity) {
        if(identity.isUrlBased()){
            // URL-based identity: lookup by canonical_url
            String canonicalUrl = identity.getCanonicalUrl()
                    .orElseThrow(()-> new IllegalStateException("URL-based identity has no URL"));
            return jpaRepository.findByCanonicalUrl(canonicalUrl);
        }else{
            // Hash-based identity: lookup by fallback_hash
            String fallbackHash = identity.getFallbackHash()
                    .orElseThrow(()-> new IllegalStateException("Hash-based identity has no hash"));
            return jpaRepository.findByFallbackHash(fallbackHash);
        }
    }


    /**
     * Fetches NEW jobs ordered oldest first (notification queue).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Create Pageable with limit</li>
     *   <li>Spring Data JPA executes query (ORDER BY entered_new_at ASC, id ASC)</li>
     *   <li>Mapper converts: List&lt;JobEntity&gt; → List&lt;Job&gt;</li>
     *   <li>Return domain objects to caller</li>
     * </ol>
     *
     * <p><strong>SQL generated:</strong>
     * <pre>
     * SELECT * FROM jobs
     * WHERE state = 'NEW'
     * ORDER BY entered_new_at ASC, id ASC
     * LIMIT ?
     * </pre>
     *
     * <p><strong>Index usage:</strong>
     * Compound index idx_jobs_new_queue (entered_new_at, id) ensures:
     * <ul>
     *   <li>Fast filtering (WHERE state='NEW')</li>
     *   <li>Fast sorting (ORDER BY entered_new_at, id)</li>
     *   <li>No full table scan</li>
     * </ul>
     *
     * <p><strong>Deterministic ordering:</strong>
     * Secondary sort by id (UUID) ensures stable order when multiple jobs
     * have identical entered_new_at timestamp. Prevents "disappearing items"
     * when Notifier fetches multiple times.
     *
     * @param limit maximum number of jobs to return
     * @return list of domain Jobs (oldest NEW first), up to limit
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobEntity> fetchNewOldestFirst(int limit) {
        // 1. Create pagination (page 0, size = limit)
        Pageable pageable = PageRequest.of(0, limit);

        return jpaRepository.findNewJobsOrderedOldestFirst(pageable);
    }

    /**
     * Implementation of the filtered search. Maps domain types to JDBC-compatible
     * types for the native SQL query execution.
     *
     * @param filter the criteria to apply
     * @param limit  maximum results
     * @param offset pagination offset
     * @return filtered list of job entities
     */
    @Override
    @Transactional
    public List<JobEntity> fetchNewWithFilters(JobFilter filter, int limit, int offset) {
        String location = filter.hasLocation() ? filter.getLocation() : null;
        String seniority = filter.hasSeniority() ? filter.getSeniority().name() : null;

        String[] keywords = null;
        if (filter.hasKeywords()) {
            keywords = filter.getKeywords().stream()
                    .map(String::toLowerCase)
                    .toArray(String[]::new);
        }
        return jpaRepository.findNewJobsWithFilters(
                location,
                seniority,
                keywords,
                limit,
                offset
        );
    }

    /**
     * Marks jobs as CONSUMED (NEW → CONSUMED transition).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Fetch all entities by IDs</li>
     *   <li>For each entity:
     *     <ul>
     *       <li>If state == NEW: transition to CONSUMED</li>
     *       <li>If state == CONSUMED: idempotent (already done)</li>
     *       <li>If state == STALE: skip (cannot consume stale jobs)</li>
     *     </ul>
     *   </li>
     *   <li>Spring Data JPA batch UPDATE</li>
     *   <li>Transaction commits</li>
     * </ol>
     *
     * <p><strong>State transition:</strong>
     * <pre>
     * NEW → CONSUMED
     * </pre>
     *
     * <p><strong>Updates:</strong>
     * <ul>
     *   <li>state = CONSUMED</li>
     *   <li>state_changed_at = now</li>
     *   <li>entered_new_at = NULL (no longer in queue)</li>
     * </ul>
     *
     * <p><strong>Idempotency:</strong>
     * Safe to call multiple times with same IDs:
     * <ul>
     *   <li>NEW → CONSUMED (first call)</li>
     *   <li>CONSUMED → CONSUMED (subsequent calls, no-op)</li>
     * </ul>
     *
     * <p><strong>Batch operation:</strong>
     * Processes multiple IDs in single transaction for efficiency.
     *
     * @param ids list of job IDs to mark as consumed
     * @param now current timestamp (for state_changed_at)
     */
    @Override
    public JobRepository.ConsumptionStats markConsumed(List<UUID> ids, Instant now) {
        // 1. Fetch all entities by IDs
        List<JobEntity> entities = jpaRepository.findAllById(ids);

        int marked = 0;
        int alreadyConsumed = 0;

        // 2. Update state for each entity
        for  (JobEntity entity : entities) {
            // Only transition NEW → CONSUMED (idempotent if already CONSUMED)
            if (entity.getState() == JobState.NEW) {
                entity.setState(JobState.CONSUMED);
                entity.setStateChangedAt(now);
                entity.setEnteredNewAt(null); // Remove from NEW queue
                marked++;
            }else if (entity.getState() == JobState.CONSUMED) {
                alreadyConsumed++;
            }
            // If already CONSUMED or STALE: no-op (idempotent)
        }

        // 3. Batch save (Spring Data JPA generates UPDATE for changed entities)
        jpaRepository.saveAll(entities);
        // SQL: UPDATE jobs SET state='CONSUMED', state_changed_at=?, entered_new_at=NULL WHERE id IN (...)

        int notFound = ids.size() - entities.size();

        return new JobRepository.ConsumptionStats(marked, alreadyConsumed, notFound);
    }

    /**
     * Marks jobs as STALE (NEW → STALE transition, TTL cleanup).
     * <p>
     * <strong>Flow:</strong>
     * <ol>
     *   <li>Fetch all entities by IDs</li>
     *   <li>For each entity: transition NEW → STALE</li>
     *   <li>Spring Data JPA batch UPDATE</li>
     *   <li>Transaction commits</li>
     * </ol>
     *
     * <p><strong>State transition:</strong>
     * <pre>
     * NEW (>7 days) → STALE
     * </pre>
     *
     * <p><strong>Updates:</strong>
     * <ul>
     *   <li>state = STALE</li>
     *   <li>state_changed_at = now</li>
     *   <li>entered_new_at = NULL (no longer in queue)</li>
     * </ul>
     *
     * <p><strong>TTL cleanup context:</strong>
     * Called by scheduled task that identifies jobs where:
     * <pre>
     * state = 'NEW' AND entered_new_at < (now - 7 days)
     * </pre>
     *
     * <p><strong>Purpose:</strong>
     * Prevents NEW queue from growing indefinitely if Notifier is down.
     * Stale jobs are archived after 7 days.
     *
     * @param ids list of job IDs to mark as stale
     * @param now current timestamp (for state_changed_at)
     */
    @Override
    public void markStale(List<UUID> ids, Instant now) {
        // 1. Fetch all entities by IDs
        List<JobEntity> entities = jpaRepository.findAllById(ids);
        // 2. Update state for each entity
        for  (JobEntity entity : entities) {
            // Transition NEW → STALE
            if(entity.getState() == JobState.NEW) {
                entity.setState(JobState.STALE);
                entity.setStateChangedAt(now);
                entity.setEnteredNewAt(null);  // Remove from NEW queue
            }
            // If already STALE or CONSUMED: no-op
        }

        // 3. Batch save
        jpaRepository.saveAll(entities);
        // SQL: UPDATE jobs SET state='STALE', state_changed_at=?, entered_new_at=NULL WHERE id IN (...)
    }

    @Override
    @Transactional
    public List<JobEntity> findStaleJobs(Instant cutoff) {
        return jpaRepository.findStaleJobs(cutoff);
    }


    /**
     * Fetches all jobs with pagination, filters, and sorting.
     *
     * @param page           page number (0-based)
     * @param size           page size
     * @param seniorities      optional seniority filter
     * @param employmentTypes optional employment type filter
     * @param locations       optional location filter (partial match)
     * @param sources         optional source filter
     * @param sortBy         sort field name
     * @param sortOrder      sort direction (ASC/DESC)
     * @return list of JobEntity for current page
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobEntity> fetchAllPaginated(int page, int size, Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String sortBy, String sortOrder) {

        int offset = (page) * size;
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        boolean useSalarySort = "salary".equals(sortBy);
        String dbSortField = mapToDbColumn(sortBy);
        String[] seniorityParams = (seniorities!=null && seniorities.length>0)
                ? Arrays.stream(seniorities).map(Enum::name).toArray(String[]::new) : null;
        String[] employmentTypeParams = (employmentTypes!=null && employmentTypes.length>0)
                ? Arrays.stream(employmentTypes).map(Enum::name).toArray(String[]::new) : null;
        String[] locationParams = (locations !=null && locations.length>0)
                ? Arrays.stream(locations).map(loc -> "%" + loc.trim() + "%").toArray(String[]::new) : null;
        String[] sourceParams = (sources !=null && sources.length>0)
                ? sources : null;

        if (isAsc){
            return jpaRepository.findJobsUniversalAsc(seniorityParams,employmentTypeParams,locationParams,sourceParams,dbSortField,useSalarySort,size,offset);
        }else{
            return jpaRepository.findJobsUniversalDesc(seniorityParams,employmentTypeParams,locationParams,sourceParams,dbSortField,useSalarySort,size,offset);
        }
    }

    private String mapToDbColumn(String javaField) {
        return switch (javaField){
            case "publishedDate" -> "publishedDate";
            case "company" -> "company";
            case  "salary" -> "salary";
            default -> javaField;
        };
    }



    @Override
    @Transactional(readOnly = true)
    public long countAll(Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources) {

        boolean hasFilters = (seniorities != null && seniorities.length > 0) ||
                (employmentTypes != null && employmentTypes.length > 0) ||
                (locations != null && locations.length > 0) ||
                (sources != null && sources.length > 0);

        if (!hasFilters) {
            return jpaRepository.count();
        }else{

            String[] sParams = (seniorities !=null) ? Arrays.stream(seniorities).map(Enum::name).toArray(String[]::new) : null;
            String[] eParams = (employmentTypes !=null) ? Arrays.stream(employmentTypes).map(Enum::name).toArray(String[]::new) : null;
            String[] locPatterns = (locations != null && locations.length>0)
                    ? Arrays.stream(locations).map(loc -> "%" + loc.trim() + "%").toArray(String[]::new) : null;

            return jpaRepository.countWithFilters(sParams, eParams,locPatterns,sources);
        }
    }

}