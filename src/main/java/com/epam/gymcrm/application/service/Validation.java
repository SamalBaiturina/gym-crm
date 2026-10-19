package com.epam.gymcrm.application.service;

import com.epam.gymcrm.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks that required fields are filled before create/update.
 */
final class Validation {

    private static final Logger log = LoggerFactory.getLogger(Validation.class);

    private Validation() {
    }

    /** Runs the checks; if one of them fails, logs a warning with the context and rethrows the exception. */
    static void validated(String context, Runnable checks) {
        try {
            checks.run();
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed ({}): {}", context, e.getMessage());
            throw e;
        }
    }

    static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    static void requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }

    static void requireUser(User user) {
        requireNotNull(user, "user");
        requireText(user.firstName(), "firstName");
        requireText(user.lastName(), "lastName");
    }
}
