package pl.jobscraper.core.infrastructure.persistence.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import pl.jobscraper.core.domain.identity.JobIdentity;
import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.domain.model.JobState;
import pl.jobscraper.core.domain.port.JobRepository;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;
import pl.jobscraper.core.infrastructure.persistence.mapper.JobEntityMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            String canonicalUrl = identity.getCanonicalUrl()
                    .orElseThrow(()-> new IllegalStateException("URL-based identity has no URL"));
            return jpaRepository.findByCanonicalUrl(canonicalUrl);
        }else{
            String fallbackHash = identity.getFallbackHash()
                    .orElseThrow(()-> new IllegalStateException("Hash-based identity has no hash"));
            return jpaRepository.findByFallbackHash(fallbackHash);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> fetchNewOldestFirst(int limit) {

        Pageable pageable = PageRequest.of(0, limit);

        List<JobEntity> entities = jpaRepository.findNewJobsOrderedOldestFirst(pageable);

        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public void markConsumed(List<UUID> ids, Instant now) {
        List<JobEntity> entities = jpaRepository.findAllById(ids);

        for  (JobEntity entity : entities) {
            if (entity.getState() == JobState.NEW) {
                entity.setState(JobState.CONSUMED);
                entity.setStateChangedAt(now);
                entity.setEnteredNewAt(null);
            }
        }
        jpaRepository.saveAll(entities);
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
}
