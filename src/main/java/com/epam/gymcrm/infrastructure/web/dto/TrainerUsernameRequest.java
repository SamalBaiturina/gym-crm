package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/** A reference to a trainer by username. */
public record TrainerUsernameRequest(@NotBlank String username) {
}
