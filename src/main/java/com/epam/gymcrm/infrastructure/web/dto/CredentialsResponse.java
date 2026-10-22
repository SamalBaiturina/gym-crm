package com.epam.gymcrm.infrastructure.web.dto;

/** Generated username and password, returned once after registration. */
public record CredentialsResponse(String username, String password) {

    @Override
    public String toString() {
        return "CredentialsResponse{username='" + username + "'}";
    }
}
