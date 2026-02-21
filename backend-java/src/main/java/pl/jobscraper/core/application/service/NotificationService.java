package pl.jobscraper.core.application.service;

import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import pl.jobscraper.core.application.dto.JobFilter;
import pl.jobscraper.core.infrastructure.notifier.INotifier;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;
import pl.jobscraper.core.infrastructure.persistence.repository.IJobProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service responsible for coordinating the job notification process.
 * It fetches new job offers from the provider and broadcasts them to all
 * registered notification channels.
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final IJobProvider provider;
    private final List<INotifier> notifiers;

    /**
     * Constructs the service with a job provider and a list of available notifiers.
     * Spring automatically injects all beans implementing the {@link INotifier} interface.
     *
     * @param provider  The source of job postings.
     * @param notifiers A list of communication channels (e.g., Discord, Email).
     */
    @Autowired
    public NotificationService(IJobProvider provider, List<INotifier> notifiers) {
        this.provider = provider;
        this.notifiers = notifiers;
        logger.info("NotificationService initialized successfully with {} notifiers", notifiers.size());
    }

    /**
     * Executes a single notification cycle.
     * Fetches new jobs and, if any are found, iterates through all registered
     * notifiers to deliver the updates.
     * If any notifier sent notifications, the jobs from lists are marked as CONSUMED.
     *
     * @throws IOException          If an I/O error occurs during data retrieval or sending.
     * @throws InterruptedException If the process is interrupted during execution.
     */
    @Retryable(
            backoff = @Backoff(delay = 5000),
            recover = "recoverNotificationCycle"
    )
    public void runNotificationCycle() throws IOException, InterruptedException {
        List<JobEntity> jobs = provider.getNewJobs(JobFilter.none());

        if(jobs.isEmpty()) {
            logger.info("No new jobs found, skipping notifications");
            return;
        }

        logger.info("Found {} new jobs, sending notifications", jobs.size());

        boolean consumed = false;

        for(INotifier notifier : notifiers) {
            try {
                boolean sent = notifier.send(jobs);

                if (sent) consumed = true;
            } catch (Exception e) {
                logger.error("Notifier {} failed: {}", notifier.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
        }

        if(consumed) provider.makeConsumedNotifications(jobs);
    }

    @Recover
    public void recoverNotificationCycle(IOException e) {
        logger.error("CRITICAL: All retry attempts failed for notification service.");
        logger.error("Exception type: {}", e.getClass().getSimpleName());
        logger.error("Exception message: {}", e.getMessage());
    }
}