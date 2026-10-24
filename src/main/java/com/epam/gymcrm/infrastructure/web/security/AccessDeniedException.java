package com.epam.gymcrm.infrastructure.web.security;

/** The caller is known, but tries to reach data of another user. Mapped to 403. */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
