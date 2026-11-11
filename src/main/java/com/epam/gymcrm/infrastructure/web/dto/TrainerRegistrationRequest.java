package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Registration of a trainer. The specialization is a reference to a training type by its id. */
public record TrainerRegistrationRequest(@NotBlank String firstName,
                                         @NotBlank String lastName,
                                         @NotNull Long specializationId) {
}
