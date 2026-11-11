package com.epam.gymcrm.infrastructure.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Gives every request a transaction id. The id is taken from the X-Transaction-Id header if the
 * caller sent one, otherwise it is generated. It is put into the logging context (key
 * "transactionId"), so every log line of the request carries it, and it is returned in the
 * response header. The same header can be passed on to other services.
 */
public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    /** Only short, plain ids are accepted, so a client cannot put line breaks or junk into the log. */
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String transactionId = request.getHeader(HEADER);
        if (transactionId == null || !VALID_ID.matcher(transactionId).matches()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, transactionId);
        response.setHeader(HEADER, transactionId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
