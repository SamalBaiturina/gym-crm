package com.epam.gymcrm.infrastructure.web.error;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Turns exceptions from all controllers into JSON errors with the right HTTP status.
 * Extends ResponseEntityExceptionHandler so that Spring's own errors (405, 415 and so on)
 * keep their status and get the same JSON format instead of becoming 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", List.of(e.getMessage())));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", List.of(e.getMessage())));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"gym-crm\"")
                .body(new ApiError("UNAUTHORIZED", List.of(e.getMessage())));
    }

    /** Anything unexpected: the stack trace goes to the log, the client gets a general message. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", List.of("Unexpected error")));
    }

    /** Request body failed the @Valid checks: one detail per field. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(status).body(new ApiError("VALIDATION_ERROR", details));
    }

    /** Request body is missing or is not valid JSON. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError("MALFORMED_REQUEST", List.of("Request body is missing or malformed")));
    }

    /** Other errors raised by Spring MVC (405, 415, ...): keep the status, use our format. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String type = status == null ? "ERROR" : status.name();
        String reason = status == null ? "Request failed" : status.getReasonPhrase();
        return ResponseEntity.status(statusCode).headers(headers)
                .body(new ApiError(type, List.of(reason)));
    }
}
