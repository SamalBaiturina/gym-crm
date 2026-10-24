package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.application.service.TrainerProfile;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Response of GET and PUT /trainers/{username}. */
public record TrainerProfileResponse(String username, String firstName, String lastName,
                                     TrainingTypeResponse specialization,
                                     @JsonProperty("isActive") boolean isActive,
                                     List<TraineeSummaryResponse> trainees) {

    public static TrainerProfileResponse from(TrainerProfile profile) {
        Trainer trainer = profile.trainer();
        User user = trainer.user();
        return new TrainerProfileResponse(user.username(), user.firstName(), user.lastName(),
                TrainingTypeResponse.from(trainer.specialization()), user.isActive(),
                profile.trainees().stream().map(TraineeSummaryResponse::from).toList());
    }
}
