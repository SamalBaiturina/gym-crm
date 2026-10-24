package com.epam.gymcrm.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body of PUT /trainers/{username}. The specialization is read-only: if the client sends it,
 * it is ignored.
 */
public record UpdateTrainerRequest(@NotBlank String firstName,
                                   @NotBlank String lastName,
                                   @NotNull @JsonProperty("isActive") Boolean isActive) {
}
