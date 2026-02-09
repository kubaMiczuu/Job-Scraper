package pl.jobscraper.core.domain.model.value;

import pl.jobscraper.core.domain.model.Job;

/**
 * Types of employment contracts offered in job postings.
 * <p>
 * This enum represents the employment/contract type for a job.
 * It is an optional field in {@link pl.jobscraper.core.domain.model.Job}.
 *
 * <p><strong>Context:</strong> Employment types are specific to Polish job market
 * but can be adapted for other markets by mapping local contracts types to these values.
 *
 * <p><strong>Usage in filtering:</strong>
 * Employment type can be used as a filter in {@code GET /api/jobs/new?employmentType=B2B}
 * to return only jobs with specified contract type.
 *
 * @see Job#getEmploymentType()
 */
public enum EmploymentType {

    /**
     * Umowa o Pracę (Employment Contract) - Polish permanent employment.
     * <p>
     * Standard employment contract in Poland with:
     * <ul>
     *   <li>Full social security (ZUS)</li>
     *   <li>Paid vacation (20-26 days)</li>
     *   <li>Sick leave</li>
     *   <li>Employment protection</li>
     * </ul>
     *
     * <p>Tax: ~31% income tax + social contributions.
     */
    UOP,

    /**
     * Business-to-Business (B2B) contract - self-employment/contractor.
     * <p>
     * Common in Polish IT market. Developer operates as a business entity
     * (jednoosobowa działalność gospodarcza or spółka) and invoices the client.
     *
     * <p>Characteristics:
     * <ul>
     *   <li>No paid vacation (typically negotiated higher rate to compensate)</li>
     *   <li>No sick leave</li>
     *   <li>Flexible taxation (linear 19% or progressive scale)</li>
     *   <li>More flexibility, less employment protection</li>
     * </ul>
     */
    B2B,

    /**
     * Internship (staż, praktyka).
     * <p>
     * Temporary position for students or recent graduates, typically:
     * <ul>
     *   <li>Fixed duration (3-12 months)</li>
     *   <li>Lower compensation (or unpaid)</li>
     *   <li>Educational/training purpose</li>
     * </ul>
     */
    INTERNSHIP,

    /**
     * Other contract types.
     * <p>
     * Includes:
     * <ul>
     *   <li>Umowa zlecenie (contract of mandate)</li>
     *   <li>Umowa o dzieło (contract for specific work)</li>
     *   <li>Temporary agency work</li>
     *   <li>Volunteer work</li>
     *   <li>Unspecified or mixed contract types</li>
     * </ul>
     */
    OTHER
}