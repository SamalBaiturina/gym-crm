package com.epam.gymcrm.domain.model;

/**
 * Request to replace the password: current credentials plus the new password.
 */
public record PasswordChange(Credentials current, String newPassword) {

    public PasswordChange {
        if (current == null) {
            throw new IllegalArgumentException("current credentials are required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }
    }

    @Override
    public String toString() {
        return "PasswordChange{username='" + current.username() + "'}";
    }
}
