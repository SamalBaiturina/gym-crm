package com.epam.gymcrm.infrastructure.web.dto;

import com.epam.gymcrm.application.service.TraineeProfile;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/** Response of GET and PUT /trainees/{username}. */
public record TraineeProfileResponse(String username, String firstName, String lastName,
                                     LocalDate dateOfBirth, String address,
                                     @JsonProperty("isActive") boolean isActive,
                                     List<TrainerSummaryResponse> trainers) {

    public static TraineeProfileResponse from(TraineeProfile profile) {
        Trainee trainee = profile.trainee();
        User user = trainee.user();
        return new TraineeProfileResponse(user.username(), user.firstName(), user.lastName(),
                trainee.dateOfBirth(), trainee.address(), user.isActive(),
                profile.trainers().stream().map(TrainerSummaryResponse::from).toList());
    }
}
