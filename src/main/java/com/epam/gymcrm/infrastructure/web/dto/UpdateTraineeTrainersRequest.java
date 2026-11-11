package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Body of PUT /trainees/{username}/trainers: the new full list of the trainee's trainers. */
public record UpdateTraineeTrainersRequest(@NotNull List<@NotNull @Valid TrainerUsernameRequest> trainers) {
}
