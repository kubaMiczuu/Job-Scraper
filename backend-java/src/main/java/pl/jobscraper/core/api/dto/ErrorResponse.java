package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response DTO.
 * <p>
 * Returned for all errors (4xx, 5xx) with consistent structure.
 *
 * <p><strong>Example JSON:</strong>
 * <pre>{@code
 * {
 *   "timestamp": "2026-02-17T14:30:00Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "path": "/api/jobs",
 *   "details": [
 *     "title: must not be blank",
 *     "company: must not be blank"
 *   ]
 * }
 * }</pre>
 *
 * @param timestamp when error occurred (ISO-8601)
 * @param status    HTTP status code (400, 404, 500, etc.)
 * @param error     HTTP status text (Bad Request, Not Found, etc.)
 * @param message   error message (user-friendly)
 * @param path      request path that caused error
 * @param details   optional validation details (field errors)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING,  pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        @JsonProperty("details")
        List<String> details

) {

    /**
     * Creates error response without details.
     *
     * @param timestamp when error occurred
     * @param status    HTTP status code
     * @param error     HTTP status text
     * @param message   error message
     * @param path      request path
     * @return ErrorResponse without details
     */
    public static ErrorResponse of(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ){
        return new ErrorResponse(timestamp,status,error,message,path,null);
    }

    /**
     * Creates error response with validation details.
     *
     * @param timestamp when error occurred
     * @param status    HTTP status code
     * @param error     HTTP status text
     * @param message   error message
     * @param path      request path
     * @param details   validation details
     * @return ErrorResponse with details
     */
    public static ErrorResponse withDetails(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<String> details
    ){
        return new ErrorResponse(timestamp,status,error,message,path,details);
    }
}
