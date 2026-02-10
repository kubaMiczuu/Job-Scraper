package pl.jobscraper.core.domain.model.value;

import pl.jobscraper.core.domain.model.Job;

/**
 * Job seniority/experience levels.
 * <p>
 * This enum represents the required experience level fo a job posting.
 * It is an optional field in {@link pl.jobscraper.core.domain.model.Job}.
 *
 * <p><strong>Typical mappings from job descriptions:</strong>
 * <ul>
 *     <li>JUNIOR: 0-2 years experience, entry-level, trainee</li>
 *     <li>MID: 2-5 years experience, intermediate, regular</li>
 *     <li>SENIOR: 5+ years experience, expert, advanced</li>
 *     <li>LEAD: Team led, tech lead, principal, staff engineer</li>
 * </ul>
 *
 * <p><strong>Usage in filtering:</strong>
 * Seniority can be used as a filter in {@code GET /api/jobs/new?seniority=SENIOR}
 * to return only jobs matching specified experience level.
 *
 * @see Job#getSeniority()
 */
public enum Seniority {
    /**
     * Junior level (0-2 years experience).
     * <p>
     * Typically trainee, intern, junior developer, entry-level.
     */
    JUNIOR,

    /**
     * Mid-level (2-5 years experience).
     * <p>
     * Typically: regular developer, intermediate, mid-level.
     */
    MID,

    /**
     * Senior level (5+ years experience).
     * <p>
     * Typically: senior developer, expert, advanced, specialist.
     */
    SENIOR,

    /**
     * Lead level (team leadership).
     * <p>
     * Typically: tech lead, team lead, principal engineer, staff engineer,
     * engineering manager (with hands-on coding).
     */
    LEAD, EXPERT
}