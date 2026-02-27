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
 * Infrastructure adapter for PostgreSQL persistence.
 * <p>
 * Orchestrates the mapping between domain business objects and JPA entities,
 * delegating actual SQL execution to Spring Data JPA.
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

    public JobRepositoryImpl(SpringDataJobJpaRepository jpaRepository, JobEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void saveNew(Job job, JobIdentity identity, Instant now) {
        JobEntity entity = mapper.toEntity(job, identity, JobState.NEW, now);
        jpaRepository.save(entity);
    }

    @Override
    public void updateExisting(UUID id, Job job, Instant now) {
        JobEntity entity = jpaRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Job not found with ID: " + id));
        mapper.updateEntityFromJob(entity, job);
        entity.setUpdatedAt(now);
        jpaRepository.save(entity);
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<JobEntity> fetchNewOldestFirst(int limit) {
        // 1. Create pagination (page 0, size = limit)
        Pageable pageable = PageRequest.of(0, limit);

        return jpaRepository.findNewJobsOrderedOldestFirst(pageable);
    }

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

    @Override
    public JobRepository.ConsumptionStats markConsumed(List<UUID> ids, Instant now) {
        List<JobEntity> entities = jpaRepository.findAllById(ids);

        int marked = 0;
        int alreadyConsumed = 0;

        for  (JobEntity entity : entities) {
            if (entity.getState() == JobState.NEW) {
                entity.setState(JobState.CONSUMED);
                entity.setStateChangedAt(now);
                entity.setEnteredNewAt(null);
                marked++;
            }else if (entity.getState() == JobState.CONSUMED) {
                alreadyConsumed++;
            }
        }
        jpaRepository.saveAll(entities);
        int notFound = ids.size() - entities.size();
        return new JobRepository.ConsumptionStats(marked, alreadyConsumed, notFound);
    }

    @Override
    public void markStale(List<UUID> ids, Instant now) {
        List<JobEntity> entities = jpaRepository.findAllById(ids);
        for  (JobEntity entity : entities) {
            if(entity.getState() == JobState.NEW) {
                entity.setState(JobState.STALE);
                entity.setStateChangedAt(now);
                entity.setEnteredNewAt(null);
            }
        }
        jpaRepository.saveAll(entities);
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
    public List<JobEntity> fetchAllPaginated(int page, int size, Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] keywords,String sortBy, String sortOrder) {

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
        String[] keywordParams = (keywords != null && keywords.length>0)
                ? keywords : null;

        if (isAsc){
            return jpaRepository.findJobsUniversalAsc(seniorityParams,employmentTypeParams,locationParams,sourceParams, keywordParams, dbSortField,useSalarySort,size,offset);
        }else{
            return jpaRepository.findJobsUniversalDesc(seniorityParams,employmentTypeParams,locationParams,sourceParams, keywordParams, dbSortField,useSalarySort,size,offset);
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
    public long countAll(Seniority[] seniorities, EmploymentType[] employmentTypes, String[] locations, String[] sources, String[] keywords) {

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

            return jpaRepository.countWithFilters(sParams, eParams,locPatterns,sources,keywords);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctSeniorities() {return jpaRepository.findDistinctSeniorities();}
    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctEmploymentTypes() {return jpaRepository.findDistinctEmploymentTypes();}
    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctLocations() {return jpaRepository.findDistinctLocations();}
    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctSources() {return jpaRepository.findDistinctSources();}

}