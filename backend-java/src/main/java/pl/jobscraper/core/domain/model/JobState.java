package pl.jobscraper.core.domain.model;

/**
 * Technical lifecycle states of a job posting.
 * <p>
 * Tracks the notification status within the system. This is an infrastructure
 * concern used to coordinate data flow between the Scraper and the Notifier.
 * * <p>The domain model {@link Job} remains stateless; these transitions are managed
 * by the persistence layer to ensure reliable delivery.
 *
 * @see pl.jobscraper.core.infrastructure.persistence.entity.JobEntity
 */
public enum JobState {
    /** Initial state for newly discovered jobs.
     * Ready to be fetched by notification services.
     */
    NEW,

    /** Terminal state for successfully processed jobs.
     * Indicates that the Notifier has acknowledged receipt and delivery.
     */
    CONSUMED,

    /** Expiration state for jobs not processed within the defined retention period.
     * Prevents queue congestion by bypassing old or irrelevant postings.
     */
    STALE
}