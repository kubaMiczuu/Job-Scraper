package pl.jobscraper.core.domain.model.value;

import pl.jobscraper.core.domain.model.Job;

/**
 * Standardized job seniority levels.
 * <p>
 * Categorizes experience requirements into discrete tiers to enable
 * consistent filtering and notification matching across different job sources.
 *
 * @see Job#getSeniority()
 */
public enum Seniority {
    /**
     * Entry-level positions, trainees, and early-career developers.
     */
    JUNIOR,

    /**
     * Regular, intermediate positions for independent contributors.
     */
    MID,

    /**
     * Advanced positions requiring deep technical expertise and autonomy.
     */
    SENIOR,

    /**
     * Technical leadership, mentoring, and team-level accountability.
     */
    LEAD,

    /**
     * Expert or Principal positions focusing on deep specialization or architectural impact.
     */
    EXPERT
}