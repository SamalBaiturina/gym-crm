package com.epam.gymcrm.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record User(String firstName, String lastName, String username,
                   String password, boolean isActive) {

    public static User of(String firstName, String lastName) {
        return new User(firstName, lastName, null, null, false);
    }

    public User withCredentials(String username, String password) {
        return new User(firstName, lastName, username, password, true);
    }

    public User withNames(String firstName, String lastName) {
        return new User(firstName, lastName, username, password, isActive);
    }

    public User withPassword(String password) {
        return new User(firstName, lastName, username, password, isActive);
    }

    public User withActive(boolean isActive) {
        return new User(firstName, lastName, username, password, isActive);
    }

    /** Constant-time comparison, so the response time does not reveal how many characters matched. */
    public boolean passwordMatches(String given) {
        if (password == null || given == null) {
            return false;
        }
        return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "User{firstName='" + firstName + "', lastName='" + lastName
                + "', username='" + username + "', isActive=" + isActive + "}";
    }
}
