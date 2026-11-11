package com.epam.gymcrm.domain.exception;

/** The caller is known, but is not allowed to perform this operation. Mapped to 403. */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
