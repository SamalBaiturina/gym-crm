package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/** Body of the trainee registration request. Date of birth and address are optional. */
public record TraineeRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address) {
}
