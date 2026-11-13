package com.epam.gymcrm.infrastructure.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Logs one line per REST call: the method, the path, the status and the time. The level depends on
 * the status: INFO below 400, WARN for 4xx, ERROR for 5xx. The query string and the request and
 * response bodies (for an error that is the error message) can hold personal data, so they go to
 * a second line at DEBUG level only. Passwords are masked there. Headers are never logged,
 * so the Authorization header cannot leak.
 * <p>
 * The request body is logged only if the application has read it: a request rejected earlier
 * (for example with 401) has no body in the DEBUG line.
 * <p>
 * Calls to /actuator are not logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RestCallLoggingFilter.class);

    private static final String FORMAT = "{} -> {} ({} ms)";
    private static final String DETAILS_FORMAT = "{} request={} response={}";
    private static final int MAX_BODY_LENGTH = 1000;
    /** How much of a request body is kept for the log. The application still reads the whole body. */
    private static final int MAX_CACHED_REQUEST_BYTES = 10_000;
    /** A password value is masked even if the cache limit cut it off before the closing quote. */
    private static final Pattern SECRET_FIELDS = Pattern.compile(
            "(\"(?:password|newPassword)\"\\s*:\\s*)\"(?:[^\"\\\\]|\\\\.)*\\\\?(?:\"|$)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, MAX_CACHED_REQUEST_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long start = System.nanoTime();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logCall(wrappedRequest, wrappedResponse, (System.nanoTime() - start) / 1_000_000);
            // the response body was held back to be logged, now it goes to the client
            wrappedResponse.copyBodyToResponse();
        }
    }

    /** Replaces the value of the password fields in a JSON text with "***", also when the value is cut off. */
    public static String mask(String json) {
        return SECRET_FIELDS.matcher(json).replaceAll("$1\"***\"");
    }

    private void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                         long millis) {
        int status = response.getStatus();
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        if (status >= 500) {
            log.error(FORMAT, endpoint, status, millis);
        } else if (status >= 400) {
            log.warn(FORMAT, endpoint, status, millis);
        } else {
            log.info(FORMAT, endpoint, status, millis);
        }
        // the query string and the bodies can hold personal data: DEBUG only, and not even built otherwise
        if (log.isDebugEnabled()) {
            String query = request.getQueryString();
            log.debug(DETAILS_FORMAT, query == null ? endpoint : endpoint + "?" + query,
                    describe(request.getContentAsByteArray()), describe(response.getContentAsByteArray()));
        }
    }

    /** One line, passwords masked first and only then shortened, so a cut password cannot leak. */
    private static String describe(byte[] body) {
        if (body.length == 0) {
            return "-";
        }
        String text = mask(new String(body, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", " "));
        return text.length() > MAX_BODY_LENGTH ? text.substring(0, MAX_BODY_LENGTH) + "...(truncated)" : text;
    }

    /** Health checks and metric scrapes come every few seconds; logging them would bury the real calls. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }
}
