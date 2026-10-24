package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of PUT /login. Username and old password come from the Authorization header. */
public record ChangePasswordRequest(@NotBlank String newPassword) {

    @Override
    public String toString() {
        return "ChangePasswordRequest{}";
    }
}
