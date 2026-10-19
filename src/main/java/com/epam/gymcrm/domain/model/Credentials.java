package com.epam.gymcrm.domain.model;

/**
 * Username and password of a person who calls an operation.
 * Both parts are required, so an invalid pair cannot be created.
 */
public record Credentials(String username, String password) {

    public Credentials {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
    }

    @Override
    public String toString() {
        return "Credentials{username='" + username + "'}";
    }
}
