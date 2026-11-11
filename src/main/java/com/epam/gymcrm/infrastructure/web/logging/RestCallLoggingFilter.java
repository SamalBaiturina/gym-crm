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
 * Logs one line per REST call: the endpoint, the status, the time, the request body and the
 * response body (for an error that is the error message). Passwords are masked. Headers are
 * never logged, so the Authorization header cannot leak. The level depends on the status:
 * INFO below 400, WARN for 4xx, ERROR for 5xx.
 * <p>
 * The request body is logged only if the application has read it: a request rejected earlier
 * (for example with 401) is logged without a body.
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RestCallLoggingFilter.class);

    private static final String FORMAT = "{} -> {} ({} ms) request={} response={}";
    private static final int MAX_BODY_LENGTH = 1000;
    private static final Pattern SECRET_FIELDS =
            Pattern.compile("(\"(?:password|newPassword)\"\\s*:\\s*)\"(?:[^\"\\\\]|\\\\.)*\"");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
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

    /** Replaces the value of the password fields in a JSON text with "***". */
    public static String mask(String json) {
        return SECRET_FIELDS.matcher(json).replaceAll("$1\"***\"");
    }

    private void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                         long millis) {
        int status = response.getStatus();
        String endpoint = request.getMethod() + " " + request.getRequestURI()
                + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        String requestBody = describe(request.getContentAsByteArray());
        String responseBody = describe(response.getContentAsByteArray());
        if (status >= 500) {
            log.error(FORMAT, endpoint, status, millis, requestBody, responseBody);
        } else if (status >= 400) {
            log.warn(FORMAT, endpoint, status, millis, requestBody, responseBody);
        } else {
            log.info(FORMAT, endpoint, status, millis, requestBody, responseBody);
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
}
