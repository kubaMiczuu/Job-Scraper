package pl.jobscraper.core.domain.model.value;

import pl.jobscraper.core.domain.model.Job;

/**
 * Standardized employment contract types.
 * <p>
 * Aggregates various market-specific contracts into high-level categories
 * to simplify filtering and notification logic.
 *
 * @see Job#getEmploymentType()
 */
public enum EmploymentType {

    /**
     * permanent employment contract (pl: Umowa o Pracę).
     */
    UOP,

    /**
     * Self-employment or service agreement (Business-to-Business).
     */
    B2B,

    /**
     * Training positions and student programs.
     */
    INTERNSHIP,

    /**
     * Any other types, including mandate contracts (pl: Umowa Zlecenie) or unspecified.
     */
    OTHER
}