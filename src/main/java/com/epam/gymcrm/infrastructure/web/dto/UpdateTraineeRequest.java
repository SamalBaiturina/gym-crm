package com.epam.gymcrm.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Body of PUT /trainees/{username}. The username comes from the path and cannot be changed. */
public record UpdateTraineeRequest(@NotBlank String firstName,
                                   @NotBlank String lastName,
                                   LocalDate dateOfBirth,
                                   String address,
                                   @NotNull @JsonProperty("isActive") Boolean isActive) {
}
