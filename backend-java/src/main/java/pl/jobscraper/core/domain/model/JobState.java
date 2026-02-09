package pl.jobscraper.core.domain.model;

/**
 * Lifecycle states of a job posting in the data layer.
 * <p>
 * This enum represents the technical state of a job in the persistence layer,
 * NOT business logic. The state tracks whether a job has been processed by
 * the Notifier service.
 *
 * <p><strong>State transitions: </strong>
 * <pre>
 * INSERT -> NEW -> (Notifier fetches and processes) -> CONSUMED
 * NEW -> (7 days, not consumed) -> STALE
 * </pre>
 *
 * <p><strong>Why states are in persistence layer, not domain:</strong>
 * The {@link Job} domain model does NOT have a state field. Job is a pure
 * business object. JobState is a technical concern managed by the persistence
 * layer {@link pl.jobscraper.core.infrastructure.persistence.entity.JobEntity} to track notification status.
 *
 * @see pl.jobscraper.core.infrastructure.persistence.entity.JobEntity
 */
public enum JobState {
    /**
     * Ready for notification.
     * <p>
     * A job enters NEW state when:
     * <ul>
     *     <li>First inserted into database (never seen before)</li>
     *     <li>NOT when updated (updates don't repromote to NEW)</li>
     * </ul>
     *
     * <p>Jobs in NEW state returned by {@code GET /api/jobs/new} endpoint
     * in deterministic order (oldest first: {@code entered_new_at ASC, id ASC}).
     *
     * <p><strong>TTL (Time-To-Live):</strong> 7 days. If not consumed within
     * 7 days, job transitions to STALE (automatic cleanup).
     */
    NEW,
    /**
     * Successfully processed by Notifier.
     * <p>
     * Transition: NEW -> CONSUMED occurs when Notifier calls
     * {@code POST /api/jobs/mark-consumed} after successfully sending notifications.
     *
     * <p><strong>Read-then-mark pattern:</strong>
     * <ol>
     *     <li>Notifier: GET /api/jobs/new (fetch NEW jobs)</li>
     *     <li>Notifier: Process and send notifications</li>
     *     <li>Notifier: POST /api/jobs/mark-consumed (mark as CONSUMED)</li>
     * </ol>
     *
     * <p>If Notifier crashes between steps 2-3, jobs remain NEW and will be
     * re-fetched on next run (at-least-once delivery guarantee).
     */
    CONSUMED,
    /**
     * Expired (not consumed within TTL).
     * <p>
     * Jobs transition NEW -> STALE after 7 days if not consumed by Notifier.
     * This prevents the NEW queue from growing indefinitely if Notifier is down.
     *
     * <p><strong>Cleanup mechanism:</strong>
     * A scheduled task runs daily (e.g., 3:00 AM) and moves jobs where
     * <pre>{@code
     * state = 'NEW' AND entered_new_at < (NOW() - INTERVAL '7 days')
     * }</pre>
     * to STALE state.
     *
     * <p>STALE jobs are NOT returned by {@code GET /api/jobs/new}.
     */
    STALE
}