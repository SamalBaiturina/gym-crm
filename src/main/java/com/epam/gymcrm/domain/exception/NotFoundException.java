package com.epam.gymcrm.domain.exception;

import java.util.NoSuchElementException;

/**
 * A requested object does not exist (trainee, trainer, training type).
 * It extends NoSuchElementException, so code that catches the standard exception still works.
 */
public class NotFoundException extends NoSuchElementException {

    public NotFoundException(String message) {
        super(message);
    }
}
