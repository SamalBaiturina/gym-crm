package com.epam.gymcrm.infrastructure.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final AtomicReference<String> idSeenByTheApplication = new AtomicReference<>();
    private final FilterChain recordingChain =
            (req, res) -> idSeenByTheApplication.set(MDC.get(TransactionIdFilter.MDC_KEY));

    @Test
    void generatesAnIdWhenTheCallerSentNone() throws Exception {
        filter.doFilter(request, response, recordingChain);

        String id = idSeenByTheApplication.get();
        assertNotNull(id);
        UUID.fromString(id);
        assertEquals(id, response.getHeader(TransactionIdFilter.HEADER));
    }

    @Test
    void keepsAValidIdFromTheCaller() throws Exception {
        request.addHeader(TransactionIdFilter.HEADER, "order-42.A_b");

        filter.doFilter(request, response, recordingChain);

        assertEquals("order-42.A_b", idSeenByTheApplication.get());
        assertEquals("order-42.A_b", response.getHeader(TransactionIdFilter.HEADER));
    }

    @ParameterizedTest
    @ValueSource(strings = {"has spaces", "line\nbreak", "{json}", ""})
    void replacesAnInvalidIdFromTheCaller(String incoming) throws Exception {
        request.addHeader(TransactionIdFilter.HEADER, incoming);

        filter.doFilter(request, response, recordingChain);

        assertNotEquals(incoming, idSeenByTheApplication.get());
        UUID.fromString(idSeenByTheApplication.get());
    }

    @Test
    void replacesAnIdThatIsTooLong() throws Exception {
        request.addHeader(TransactionIdFilter.HEADER, "a".repeat(65));

        filter.doFilter(request, response, recordingChain);

        UUID.fromString(idSeenByTheApplication.get());
    }

    @Test
    void clearsTheIdAfterTheRequest() throws Exception {
        filter.doFilter(request, response, recordingChain);

        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }

    @Test
    void clearsTheIdEvenWhenTheRequestFails() {
        FilterChain failingChain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, failingChain));
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }
}
