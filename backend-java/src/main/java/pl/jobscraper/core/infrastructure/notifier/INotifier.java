package pl.jobscraper.core.infrastructure.notifier;

import pl.jobscraper.core.domain.model.Job;
import pl.jobscraper.core.infrastructure.persistence.entity.JobEntity;

import java.io.IOException;
import java.util.List;

/**
 * Common interface for notification delivery services.
 * Any class implementing this interface is responsible for formatting
 * and broadcasting job offers to a specific destination or platform.
 */
public interface INotifier {

    /**
     * Sends a collection of job offers through the implemented communication channel.
     *
     * @param jobs A {@link List} of {@link Job} objects to be delivered.
     * @throws IOException          If a network or I/O error occurs during the sending process.
     * @throws InterruptedException If the operation is interrupted before completion.
     */
    boolean send(List<JobEntity> jobs) throws IOException, InterruptedException;
}
