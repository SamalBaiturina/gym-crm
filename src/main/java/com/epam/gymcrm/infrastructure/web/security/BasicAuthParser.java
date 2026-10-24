package com.epam.gymcrm.infrastructure.web.security;

import com.epam.gymcrm.application.service.AuthenticationException;
import com.epam.gymcrm.domain.exception.ValidationException;
import com.epam.gymcrm.domain.model.Credentials;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Reads "Authorization: Basic base64(username:password)". Any problem with the header means 401. */
public final class BasicAuthParser {

    private static final String PREFIX = "Basic ";

    private BasicAuthParser() {
    }

    /**
     * Reads username and password from the header value. Throws AuthenticationException (401) if the
     * header is missing or malformed.
     */
    public static Credentials parse(String header) {
        if (header == null || !header.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            throw new AuthenticationException("Authorization header with Basic credentials is required");
        }
        String decoded = decode(header.substring(PREFIX.length()).trim());
        int colon = decoded.indexOf(':');
        if (colon < 0) {
            throw malformed();
        }
        try {
            return new Credentials(decoded.substring(0, colon), decoded.substring(colon + 1));
        } catch (ValidationException e) {
            throw malformed();
        }
    }

    private static String decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw malformed();
        }
    }

    private static AuthenticationException malformed() {
        return new AuthenticationException("Authorization header is malformed");
    }
}
