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
     * Umowa zlecenie (Contract of mandate).
     */
    UZ,

    /**
     * Umowa o dzieło (Contract for specific work).
     */
    UOD,

    /**
     * Permanent employment (mapped from various sources).
     * Usually means full-time permanent position.
     */
    PERMANENT,

    /**
     * Contract/temporary employment.
     */
    CONTRACT,

    /**
     * Training positions and student programs.
     */
    INTERNSHIP,

    /**
     * Any other types, including mandate contracts (pl: Umowa Zlecenie) or unspecified.
     */
    OTHER;

    public static EmploymentType fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        String normalized = value.trim().toLowerCase();

        return switch (normalized) {
            case "b2b", "bussines to bussines " -> B2B;
            case "permanent", "full-time", "pełny etat", "etat" -> PERMANENT;
            case "contract", "temporary", "fixed-term", "temporary contract" -> CONTRACT;
            case "uop", "umowa o prace", "umowa o pracę" -> UOP;
            case "uz", "umowa zlecenie" -> UZ;
            case "uod", "umowa o dzieło", "umowa o dzielo" -> UOD;
            case "internship", "intern", "staż", "staz", "praktyka" -> INTERNSHIP;
            default -> {
                try {
                    yield EmploymentType.valueOf(normalized.toUpperCase());
                } catch (IllegalArgumentException e) {
                    yield OTHER;  // Unknown type
                }
            }
        };
    }

}