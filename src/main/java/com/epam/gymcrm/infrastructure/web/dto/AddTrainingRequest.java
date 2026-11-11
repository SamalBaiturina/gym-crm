package com.epam.gymcrm.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Body of POST /trainings. The training type is not sent: it is the trainer's specialization. */
public record AddTrainingRequest(@NotBlank String traineeUsername,
                                 @NotBlank String trainerUsername,
                                 @NotBlank String trainingName,
                                 @NotNull LocalDate trainingDate,
                                 @NotNull @Positive Integer trainingDuration) {
}
