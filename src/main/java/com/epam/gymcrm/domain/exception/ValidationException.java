package com.epam.gymcrm.domain.exception;

/**
 * The given data is not acceptable (missing required field, wrong range).
 * It extends IllegalArgumentException, so code that catches the standard exception still works.
 */
public class ValidationException extends IllegalArgumentException {

    public ValidationException(String message) {
        super(message);
    }
}
