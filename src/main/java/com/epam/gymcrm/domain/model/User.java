package com.epam.gymcrm.domain.model;

public record User(String firstName, String lastName, String username,
                   String password, boolean isActive) {

    public static User of(String firstName, String lastName) {
        return new User(firstName, lastName, null, null, false);
    }

    public User withCredentials(String username, String password) {
        return new User(firstName, lastName, username, password, true);
    }

    @Override
    public String toString() {
        return "User{firstName='" + firstName + "', lastName='" + lastName
                + "', username='" + username + "', isActive=" + isActive + "}";
    }
}
