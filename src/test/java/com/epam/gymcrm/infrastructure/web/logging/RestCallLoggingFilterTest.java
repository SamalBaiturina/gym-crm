package com.epam.gymcrm.infrastructure.web.logging;

import ch.qos.logback.classic.Level;
import com.epam.gymcrm.support.LogCapture;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestCallLoggingFilterTest {

    private final RestCallLoggingFilter filter = new RestCallLoggingFilter();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static MockHttpServletRequest request(String method, String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setContent(body.getBytes());
        return request;
    }

    /** An application that reads the request body and answers with the given status and body. */
    private static FilterChain application(int status, String responseBody) {
        return (req, res) -> {
            req.getInputStream().readAllBytes();
            ((HttpServletResponse) res).setStatus(status);
            res.setContentType("application/json");
            res.getWriter().write(responseBody);
        };
    }

    @Test
    void maskHidesPasswordValuesAndKeepsTheRest() {
        assertEquals("{\"password\":\"***\"}", RestCallLoggingFilter.mask("{\"password\":\"abc\"}"));
        assertEquals("{\"newPassword\": \"***\"}", RestCallLoggingFilter.mask("{\"newPassword\": \"x y\"}"));
        assertEquals("{\"username\":\"John\",\"password\":\"***\"}",
                RestCallLoggingFilter.mask("{\"username\":\"John\",\"password\":\"a\\\"b\"}"));
        assertEquals("{\"firstName\":\"John\"}", RestCallLoggingFilter.mask("{\"firstName\":\"John\"}"));
    }

    @Test
    void maskHidesAPasswordThatWasCutOff() {
        assertEquals("{\"password\":\"***\"", RestCallLoggingFilter.mask("{\"password\":\"abc"));
        assertEquals("{\"password\":\"***\"", RestCallLoggingFilter.mask("{\"password\":\"ab\\"));
    }

    @Test
    void applicationStillReadsTheWholeBodyWhenItIsLargerThanTheCacheLimit() throws Exception {
        String body = "{\"data\":\"" + "x".repeat(20_000) + "\"}";
        FilterChain readsEverything = (req, res) ->
                res.getWriter().write(String.valueOf(req.getInputStream().readAllBytes().length));

        filter.doFilter(request("POST", "/x", body), response, readsEverything);

        assertEquals(String.valueOf(body.length()), response.getContentAsString());
    }

    @Test
    void successfulCallIsLoggedAtInfoWithoutPasswords() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request("POST", "/trainees", "{\"firstName\":\"John\"}"), response,
                    application(200, "{\"username\":\"John.Smith\",\"password\":\"secret12345\"}"));

            List<String> lines = logs.messages(Level.INFO);
            assertEquals(1, lines.size());
            String line = lines.get(0);
            assertTrue(line.contains("POST /trainees -> 200"));
            assertTrue(line.contains("request={\"firstName\":\"John\"}"));
            assertTrue(line.contains("\"password\":\"***\""));
            assertFalse(line.contains("secret12345"));
        }
    }

    @Test
    void clientStillReceivesTheResponseBody() throws Exception {
        filter.doFilter(request("GET", "/x", ""), response, application(200, "{\"a\":1}"));

        assertEquals("{\"a\":1}", response.getContentAsString());
        assertEquals(200, response.getStatus());
    }

    @Test
    void clientErrorIsLoggedAtWarnWithTheErrorMessage() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request("GET", "/login", ""), response,
                    application(401, "{\"type\":\"UNAUTHORIZED\",\"details\":[\"Invalid username or password\"]}"));

            List<String> lines = logs.messages(Level.WARN);
            assertEquals(1, lines.size());
            assertTrue(lines.get(0).contains("GET /login -> 401"));
            assertTrue(lines.get(0).contains("Invalid username or password"));
            assertTrue(lines.get(0).contains("request=-"));
        }
    }

    @Test
    void serverErrorIsLoggedAtError() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request("GET", "/x", ""), response, application(500, "{\"type\":\"INTERNAL_ERROR\"}"));

            assertEquals(1, logs.messages(Level.ERROR).size());
        }
    }

    @Test
    void queryStringIsPartOfTheEndpoint() throws Exception {
        MockHttpServletRequest request = request("GET", "/trainees/John.Smith/trainings", "");
        request.setQueryString("periodFrom=2026-01-01");
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request, response, application(200, "[]"));

            assertTrue(logs.messages(Level.INFO).get(0)
                    .contains("GET /trainees/John.Smith/trainings?periodFrom=2026-01-01 -> 200"));
        }
    }

    @Test
    void longBodiesAreCut() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request("GET", "/x", ""), response, application(200, "x".repeat(5000)));

            String line = logs.messages(Level.INFO).get(0);
            assertTrue(line.contains("...(truncated)"));
            assertTrue(line.length() < 1300);
        }
    }

    @Test
    void actuatorCallsAreNotLogged() throws Exception {
        try (LogCapture logs = new LogCapture(RestCallLoggingFilter.class)) {
            filter.doFilter(request("GET", "/actuator/health", ""), response,
                    application(200, "{\"status\":\"UP\"}"));

            assertTrue(logs.messages(Level.INFO).isEmpty());
        }
        assertEquals("{\"status\":\"UP\"}", response.getContentAsString());
    }
}
