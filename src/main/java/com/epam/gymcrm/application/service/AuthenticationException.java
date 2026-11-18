package com.epam.gymcrm.application.service;

/**
 * The caller could not be authenticated: the Authorization header is missing or malformed, the username is
 * unknown or the password is wrong. The web layer answers it with 401.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Invalid username or password");
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
