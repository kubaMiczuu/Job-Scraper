package pl.jobscraper.core.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Global API error representation.
 * <p>Provides a consistent structure for all 4xx and 5xx responses across the system.
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
