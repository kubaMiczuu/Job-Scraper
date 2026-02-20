package pl.jobscraper.core.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pl.jobscraper.core.api.dto.ErrorResponse;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * <p>
 * Catches exceptions thrown by controllers and converts them
 * to consistent JSON error responses.
 *
 * <p><strong>Handled exceptions:</strong>
 * <ul>
 *   <li>MethodArgumentNotValidException - validation errors (400)</li>
 *   <li>IllegalArgumentException - bad arguments (400)</li>
 *   <li>MethodArgumentTypeMismatchException - type conversion (400)</li>
 *   <li>Exception - all other errors (500)</li>
 * </ul>
 *
 * <p><strong>Response format:</strong>
 * All errors return ErrorResponse DTO with consistent structure:
 * timestamp, status, error, message, path, details (optional)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors (Spring Boot 4.x).
     * <p>
     * Triggered when @Valid annotation fails on request parameters.
     * New in Spring Boot 4.x - replaces some MethodArgumentNotValidException cases.
     *
     * @param ex      validation exception
     * @param request HTTP request
     * @return 400 Bad Request with validation details
     */
    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(org.springframework.web.method.annotation.HandlerMethodValidationException ex, HttpServletRequest request) {

        List<String> details = ex.getAllErrors().stream()
                        .map(error->{
                            String fieldName = "field";
                            if(error.getCodes() != null && error.getCodes().length > 0) {
                                for (String code : error.getCodes()) {
                                    int lastDot = code.lastIndexOf('.');
                                    if(lastDot >0 && lastDot < code.length()-1) {
                                        fieldName = code.substring(lastDot + 1);
                                        break;
                                    }
                                }
                            }
                            String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "validation failed";
                            return fieldName + ": " + message;
                        }).toList();


        log.warn("Handler method validation failed for {}: {}", request.getRequestURI(), details);

        ErrorResponse errorResponse = ErrorResponse.withDetails(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request parameters",
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles validation errors (Bean Validation).
     * <p>
     * Triggered when @Valid annotation fails on request DTO.
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * POST /api/jobs with empty title
     * → 400 Bad Request
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Invalid request body",
     *   "details": ["title: must not be blank"]
     * }
     * }</pre>
     *
     * @param ex      validation exception
     * @param request HTTP request
     * @return 400 Bad Request with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error->{
                    if(error instanceof FieldError fieldError){
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        log.warn("Validation failed for {}: {}", request.getRequestURI(), details);

        ErrorResponse errorResponse = ErrorResponse.withDetails(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request body",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles IllegalArgumentException (business logic errors).
     * <p>
     * Triggered by service layer validation.
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * POST /mark-consumed with empty IDs list
     * → 400 Bad Request
     * {
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "IDs list cannot be null or empty"
     * }
     * }</pre>
     *
     * @param ex      illegal argument exception
     * @param request HTTP request
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument for {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles type conversion errors (query params, path variables).
     * <p>
     * Triggered when parameter type doesn't match expected type.
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * GET /api/jobs/all?page=abc
     * → 400 Bad Request
     * {
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Invalid parameter 'page': expected type Integer"
     * }
     * }</pre>
     *
     * @param ex      type mismatch exception
     * @param request HTTP request
     * @return 400 Bad Request with type error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format(
                "Invalid parameter '%s': expected type %s",
                ex.getName(),
                ex.getRequiredType() !=  null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        log.warn("Type mismatch for {}: {}", request.getRequestURI(), message);

        ErrorResponse errorResponse = ErrorResponse.of(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all other uncaught exceptions.
     * <p>
     * Catches unexpected errors (database down, NPE, etc.).
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * GET /api/jobs/all (database down)
     * → 500 Internal Server Error
     * {
     *   "status": 500,
     *   "error": "Internal Server Error",
     *   "message": "An unexpected error occurred"
     * }
     * }</pre>
     *
     * @param ex      any exception
     * @param request HTTP request
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error for {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occured",
                request.getRequestURI()
        );

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
